package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.entity.User;
import cn.yionr.share.service.impl.FileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class FileController {

    @Autowired
    FileServiceImpl fileService;

    /**
     *
     * @param file
     * @param password
     * @param times
     * @return 0 shows error ;
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(MultipartFile file,String password,int times) throws IOException {

        SFile sf = new SFile();
        sf.setName(file.getOriginalFilename());
        sf.setPassword(password);
        sf.setTimes(times);
        sf.setUid(1);

        SFileWrapper sfw = new SFileWrapper();
        sfw.setsFile(sf);
        File tempf = new File("tempfile");
        if(!tempf.exists())
            tempf.createNewFile();
        file.transferTo(tempf);

        sfw.setFile(tempf);
        try {
            return fileService.upload(sfw);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @GetMapping("")
    public String download(){
        return "";
    }
}
