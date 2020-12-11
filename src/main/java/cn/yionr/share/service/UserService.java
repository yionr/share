package cn.yionr.share.service;

import cn.yionr.share.entity.User;
import cn.yionr.share.service.exception.UserAlreadyExsitException;
import cn.yionr.share.service.exception.UserNotExsitException;
import cn.yionr.share.service.exception.WrongPasswordException;

public interface UserService {

    int regedit(User user) throws UserAlreadyExsitException;

    void login(User user) throws UserNotExsitException, WrongPasswordException;

}
