package cn.yionr.share.service.impl;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.mapper.SFileMapper;
import cn.yionr.share.mapper.UserMapper;
import cn.yionr.share.service.FileService;
import cn.yionr.share.service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    SFileMapper sFileMapper;
    UserMapper userMapper;

    String filePath;
    String textFilePath;
    String imageFilePath;
    String trashBinPath;

    Map<String, Long> fileMap;

    public List<String> codePool = new ArrayList<>();

    @Autowired
    public FileServiceImpl(SFileMapper sFileMapper, UserMapper userMapper, @Value("${files.dir}") String filePath, @Value("${textFiles.dir}") String textFilePath, @Value("${imageFiles.dir}") String imageFilePath,@Value("${trashBin.dir}") String trashBinPath) {

        this.filePath = filePath;
        this.textFilePath = textFilePath;
        this.imageFilePath = imageFilePath;
        this.trashBinPath = trashBinPath;

        this.sFileMapper = sFileMapper;
        this.userMapper = userMapper;



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

        List<String> remoteCodes = sFileMapper.listCodes();

        File localFileDir = new File(filePath);
        File localTextFileDir = new File(textFilePath);
        File localImageFileDir = new File(imageFilePath);
        File localTrashBinDir = new File(trashBinPath);

        if (!localFileDir.exists()) {
            log.warn("文件存放目录不存在，即将自动创建");
            if (localFileDir.mkdirs())
                log.info("创建成功");
            else
                log.error("创建失败");
        }
        if (!localTextFileDir.exists()) {
            log.warn("文本存放目录不存在，即将自动创建");
            if (localTextFileDir.mkdirs())
                log.info("创建成功");
            else {
                log.error("创建失败");
                System.exit(0);
            }

        }
        if (!localImageFileDir.exists()) {
            log.warn("图片存放目录不存在，即将自动创建");
            if (localImageFileDir.mkdirs())
                log.info("创建成功");
            else {
                log.error("创建失败");
                System.exit(0);
            }

        }
        if (!localTrashBinDir.exists()) {
            log.warn("回收站不存在，即将自动创建");
            if (localTrashBinDir.mkdirs())
                log.info("创建成功");
            else {
                log.error("创建失败");
                System.exit(0);
            }

        }

        for (File file : localTrashBinDir.listFiles()) {
            if (!file.delete()) {
                log.warn("回收站文件"+ file.getName() +"删除失败");
            }
        }
        log.info("其余回收站文件已清空");


        List<String> localCodes = new ArrayList<>(Arrays.asList(Objects.requireNonNull(localFileDir.list())));
        localCodes.addAll(new ArrayList<>(Arrays.asList(Objects.requireNonNull(localTextFileDir.list()))));
        localCodes.addAll(new ArrayList<>(Arrays.asList(Objects.requireNonNull(localImageFileDir.list()))));


        List<String> localCodesClone = new ArrayList<>(localCodes);

        String[] remoteCodeArr = remoteCodes.toArray(new String[0]);

        log.info("服务器文件存储路径为： " + filePath);
        log.info("服务器文本存储路径为： " + textFilePath);
        log.info("服务器图片存储路径为： " + imageFilePath);
        log.info("服务器回收站路径为： " + trashBinPath);
        log.info("=====================================");
        log.info("数据库现有取件码: " + remoteCodes.toString());
        log.info("本地现有取件码: " + localCodes.toString());

        for (String code : remoteCodeArr) {
            if (localCodes.contains(code)) {
                remoteCodes.remove(code);
                localCodes.remove(code);
                codePool.remove(code);
            }
        }

        if (remoteCodes.isEmpty() && localCodes.isEmpty())
            log.info("数据库取件码与本地文件一一对应，没有异常取件码！");
        else {
            if (!remoteCodes.isEmpty()) {
//            数据库中有取件码但是本地没有
                log.error("异常取件码: " + remoteCodes.toString() + " , 以上取件码的本地文件已丢失！即将删除记录。。。");
                for (String code : remoteCodes)
                    deleteInfo(code);
            }
            if (!localCodes.isEmpty()) {
//            本地中有取件码但是数据库没有记录
                log.error("异常取件码: " + localCodes.toString() + " , 以上取件码的数据库记录已丢失！即将删除文件。。。");
                List<File> fileList = new ArrayList<>();
                for (String code : localCodes) {
                    fileList.add(new File(filePath,code));
                    fileList.add(new File(textFilePath,code));
                    fileList.add(new File(imageFilePath,code));
                }
                for (File item :
                        fileList) {
                    if (item.exists()){
                        if (item.delete()) {
                            log.info("文件 " + item.getName() + " 删除成功");
                        }
                        else{
                            log.warn("文件 " + item.getName() + " 删除失败");
                        }
                    }

                }
            }
        }
//        FIXME 这种方式要求三个文件夹不能有包含关系

//TODO 所有 和文件删除有关的都交给线程去做。 其他地方不能删除的话，直接去掉会有bug，之前为了图方便，下载部分没做次数的校验，只是文件存在就能下载，之后吧这个改了

        Thread t = new Thread(() -> {
            while (true) {
//                将文件删除至trash文件夹，且在服务器容量不足或其他条件下清空该文件夹
                log.info("开始定期检查并删除过期文件、次数为0的文件（保留数据库记录）");
                log.debug("fileMap内容为：" + fileMap.toString());
//                遍历map,将outofdate的file delete
                Iterator<Map.Entry<String, Long>> iterator = fileMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> entry = iterator.next();
                    String code = entry.getKey();
                    boolean visitor = sFileMapper.queryUID(code) == -1;
                    int deadTime = visitor ? 7 : 30;
                    if (Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getValue()), ZoneId.systemDefault()), LocalDateTime.now()).toDays() > deadTime) {
                        log.info("文件 " + code + " 已过期，即将删除(移至回收站)");
                        try {
                            deleteFile(code);
                            iterator.remove();
                            log.info("删除成功，同时删除fileMap记录");
                        } catch (FileNotFoundException e) {
                            log.warn(e.getMessage());
                            iterator.remove();
                            log.info("已在fileMap中移除维护！");
                        }
                    }
//                    过期文件移至回收站，在前端会显示过期。那数据库记录什么时候删掉？
//                    既过期又没次数的文件，优先显示哪个？ a: 过期文件
//                    没次数的文件移至回收站，在前端显示没次数，那数据库记录什么时候删掉？
//                    之所以移至回收站，是为以后的额外功能做准备，所以现在回收站的作用就是，让过期以及没次数的文件在前端能给用户显示，同时一直占用着数据库，在下次启动的时候删掉数据库冗余（暂时），并删掉回收站
                    else{
                        if (sFileMapper.queryTimes(code) <= 0){
                            log.info("文件 " + code + " 下载次数已用完，即将删除(移至回收站)");
                            try {
                                deleteFile(code);
                                iterator.remove();
                                log.info("删除成功，同时删除fileMap记录");
                            } catch (FileNotFoundException e) {
                                log.warn(e.getMessage());
                                iterator.remove();
                                log.info("已在fileMap中移除维护！");
                            }
                        }

                    }

                }
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    log.error("线程sleep出问题了");
                }

            }
        });

        Thread t1 = new Thread(() -> {
//            获取所有的本地文件，放入fileMap
//            当文件被删除时，同时remove mapitem 添加文件也要additem
            for (String fid : localCodesClone)
                fileMap.put(fid, sFileMapper.queryFile(fid).getUploaded_time());
            log.info("fileMap建立完成，内容为：" + fileMap.toString());
            log.info("接下来开始定时任务");
            t.start();
        });

        fileMap = new HashMap<>();
        t1.start();
    }

    public String upload(SFileWrapper sfw, String email) throws IOException, AlogrithmException, FailedCreateFileException, FailedSaveIntoDBException, CopyFailedException {
        if (email == null) {
            sfw.getSFile().setUid(-1);
            log.info("设置uid为: -1 (游客)");
        } else {
            int uid = userMapper.queryUser(email).getUid();
            sfw.getSFile().setUid(uid);
            log.info("设置uid为: " + uid);
        }

        sfw.getSFile().setUploaded_time(new Date().getTime());

        String fid = codePool.remove((int) (Math.random() * (codePool.size() + 1)));
        log.info("从池中随到取件码: " + fid);
        log.info("池剩余取件码个数: " + (10000 - codePool.size()));
        sfw.getSFile().setFid(fid);
        File dstFile;
        if (sfw.getSFile().getFiletype().equals("text")) {
            sfw.getSFile().setName(fid);
            dstFile = new File(textFilePath, sfw.getSFile().getFid());
        } else if (sfw.getSFile().getFiletype().equals("image")) {
            dstFile = new File(imageFilePath, sfw.getSFile().getFid());
        } else {
            dstFile = new File(filePath, sfw.getSFile().getFid());
        }
        if (!dstFile.exists()) {
            if (dstFile.createNewFile()) {
                try {
                    FileUtils.copyFile(sfw.getFile(), dstFile);
                    log.info("文件保存成功,即将删除临时文件");
                    if (sfw.getFile().delete()) {
                        log.info("临时文件删除成功");
                    } else {
                        log.warn("临时文件删除失败");
                    }
                    fileMap.put(fid, sfw.getSFile().getUploaded_time());

                } catch (IOException e) {
                    log.warn("文件拷贝失败");
                    throw new CopyFailedException("文件拷贝失败");
                }
                if (sFileMapper.addSFile(sfw.getSFile()) == 1) {
                    log.info("记录存入数据库成功");
                    return sfw.getSFile().getFid();
                } else {
                    log.warn("记录存入数据库失败,准备删除仓库文件");
                    if (dstFile.delete()) {
                        log.info("文件删除成功,并同步删除fileMap的记录");
                        fileMap.remove(sfw.getSFile().getFid());
                    } else {
                        log.warn("文件删除失败");
                    }
                    throw new FailedSaveIntoDBException("记录存入数据库失败");
                }
            } else {
                log.error("文件创建失败");
                throw new FailedCreateFileException("文件创建失败");
            }
        } else {
            log.error("该取件码已有对应文件，算法出现错误！");
            throw new AlogrithmException("该取件码已有对应文件，算法出现错误！");
        }
    }

