package cn.yionr.share.controller;

import cn.yionr.share.entity.User;
import cn.yionr.share.service.intf.FileService;
import cn.yionr.share.service.intf.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("/regedit")
    public int regedit(String email,String password){
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return userService.regedit(user);
    }
    @RequestMapping("/login")
    public int login(String email,String password){
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return userService.login(user);
    }
}
