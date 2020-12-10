package cn.yionr.share.service.impl;

import cn.yionr.share.dao.UserDao;
import cn.yionr.share.entity.User;
import cn.yionr.share.service.intf.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    UserDao userDao;

    public UserServiceImpl(UserDao userDao){
        this.userDao = userDao;
    }

    @Override
    public int regedit(User user) {

        if (userDao.queryUser(user.getEmail()) != null){
//            该邮箱已注册
            return 0;
        }
        return userDao.addUser(user);
    }

    @Override
    public int login(User user) {
        User queryUser;
        queryUser = userDao.queryUser(user.getEmail());
        if (queryUser == null)
            return -1;
        else if (!queryUser.getPassword().equals(user.getPassword()))
            return 0;
        else
            return 1;
    }
}
