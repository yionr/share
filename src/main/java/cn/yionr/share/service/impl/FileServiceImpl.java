package cn.yionr.share.service.impl;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.mapper.SFileMapper;
import cn.yionr.share.mapper.UserMapper;
import cn.yionr.share.service.FileService;
import cn.yionr.share.service.exception.*;
import cn.yionr.share.tool.HBaseUtils;
import cn.yionr.share.tool.HadoopUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    SFileMapper sFileMapper;
    UserMapper userMapper;

    HBaseUtils hBaseUtils;
    HadoopUtils hadoopUtils;

    Map<String, Long> fileMap = new ConcurrentHashMap<>();

    List<String> codePool = new ArrayList<>();

    @Autowired
    public FileServiceImpl(SFileMapper sFileMapper, UserMapper userMapper, HBaseUtils hBaseUtils, HadoopUtils hadoopUtils) throws IOException {
        this.sFileMapper = sFileMapper;
        this.userMapper = userMapper;
        this.hBaseUtils = hBaseUtils;
        this.hadoopUtils = hadoopUtils;

        // 构造一个包含0000~9999的List

        for (int i = 0; i < 10000; i++) {
            if (i < 10)
                codePool.add("000" + i);
            else if (i < 100)
                codePool.add("00" + i);
            else if (i < 1000)
                codePool.add("0" + i);
            else
                codePool.add("" + i);
        }
        log.info("codePoll一共{}",codePool.size());

        List<String> localCodes = hBaseUtils.scanRowKey();
        localCodes.addAll(hadoopUtils.listFiles());
        List<String> remoteCodes = sFileMapper.listCodes();

        log.info("数据库现有取件码: {}", remoteCodes.toString());
        log.info("本地现有取件码: {}", localCodes.toString());

        localCodes.removeAll(remoteCodes);

//        现在这种情况下， 如果释放取件码（！本地文件并不会立刻没掉，这样用户如果随到这个码，那就会在存储时冲突！，所以必须也释放文件）会导致下次启动时本地出现很多无效文件，自动删除
//        如果释放文件，会导致取件码只能标记状态，浪费了，本来标记状态时为了以后能够有个万一，取回的。
//        所以，前端只有一个接口release就行了，先释放文件，然后释放取件码...说回来，只释放一个没有任何作用
        if (localCodes.isEmpty())
            log.info("本地无无效文件");
        else {
            log.error("本地无效文件: {}, 以上文件的数据库记录已丢失！删除文件。。。", localCodes.toString());
            for (String code : localCodes)
                removeInLocal(code);
        }

//        FIXME codePool的维护还没完全检查过一遍，毫无疑问，存在问题，复现方式为： 让取件码无效，然后重启服务器，然后release
//        启动时：扫描所有无效文件，移入回收站 -》 建立fileMap，维护剩下的正常的文件 -》 定期扫描，移入回收站
        for (String code : sFileMapper.listCodes()) {
            int deadTime = sFileMapper.queryUID(code) == -1 ? 7 : 30;
            if (Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(sFileMapper.queryUploaded_time(code)), ZoneId.systemDefault()), LocalDateTime.now()).toDays() > deadTime || sFileMapper.queryTimes(code) <= 0) {
                if (existsInLocal(code)){
                    if (!existsInTrash(code)){
                        log.info("{}文件已过期/下载次数用尽，移至回收站",code);
                        removeToTrash(code, sFileMapper.queryFiletype(code));
                    }
                }else {
                    sFileMapper.delete(code);
                }
            } else {
                if (existsInLocal(code)){
                    if (!existsInTrash(code)){
                        fileMap.put(code, sFileMapper.queryUploaded_time(code));
                        codePool.remove(code);
                    } else
                        log.warn("{}为什么正常的文件会在回收站？",code);
                } else {
                    log.warn("本地文件 {} 丢失...\t数据库信息为:{},删除该记录",code,sFileMapper.queryFile(code).toString());
                    sFileMapper.delete(code);
                }
            }
        }
        log.info("fileMap建立完成：{}", fileMap.toString());
        log.info("移除已占用的code后codePool剩余：{}",codePool.size());

