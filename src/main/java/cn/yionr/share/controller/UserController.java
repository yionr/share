package cn.yionr.share.controller;

import cn.yionr.share.entity.User;
import cn.yionr.share.service.intf.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }
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
