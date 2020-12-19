package cn.yionr.share.service.impl;

import cn.yionr.share.mapper.SFileMapper;
import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.mapper.UserMapper;
import cn.yionr.share.service.exception.*;
import cn.yionr.share.service.FileService;
import cn.yionr.share.tool.CodePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    SFileMapper sFileMapper;
    UserMapper userMapper;
    String filePath;
    CodePool codePool;

    @Autowired
    public FileServiceImpl(CodePool codePool, SFileMapper sFileMapper, UserMapper userMapper, @Value("${files.dir}") String filePath) {
        this.codePool = codePool;
        this.sFileMapper = sFileMapper;
        this.filePath = filePath;
        this.userMapper = userMapper;


        //search in mysql , and exclude codes
        //遍历sql中的文件id，转换 去0 改变code的状态为被占用。
        List<String> sqlcode = sFileMapper.listCodes();
        for (String code : sqlcode
        ) {
            codePool.statchange(codePool.trimZero(code));
        }


        //check local files mappered with database , first with database ,
        // if database no some file remove them
        // else show all loosed files in logger
        /**
        * 对数据库操作 以及上传下载文件加上事务就不会有一下问题。
        * */
        List<String> remoteCodes = sFileMapper.listCodes();

        List<String> localCodes = new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File(filePath).list())));

        String[] remoteCodeArr = remoteCodes.toArray(new String[0]);

        log.info("本地文件存储路径为： " + filePath);
        log.info("数据库中保存有取件码: " + remoteCodes.toString());
        log.info("本地含有取件码文件: " + localCodes.toString());

        for (String code : remoteCodeArr) {
            if (localCodes.contains(code)) {
                log.debug("存在 " + code + " 即将清除。。。");
                remoteCodes.remove(code);
                localCodes.remove(code);
                log.debug(code + " 清除完毕！");
            }
        }

        if (!remoteCodes.isEmpty()) {
            //事务回滚--不会有以下问题
//            数据库中有取件码但是本地没有
            log.warn("异常取件码: " + remoteCodes.toString() + " , 以上取件码只存在于数据库中，并没有本地文件对应");
        }
        if (!localCodes.isEmpty()) {
//            本地中有取件码但是数据库没有记录
            log.warn("异常取件码: " + localCodes.toString() + " , 以上取件码只存在于本地，并没有数据库记录对应");
        }
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

        //获取一个code
        String fid = codePool.getCode();
        sfw.getSFile().setFid(fid);
        log.info("从池中随到取件码: " + fid);

        File dstFile = new File(filePath, sfw.getSFile().getFid());
        if (!dstFile.exists()) {
            if (dstFile.createNewFile()) {
                try {
                    IOUtils.copy(new FileInputStream(sfw.getFile()), new FileOutputStream(dstFile));
                } catch (IOException e) {
                    log.warn("文件拷贝失败");
                    throw new CopyFailedException("文件拷贝失败");
                }
                log.info("文件保存成功");
                if (sFileMapper.addSFile(sfw.getSFile()) == 1) {
                    log.info("记录存入数据库成功");
                    return sfw.getSFile().getFid();
                } else {
                    log.warn("记录存入数据库失败");
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
            //获取的取件码肯定不会重复，归还取件码会删除文件
            log.info("该取件码已有对应文件，算法出现错误！");
            throw new AlogrithmException("该取件码已有对应文件，算法出现错误！");
        }
    }

    public SFileWrapper download(String code, String password, Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException {
        if (sFileMapper.queryFile(code) != null) {
            String realPassword = sFileMapper.queryPassword(code);
            if (password == null) {
                if (realPassword == null)
                    if (check) {
                        return null;
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
                        return null;
                    } else {
                        return getSFileWrapper(code);
                    }
                }
            }
        } else {
            throw new CodeNotFoundException("取件码不存在");
        }

    }

    public SFileWrapper getSFileWrapper(String code) {
        String fileName = sFileMapper.queryFile(code);
        if (fileName != null) {
//            取件码有效，文件在数据库中存在的话
            SFileWrapper sFileWrapper = new SFileWrapper();
            sFileWrapper.setFile(new File(filePath, code));
            SFile sFile = new SFile();
            sFile.setName(fileName);
            sFileWrapper.setSFile(sFile);
            sFileMapper.decreaseTime(code);
//            如果取件次数上限，则删掉数据库记录，并删掉文件
            //并且归还code
            if (sFileMapper.queryTimes(code) <= -1) {
                sFileMapper.delect(code);
//                如果在这里删掉则会导致接下来Controller无法获取到文件，直接少了一次下载次数，所以可以暂时将小于0改为小于-1顶替一下
                sFileWrapper.getFile().delete();
            }
            return sFileWrapper;
        } else {
//            code invalid
            return null;
        }
    }

//    public List<String> show() {
//        return sFileDao.listCodes();
//    }
}
