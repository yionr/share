package cn.yionr.share.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @RequestMapping("/regedit")
    public String regedit(String email,String password){
        System.out.println(email + " , " + password);
        return "success";
    }
    @RequestMapping("/login")
    public String login(String email,String password){
        System.out.println(email + " , " + password);
        return "success";
    }
}
