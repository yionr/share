package cn.yionr.share.service.impl;

import cn.yionr.share.dao.SFileDao;
import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.exception.NeedPasswordException;
import cn.yionr.share.service.intf.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    SFileDao sFileDao;

    @Value("${files.dir}")
    String filePath;

    public List<String> codePool = new ArrayList<>();

    @Autowired
    public FileServiceImpl(SFileDao sFileDao,@Value("${files.dir}") String filePath){

        //generate a codePool 4number from 0000-9999
        for(int i = 0;i < 10000;i++){
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
        codePool.removeAll(sFileDao.listCodes());

        //chk local files mappered with database , first with database ,
        // if database no some file remove them
        // else show all loosed files in logger

        List<String> remoteCodes = sFileDao.listCodes();

        List<File> localCodes = Arrays.asList(new File(filePath).listFiles());



    }

    public String upload(SFileWrapper sfw) throws IOException {
//        remove one from codePool as fid
        sfw.getsFile().setFid(codePool.remove((int)(Math.random() * (codePool.size()+1))));
//        upload file
        File dstFile = new File(filePath,sfw.getsFile().getFid());
        System.out.println(dstFile.getAbsolutePath());
        if (!dstFile.exists())
            dstFile.createNewFile();
        IOUtils.copy(new FileInputStream(sfw.getFile()),new FileOutputStream(dstFile));
//        update database
        sFileDao.addSFile(sfw.getsFile());
        System.out.println(sfw.getsFile().getFid());
        return sfw.getsFile().getFid();
    }
    public SFileWrapper download(String code) throws NeedPasswordException {
    /*
    去数据库中检查是否有文件，有的话
        0. 核对密码(暂时不做)
        1. 获取文件名
        2. 将允许下载次数(times)-1
        3. 读取指定目录，提取文件返回
     */
        String password = sFileDao.queryPassword(code);
        if (password == null)
                password = "";
        if (!"".equals(password)){
            throw new NeedPasswordException("需要密码");
        }

        return getSFileWrapper(code);
    }

    @Override
    public SFileWrapper download(String code, String password) {
        String currectPassword = sFileDao.queryPassword(code);
        if (!password.equals(currectPassword)){
            return null;
        }
        else{
//            密码正确，获取文件信息并返回
            return getSFileWrapper(code);
        }
    }



    public List<String> show(){
        return sFileDao.listCodes();
    }

    public SFileWrapper getSFileWrapper(String code){
        String fileName = sFileDao.queryFile(code);
        if (fileName != null){
//            取件码有效，文件在数据库中存在的话
            SFileWrapper sFileWrapper = new SFileWrapper();
            sFileWrapper.setFile(new File(filePath,code));
            SFile sFile = new SFile();
            sFile.setName(fileName);
            sFileWrapper.setsFile(sFile);
            sFileDao.decreaseTime(code);
//            如果取件次数上限，则删掉数据库记录，并删掉文件
            if (sFileDao.queryTimes(code) <= -1){
                sFileDao.delect(code);
//                如果在这里删掉则会导致接下来Controller无法获取到文件，直接少了一次下载次数，所以可以暂时将小于0改为小于-1顶替一下
                sFileWrapper.getFile().delete();
            }
            return sFileWrapper;
        }
        else{
//            code invalid
            return null;
        }
    }
}
