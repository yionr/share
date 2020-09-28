package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@RestController
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public String upload(MultipartFile file,String password,int times) throws IOException {
        SFile sf = new SFile();
        sf.setName(file.getOriginalFilename());
//        remove one from codePool as fid
        sf.setFid(fileService.codePool.remove((int)(Math.random() * (fileService.codePool.size()+1))).intValue());
        sf.setPassword(password);
        sf.setTimes(times);

        SFileWrapper sfw = new SFileWrapper();
        sfw.setsFile(sf);
        sfw.setFile(file.getResource().getFile());

        String dst = "/temp/" + sf.getFid();
        try {
            fileService.upload(sfw);
            return ("000" + sf.getFid())
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("")
    public String download(){
        return "";
    }
}