//        该线程访问数据库频繁，要想减少，得把更多数据存到fileMap中
        new Thread(() -> {
            while (true) {
                log.info("开始定期检查并删除过期文件、次数为0的文件（保留数据库记录）");
                log.info("fileMap内容为：" + fileMap.toString());

                Iterator<Map.Entry<String, Long>> iterator = fileMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> entry = iterator.next();
                    String code = entry.getKey();
                    boolean visitor = sFileMapper.queryUID(code) == -1;
                    int deadTime = visitor ? 7 : 30;
                    if (Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getValue()), ZoneId.systemDefault()), LocalDateTime.now()).toDays() > deadTime || sFileMapper.queryTimes(code) <= 0) {
                        log.info("文件{}已过期/下载次数用尽，移至回收站,同时删除fileMap记录", code);
                        try {
                            removeToTrash(code, sFileMapper.queryFiletype(code));
                            iterator.remove();
                        } catch (IOException e) {
                            log.warn("移至回收站发生异常");
                            iterator.remove();
                        }
                    }
                }

                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    log.error("线程sleep出问题了");
                }

            }
        }).start();
    }


    public String upload(SFileWrapper sfw, String email) throws IOException, NoLastsCodeException {
        if (codePool.size() == 0) {
            throw new NoLastsCodeException("取件码池已干涸，本系统无法再提供上传服务");
        }

        if (email == null) {
            sfw.getSFile().setUid(-1);
            log.info("设置uid为: -1 (游客)");
        } else {
            int uid = userMapper.queryUser(email).getUid();
            sfw.getSFile().setUid(uid);
            log.info("设置uid为: " + uid);
        }

        sfw.getSFile().setUploaded_time(new Date().getTime());

        String fid = codePool.remove((int) (Math.random() * codePool.size()));
        log.info("从池中随到取件码: " + fid);
        log.info("池剩余取件码个数: " + codePool.size());
        sfw.getSFile().setFid(fid);

        if (sfw.getSFile().getFiletype().equals("text"))
            sfw.getSFile().setName(fid);

        save(sfw);

        return fid;
    }

    public Map<String, Object> check(String code, String password) throws FileOutOfDateException, TimesRunOutException, IOException, FileLostException, NeedPasswordException, WrongPasswordException, CodeNotFoundException {
        Map<String, Object> result = new HashMap<>();

        if (existsInDB(code)) {
            if (outOfDate(code))
                throw new FileOutOfDateException("文件已过期");
            if (runOutOfTimes(code))
                throw new TimesRunOutException("下载次数已用完");
            String filetype = sFileMapper.queryFiletype(code);
            if (!existsInLocal(code))
                throw new FileLostException("本地文件已丢失");

            String realPassword = sFileMapper.queryPassword(code);
            result.put("filetype", filetype);

//            能执行到这里，说明文件一定是存在的
            if (comparePassword(password, realPassword)) {
//                这里是能把图片类型记录下来的，去数据库查一下文件名就行了，但是好像不太需要，等以后万一有问题再说
                if (!filetype.equals("file"))
                    result.put("content", getContent(code, filetype));
                return result;
            } else {
                if (password.isEmpty())
                    throw new NeedPasswordException("需要密码");
                else
                    throw new WrongPasswordException("密码错误");
            }
        } else {
            throw new CodeNotFoundException("取件码不存在");
        }
    }

    public SFileWrapper download(String code, String password) throws IOException, IllegalOperationException {
        if (!existsInDB(code) || runOutOfTimes(code) || !existsInLocal(code) || !comparePassword(password, sFileMapper.queryPassword(code)))
            throw new IllegalOperationException("非法操作");
        return getSFileWrapper(code);
    }

    public int release() throws IOException {
        hBaseUtils.releaseTrash();
        hadoopUtils.releaseTrash();
        log.info("已清空回收站");
        List<String> codes = sFileMapper.listCodes();
        codes.removeAll(hBaseUtils.scanRowKey());
        codes.removeAll(hadoopUtils.listFiles());
        for (String code : codes) {
            sFileMapper.delete(code);
            codePool.add(code);
        }
        log.info("{} 以上取件码文件已失效，已释放取件码，码池中剩余取件码数量：{}", codes.toString(), codePool.size());
        return codes.size();
    }

    public boolean comparePassword(String password1, String password2) {
        if (password1 == null)
            password1 = "";
        if (password2 == null)
            password2 = "";
        return password1.equals(password2);
    }

    public boolean existsInDB(String code) {
        String filename = sFileMapper.queryFileName(code);
        if (filename == null)
            filename = "";
        return !filename.equals("");

    }

    public boolean existsInLocal(String code) throws IOException {
        return hBaseUtils.exists(code) || hadoopUtils.exists(code);
    }
    public boolean existsInTrash(String code) throws IOException {
        return hBaseUtils.inTrash(code) || hadoopUtils.inTrash(code);
    }

    public boolean outOfDate(String code) {
        int uid = sFileMapper.queryUID(code);
        long during = Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(sFileMapper.queryUploaded_time(code)), ZoneId.systemDefault()), LocalDateTime.now()).toDays();
        if (uid != -1) {
            return during > 30;
        } else {
            return during > 7;
        }

    }

    public boolean runOutOfTimes(String code) {
        return sFileMapper.queryTimes(code) <= 0;
    }

    public void save(SFileWrapper sFileWrapper) throws IOException {
        SFile sFile = sFileWrapper.getSFile();
        String filetype = sFile.getFiletype();
        if ("file".equals(filetype)) {
            File file = new File(sFile.getFid());
            file.createNewFile();
            FileUtils.copyFile(sFileWrapper.getFile(), file);
            hadoopUtils.save(file);
            file.delete();
        } else if ("text".equals(filetype)) {
            hBaseUtils.insertOne(sFile.getFid(), filetype, new String(FileUtils.readFileToByteArray(sFileWrapper.getFile())));
        } else {
            String s = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(sFileWrapper.getFile()));
            hBaseUtils.insertOne(sFile.getFid(), filetype, s);
        }
        sFileMapper.addSFile(sFile);
        fileMap.put(sFile.getFid(), sFile.getUploaded_time());
        sFileWrapper.getFile().delete();
    }

    public void removeToTrash(String code, String filetype) throws IOException {
        if ("file".equals(filetype)) {
            hadoopUtils.moveToTrash(code);
        } else {
            Result select = hBaseUtils.select(code);
            byte[] value = select.getValue(hBaseUtils.dataColumnFamily.getBytes(), filetype.getBytes());
            hBaseUtils.delete(code, filetype);
            hBaseUtils.insertOne(code,"trash",new String(value));
        }

        fileMap.remove(code);
    }

    public void removeInLocal(String code) throws IOException {
        if (hadoopUtils.exists(code))
            hadoopUtils.delete(code);
        else
            hBaseUtils.delete(code);
    }


    /**
     * 该接口主要用来对以后用户进行删除的操作，目前可以暂时封掉
     */
//    public boolean delete(String code) {
//        return removeInLocal(code) && removeInDB(code);
//    }


    public byte[] readContent(String code, String filetype) throws IOException {
        Result select = hBaseUtils.select(code);
        byte[] value = select.getValue(hBaseUtils.dataColumnFamily.getBytes(), filetype.getBytes());
        decreaseTimes(code);
        return value;
    }

    public String getContent(String code, String filetype) throws IOException {

        byte[] content = readContent(code, filetype);

        return new String(content);
    }

    public SFileWrapper getSFileWrapper(String code) throws IOException {
        File file = hadoopUtils.get(code);
        decreaseTimes(code);
        return SFileWrapper.builder().file(file).sFile(SFile.builder().name(sFileMapper.queryFileName(code)).build()).build();
    }

    public void decreaseTimes(String code) {
        sFileMapper.decreaseTime(code);
        int times = sFileMapper.queryTimes(code);
        log.info("该文件剩余下载次数：{}", times);
        if (times <= 0)
            log.info("该文件下载次数已用完");
    }
}
