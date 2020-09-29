package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.impl.FileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileController {

    @Autowired
    FileServiceImpl fileService;

    @PostMapping("/upload")
    public String upload(MultipartFile file,String password,int times) throws IOException {
        SFile sf = new SFile();
        sf.setName(file.getOriginalFilename());
        sf.setPassword(password);
        sf.setTimes(times);

        SFileWrapper sfw = new SFileWrapper();
        sfw.setsFile(sf);
        sfw.setFile(file.getResource().getFile());
        try {
            fileService.upload(sfw);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("")
    public String download(){
        return "";
    }
}
