package cn.yionr.share.service.impl;

import cn.yionr.share.dao.SFileDao;
import cn.yionr.share.entity.SFileWrapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class FileServiceImpl {
    public List<String> codePool = new ArrayList<>();
    Properties prop;

    {
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

        //load properties
        prop = new Properties();
        try {
            prop.load(FileServiceImpl.class.getResourceAsStream("/common.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    SFileDao sFileDao;

    public String upload(SFileWrapper sfw) throws IOException {
//        remove one from codePool as fid
        sfw.getsFile().setFid(codePool.remove((int)(Math.random() * (codePool.size()+1))));
//        upload file
        File dstFile = new File(prop.getProperty("tempDir"),sfw.getsFile().getFid());
        System.out.println(dstFile.getAbsolutePath());
        if (!dstFile.exists())
            dstFile.createNewFile();
        IOUtils.copy(new FileInputStream(sfw.getFile()),new FileOutputStream(dstFile));
//        update database
        sFileDao.addSFile(sfw.getsFile());
        System.out.println(sfw.getsFile().getFid());
        return sfw.getsFile().getFid();
    }
}
