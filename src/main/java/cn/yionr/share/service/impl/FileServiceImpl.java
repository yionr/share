package cn.yionr.share.service.impl;

import cn.yionr.share.dao.SFileDao;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.intf.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    public List<String> codePool = new ArrayList<>();
    {
        //generate a codePool 4number like 0001/0789
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
    }

    @Autowired
    SFileDao sFileDao;

    public String upload(SFileWrapper sfw) throws IOException {
//        remove one from codePool as fid
        sfw.getsFile().setFid(codePool.remove((int)(Math.random() * (codePool.size()+1))));
//        upload file
        String dst = "/Users/yionr/temp";
        File dstFile = new File(dst,sfw.getsFile().getFid());
        if (!dstFile.exists())
            dstFile.createNewFile();
        IOUtils.copy(new FileInputStream(sfw.getFile()),new FileOutputStream(dstFile));
//        update database
        sFileDao.addSFile(sfw.getsFile());
        System.out.println(sfw.getsFile().getFid());
        return sfw.getsFile().getFid();
    }
}
