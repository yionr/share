package cn.yionr.share.service;

import cn.yionr.share.dao.FileDao;
import cn.yionr.share.entity.SFileWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    public List<Number> codePool = new ArrayList<>();
    {
        //generate a codePool
        for(int i = 0;i < 10000;i++)
            codePool.add(i);
    }

    @Autowired
    FileDao fileDao;


    public String upload(SFileWrapper fw) {
        fileDao.saveFile(fw);

    }
}
