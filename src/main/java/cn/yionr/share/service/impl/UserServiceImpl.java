package cn.yionr.share.service.impl;

import cn.yionr.share.mapper.UserMapper;
import cn.yionr.share.entity.User;
import cn.yionr.share.service.exception.UserAlreadyExsitException;
import cn.yionr.share.service.exception.UserNotExsitException;
import cn.yionr.share.service.exception.WrongPasswordException;
import cn.yionr.share.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper){
        this.userMapper = userMapper;
    }

    @Override
    public int regedit(User user) throws UserAlreadyExsitException {

        if (userMapper.queryUser(user.getEmail()) != null){
            throw new UserAlreadyExsitException("用户已存在");
        }
        return userMapper.addUser(user);
    }

    @Override
    public void login(User user) throws UserNotExsitException, WrongPasswordException {
        User queryUser;
        queryUser = userMapper.queryUser(user.getEmail());
        if (queryUser == null)
            throw new UserNotExsitException("用户不存在");
        else if (!queryUser.getPassword().equals(user.getPassword()))
            throw new WrongPasswordException("密码错误");
    }
}
