package cn.yionr.share.controller;

import cn.yionr.share.dao.FileDao;
import cn.yionr.share.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

@RestController
public class FileController {
    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        System.out.println("收到文件，名字为： " + file.getOriginalFilename());
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        String remotePath = "/temp/" + file.getOriginalFilename();
        try {
            fileService.uploadFile(file.getInputStream(),remotePath);
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "failed";
        }
    }

    @GetMapping("")
    public String download(){
        return "";
    }

    @GetMapping("/show")
    public void show(){
        fileService.listFiles();
    }
}
