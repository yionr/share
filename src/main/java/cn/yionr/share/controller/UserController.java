package cn.yionr.share.controller;

import cn.yionr.share.entity.User;
import cn.yionr.share.exception.UserAlreadyExsitException;
import cn.yionr.share.exception.UserNotExsitException;
import cn.yionr.share.exception.WrongPasswordException;
import cn.yionr.share.service.intf.UserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @return 0: 邮箱已存在； 1： 注册成功
     */
    @PostMapping("/regedit.do")
    public String regedit(User user, HttpServletResponse response, HttpServletRequest request) throws JSONException {
//TODO        注册需要验证邮箱有效性
        log.info("邮箱： " + user.getEmail() + " 提交注册");
        JSONObject json = new JSONObject();
        int status;
        try {
            status = userService.regedit(user);
            log.info("注册成功");
            addSession(request, response, user);
            return json.put("status", status).toString();
        } catch (UserAlreadyExsitException e) {
            log.info("邮箱已被注册");
            return json.put("status", 0).toString();
        }
    }

    /**
     * @return -1： 邮箱不存在； 0： 密码错误； 1： 登陆成功
     */
    @PostMapping("/login.do")
    public String login(User user, HttpServletResponse response, HttpServletRequest request) throws JSONException {
        JSONObject json = new JSONObject();
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        if (user.getEmail() == null) {
//            自动登录
            log.info("客户端发起了一次自动登录请求");
            log.info("客户端用户名为: " + email);
            User sessionUser = new User();
            sessionUser.setEmail((String) session.getAttribute("email"));
            sessionUser.setPassword((String) session.getAttribute("password"));
            try {
                userService.login(sessionUser);
                log.info("自动登录成功");
                json.put("status", 1);
            } catch (UserNotExsitException e) {
                log.warn("该用户不存在，即将清除session");
                session.invalidate();
                json.put("status", -1);
            } catch (WrongPasswordException e) {
                log.info("密码已修改，即将清除session");
                session.invalidate();
                json.put("status", 0);
            }

        } else {
//        手动登录
            log.info("客户端发起了一次手动登录请求");
            log.info("客户端用户名为: " + user.getEmail());
            try {
                userService.login(user);
                log.info("登录成功");
                addSession(request, response, user);
                json.put("status", 1);
            } catch (UserNotExsitException e) {
                log.warn("用户名不存在");
                json.put("status", -1);
            } catch (WrongPasswordException e) {
                log.info("密码错误");
                json.put("status", 0);
            }
        }
        return json.toString();

    }

    /**
     * @return 0: 退出登录并清除session成功
     */
    @PostMapping("/exit.do")
    public String exit(HttpServletRequest request) throws JSONException {
        log.info("客户端发起了退出登录的请求");
        HttpSession session = request.getSession();
        session.invalidate();
        log.info("已清除session");
        return new JSONObject().put("status", 0).toString();
    }

    void addSession(HttpServletRequest request, HttpServletResponse response, User user) {
        HttpSession session = request.getSession();
        session.setAttribute("email", user.getEmail());
        session.setAttribute("password", user.getPassword());
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(cookie);
        log.info("session添加成功");
    }
}
