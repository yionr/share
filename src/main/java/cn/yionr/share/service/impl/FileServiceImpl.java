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
import java.util.*;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    SFileMapper sFileMapper;
    UserMapper userMapper;

    String filePath;
    String textFilePath;
    String imageFilePath;

    public List<String> codePool = new ArrayList<>();

    @Autowired
    public FileServiceImpl(SFileMapper sFileMapper, UserMapper userMapper, @Value("${files.dir}") String filePath, @Value("${textFiles.dir}") String textFilePath, @Value("${imageFiles.dir}") String imageFilePath) {

        this.sFileMapper = sFileMapper;
        this.filePath = filePath;
        this.textFilePath = textFilePath;
        this.imageFilePath = imageFilePath;
        this.userMapper = userMapper;
        //generate a codePool 4number from 0000-9999
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

        //search in mysql , and exclude codes
        codePool.removeAll(sFileMapper.listCodes());

        //check local files mappered with database , first with database ,
        // if database no some file remove them
        // else show all loosed files in logger

        List<String> remoteCodes = sFileMapper.listCodes();

        File localFileDir = new File(filePath);
        File localTextFileDir = new File(textFilePath);
        File localImageFileDir = new File(imageFilePath);

        if (!localFileDir.exists())
            localFileDir.mkdirs();
        if (!localTextFileDir.exists())
            localTextFileDir.mkdirs();
        if (!localImageFileDir.exists())
            localImageFileDir.mkdirs();

        List<String> localCodes = new ArrayList<>(Arrays.asList(Objects.requireNonNull(localFileDir.list())));
        localCodes.addAll(new ArrayList<>(Arrays.asList(Objects.requireNonNull(localTextFileDir.list()))));
        localCodes.addAll(new ArrayList<>(Arrays.asList(Objects.requireNonNull(localImageFileDir.list()))));

        String[] remoteCodeArr = remoteCodes.toArray(new String[0]);

        log.info("服务器文件存储路径为： " + filePath);
        log.info("服务器文本存储路径为： " + textFilePath);
        log.info("服务器图片存储路径为： " + imageFilePath);
        log.info("=====================================");
        log.info("数据库现有取件码: " + remoteCodes.toString());
        log.info("本地现有取件码: " + localCodes.toString());

        for (String code : remoteCodeArr) {
            if (localCodes.contains(code)) {
                remoteCodes.remove(code);
                localCodes.remove(code);
            }
        }

        if (!remoteCodes.isEmpty()) {
//            数据库中有取件码但是本地没有
            log.error("异常取件码: " + remoteCodes.toString() + " , 以上取件码的本地文件已丢失！");
        }
        if (!localCodes.isEmpty()) {
//            本地中有取件码但是数据库没有记录
            log.error("异常取件码: " + localCodes.toString() + " , 以上取件码的数据库记录已丢失！");
        }
//        FIXME 这种方式要求三个文件夹不能有包含关系
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

        String fid = codePool.remove((int) (Math.random() * (codePool.size() + 1)));
        log.info("从池中随到取件码: " + fid);
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
                    log.info("文件保存成功");
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
                        log.info("文件删除成功");
                    } else {
                        log.warn("文件删除失败");
                    }
                    throw new FailedSaveIntoDBException("记录存入数据库失败");
                }
            } else {
                log.warn("文件创建失败");
                throw new FailedCreateFileException("文件创建失败");
            }
        } else {
            log.info("该取件码已有对应文件，算法出现错误！");
            throw new AlogrithmException("该取件码已有对应文件，算法出现错误！");
        }
    }


    public Object download(String code, String password, Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException, FileNotFoundException {
        String[] result = new String[2];
        if (sFileMapper.queryFile(code) != null) {
            String realPassword = sFileMapper.queryPassword(code);
            if (password == null) {
                if (realPassword.equals("")) //FIXME 这里有瑕疵，不知道是null还是”“
                    if (check) {
                        String filetype = sFileMapper.queryFiletype(code);
                        result[0] = filetype;
                        if (!filetype.equals("file"))
                            result[1] = getContent(code, filetype);
                        return result;
                    } else {
                        return getSFileWrapper(code);
                    }
                else {
                    throw new NeedPasswordException("需要密码");
                }
            } else {
                if (!password.equals(realPassword)) {
                    throw new WrongPasswordException("密码错误");
                } else {
                    if (check) {
                        String filetype = sFileMapper.queryFiletype(code);
                        result[0] = filetype;
                        if (!filetype.equals("file"))
                            result[1] = getContent(code, filetype);
                        return result;
                    } else {
                        return getSFileWrapper(code);
                    }
                }
            }
        } else {
            throw new CodeNotFoundException("取件码不存在");
        }

    }

    @Override
    public boolean deleteInfo(String code) {
        switch (sFileMapper.delete(code)){
            case 1:
                log.info("数据库记录删除成功");
                return true;
            case 0:
                log.warn("数据库没有记录被删除");
                return false;
            default:
                log.warn("删除数据库记录返回了一个异常值！");
                return false;
        }
    }

    @Override
    public boolean deleteFile(File file) throws FileNotFoundException {
        if (!file.exists()){
            throw new FileNotFoundException("要删除的文件不存在");
        }
        else{
            if (file.delete()) {
                log.info("文件删除成功");
                return true;
            }
            else{
                log.error("文件删除失败");
                return false;
            }
        }
    }

    @Override
    public boolean delete(File file, String code) throws FileNotFoundException {
        return deleteInfo(code) && deleteFile(file);
    }


    //    FIXME 需要检测文件是否存在，如果不存在给前端报错
    public String getContent(String code, String filetype) throws FileNotFoundException {
        String fileName = sFileMapper.queryFile(code);
        if (fileName != null) {
            if (filetype.equals("text")) {
                File file = new File(textFilePath, code);
                if (!file.exists())
                    throw new FileNotFoundException("文件丢失");
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

    public SFileWrapper getSFileWrapper(String code) throws FileNotFoundException {
        String fileName = sFileMapper.queryFile(code);
        if (fileName != null) {
//            取件码有效，文件在数据库中存在的话
            File srcFile = new File(filePath, code);
            if (!srcFile.exists())
                throw new FileNotFoundException("文件丢失");
            File tempFile;
            try {
                tempFile = File.createTempFile("tempFile", "temp");
                FileUtils.copyFile(srcFile, tempFile);
                log.info("保存到临时文件的文件大小为" + FileUtils.sizeOf(tempFile));
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
        if (times <= 0) {
            log.info("该文件下载次数已用完，即将删除数据库和仓库数据");
            delete(file, code);
        }
    }
}