//    TODO 要不要把check和download 分为两个方法呢

    public Map<String, Object> download(String code, String password, Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException, FileLostException, FileOutOfDateException, TimesRunOutException {
        Map<String, Object> result = new HashMap<>();

        if (existsInDB(code)) {
//            先测试过期，再测试丢失
            if (outOfDate(code))
                throw new FileOutOfDateException("文件已过期");
            if (sFileMapper.queryTimes(code) <= 0)
                throw new TimesRunOutException("下载次数已用完");
            String filetype = sFileMapper.queryFiletype(code);
            if (!existsInLocal(code, filetype))
                throw new FileLostException("服务器文件已丢失");
            String realPassword = sFileMapper.queryPassword(code);
            result.put("filetype", filetype);

//            能执行到这里，说明文件一定是存在的
            if (comparePassword(password, realPassword)) {
                if (check) {
                    if (!filetype.equals("file")) {
                        result.put("content", getContent(code, filetype));
                    }
                } else {
//                    下载
                    result.put("content", getSFileWrapper(code));
                }
                return result;
            } else {
                if ("".equals(password))
                    throw new NeedPasswordException("需要密码");
                else
                    throw new WrongPasswordException("密码错误");
            }
        } else {
            throw new CodeNotFoundException("取件码不存在");
        }

    }

    public boolean comparePassword(String password1, String password2) {
        if (password1 == null)
            password1 = "";
        if (password2 == null)
            password2 = "";
        return password1.equals(password2);
    }

    public boolean existsInDB(String code) {
        String filename = sFileMapper.queryFile(code).getName();
        if (filename == null)
            filename = "";
        return !filename.equals("");

    }

    public boolean existsInLocal(String code, String filetype) {
        switch (filetype) {
            case "text":
                return new File(textFilePath, code).exists();
            case "image":
                return new File(imageFilePath, code).exists();
            default:
                return new File(filePath, code).exists();
        }
    }

    public boolean outOfDate(String code) {
        int uid = sFileMapper.queryUID(code);
        long during = Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(sFileMapper.queryFile(code).getUploaded_time()), ZoneId.systemDefault()), LocalDateTime.now()).toDays();
        if (uid != -1) {
            return during > 30;
        } else {
            return during > 7;
        }

    }

    public boolean deleteInfo(String code) {
        switch (sFileMapper.delete(code)) {
            case 1:
                log.info("数据库记录" + code + "删除成功");
                return true;
            case 0:
                log.warn("数据库没有记录被删除");
                return false;
            default:
                log.warn("删除数据库记录返回了一个异常值！");
                return false;
        }
    }

    /**
     * 移至回收站
     * @param code
     * @return
     * @throws FileNotFoundException
     */
    public boolean deleteFile(String code) throws FileNotFoundException {
        String filetype = sFileMapper.queryFiletype(code);
        String path;
        switch (filetype){
            case "text":
                path = textFilePath;
                break;
            case "image":
                path = imageFilePath;
                break;
            default:
                path = filePath;
                break;
        }
        File target = new File(path, code);
        if (!target.exists()) {
            throw new FileNotFoundException("文件不存在");
        } else {
            try {
                FileUtils.moveFileToDirectory(target,new File(trashBinPath),false);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public boolean delete(String code) throws FileNotFoundException {
        return deleteFile(code) && deleteInfo(code);
    }


    public String getContent(String code, String filetype) {
        String fileName = sFileMapper.queryFile(code).getName();
        if (fileName != null) {
            if (filetype.equals("text")) {
                File file = new File(textFilePath, code);
                InputStreamReader reader;
                try {
                    reader = new InputStreamReader(new FileInputStream(file));
                    BufferedReader br = new BufferedReader(reader);
                    String line;
                    StringBuilder content;
                    line = br.readLine();
                    content = new StringBuilder();
                    while (line != null) {
                        content.append(line).append("\n");
                        line = br.readLine(); // 一次读入一行数据
                    }
                    reader.close();
                    br.close();
                    decreaseTimes(code, file);
                    return content.toString();
                } catch (IOException e) {
                    log.error("读取文本时出现错误");
                }
            } else {
                try {
                    File file = new File(imageFilePath, code);
                    String result = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file));
                    decreaseTimes(code, file);
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return null;
        }
        return "";
    }

    public SFileWrapper getSFileWrapper(String code) {
        String fileName = sFileMapper.queryFile(code).getName();
        if (fileName != null) {
//            取件码有效，文件在数据库中存在的话
            File srcFile = new File(filePath, code);
            File tempFile;
            try {
                tempFile = File.createTempFile("tempFile", "temp");
                FileUtils.copyFile(srcFile, tempFile);
                SFileWrapper sFileWrapper = SFileWrapper.builder().
                        file(tempFile).
                        sFile(SFile.builder().name(fileName).build()).
                        build();
                decreaseTimes(code, srcFile);
                return sFileWrapper;
            } catch (IOException e) {
                log.error("临时文件创建失败/拷贝文件失败");
                return null;
            }

        } else {
//            code invalid
            return null;
        }
    }

    public void decreaseTimes(String code, File file) throws FileNotFoundException {
        sFileMapper.decreaseTime(code);
        int times = sFileMapper.queryTimes(code);
//            如果取件次数上限，则删掉数据库记录，并删掉文件
        log.info("该文件剩余下载次数：" + times);
        if (times <= 0)
            log.info("该文件下载次数已用完");
    }
}
