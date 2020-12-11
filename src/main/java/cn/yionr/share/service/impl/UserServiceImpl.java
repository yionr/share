package cn.yionr.share.service.impl;

import cn.yionr.share.dao.UserDao;
import cn.yionr.share.entity.User;
import cn.yionr.share.exception.UserAlreadyExsitException;
import cn.yionr.share.exception.UserNotExsitException;
import cn.yionr.share.exception.WrongPasswordException;
import cn.yionr.share.service.intf.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    UserDao userDao;

    public UserServiceImpl(UserDao userDao){
        this.userDao = userDao;
    }

    @Override
    public int regedit(User user) throws UserAlreadyExsitException {

        if (userDao.queryUser(user.getEmail()) != null){
            throw new UserAlreadyExsitException("用户已存在");
        }
        return userDao.addUser(user);
    }

    @Override
    public void login(User user) throws UserNotExsitException, WrongPasswordException {
        User queryUser;
        queryUser = userDao.queryUser(user.getEmail());
        if (queryUser == null)
            throw new UserNotExsitException("用户不存在");
        else if (!queryUser.getPassword().equals(user.getPassword()))
            throw new WrongPasswordException("密码错误");
    }
}
