package cn.yionr.share.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class select {

    @Autowired
    SFileDao sFileDao;

    @Test
    public void testSelect(){
        System.out.println(sFileDao.listCodes());
    }
    @Test
    public void te(){
    }
}
