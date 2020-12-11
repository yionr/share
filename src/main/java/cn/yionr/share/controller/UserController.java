package cn.yionr.share.controller;

import cn.yionr.share.entity.User;
import cn.yionr.share.service.intf.UserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    /**
     *
     * @return 0: 邮箱已存在； 1： 注册成功
     */
    @PostMapping("/regedit")
    public String regedit(User user, HttpServletResponse response, HttpServletRequest request) throws JSONException {
//TODO        注册需要验证邮箱有效性
        JSONObject json = new JSONObject();
        int status = userService.regedit(user);
        if (status == 1){
            addSession(request, response, user);
        }
        return json.put("status",status).toString();
    }

    /**
     *
     * @return -1： 邮箱不存在； 0： 密码错误； 1： 登陆成功
     */
    @PostMapping("/login")
    public String login(User user, HttpServletResponse response, HttpServletRequest request) throws JSONException {
        JSONObject json = new JSONObject();
//        log.info("test " + user.toString() + " , " + request.getAttribute("email"));
        if (user.getEmail() == null){
//            自动登录
            log.info("客户端发起了一次自动登录请求");
            HttpSession session= request.getSession();
            User tempUser = new User();
            tempUser.setEmail((String) session.getAttribute("email"));
            tempUser.setPassword((String) session.getAttribute("password"));
            log.info("session中包含的信息：" + tempUser.toString());
            int status = userService.login(tempUser);
            if (status == 0){
                log.info("密码已修改，即将清除session");
                session.invalidate();
            }
            return json.put("status", status).toString();
        }
//        手动登录
        log.info("客户端发起了一次手动登录请求");
        log.info("请求中包含的信息：" + user.getEmail() + " ; " + user.getPassword());
        int status = userService.login(user);
        if (status == 1){
            addSession(request, response, user);
        }
        return json.put("status",status).toString();
    }

    @PostMapping("/exit")
    public String exit(HttpServletRequest request) throws JSONException {
        HttpSession session = request.getSession();
        session.invalidate();
        return new JSONObject().put("status",2).toString();
    }
    void addSession(HttpServletRequest request,HttpServletResponse response,User user){
//            创建session，设置账号密码，然后返回cookie
        HttpSession session = request.getSession();
//            如果session已经存在了？ 说明该用户正在注册多个账户。 session重复设置一个属性会覆盖，旧的就丢失了。
//            每次注册成功都是要刷新session持续时间的，手动登录的话也是，但是我的一般是自动登录，如果是自动登录，则不刷新session保存时间
        session.setAttribute("email",user.getEmail());
        session.setAttribute("password",user.getPassword());
        Cookie cookie  = new Cookie("JSESSIONID",session.getId());
        cookie.setMaxAge(60*60*24*7);
        response.addCookie(cookie);
    }
}
