package cn.yionr.share.filter;

import cn.yionr.share.mapper.UserMapper;
import cn.yionr.share.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "permissionFilter", urlPatterns = {"/upload.do","/checkFile"})
public class PermissionFilter implements Filter {

    UserMapper userMapper;

    @Autowired
    public PermissionFilter(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest) servletRequest).getSession();
        User user;
        if (session.getAttribute("email") != null) {
            user = userMapper.queryUser((String) session.getAttribute("email"));
            if (user.getPassword().equals(session.getAttribute("password"))) {
//                账号密码都正确
                log.info("用户操作");
                servletRequest.setAttribute("visitor", false);
            } else {
//                密码错误
                log.info("用户密码错误");
            }
        } else {
//            游客
            log.info("游客操作");
            servletRequest.setAttribute("visitor", true);
        }
//        限制文件大小要在这里，否则直接抛异常了
//        FIXME 在配置文件中，将单文件容量加到10G，这样大部分时候都不会出现异常了
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
