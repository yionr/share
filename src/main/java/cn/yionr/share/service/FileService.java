package cn.yionr.share.service;

import cn.yionr.share.dao.FileDao;
import cn.yionr.share.entity.HdfsFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class FileService {

    @Autowired
    FileDao fileDao;


    public void listFiles(){
        try {
            fileDao.show("/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(InputStream in, String remotePath){
        try {
            fileDao.upload(in,remotePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
