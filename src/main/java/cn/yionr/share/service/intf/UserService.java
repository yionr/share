package cn.yionr.share.service.intf;

import cn.yionr.share.entity.User;
import cn.yionr.share.exception.UserAlreadyExsitException;
import cn.yionr.share.exception.UserNotExsitException;
import cn.yionr.share.exception.WrongPasswordException;

public interface UserService {

    int regedit(User user) throws UserAlreadyExsitException;

    void login(User user) throws UserNotExsitException, WrongPasswordException;

}
