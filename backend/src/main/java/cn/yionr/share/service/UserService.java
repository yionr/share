package cn.yionr.share.service;

import cn.yionr.share.entity.User;
import cn.yionr.share.service.exception.*;

public interface UserService {

    int regedit(User user) throws UserAlreadyExsitException, UserNotActiveException;

    void login(User user) throws UserNotExsitException, WrongPasswordException, UserNotActiveException;

    User active(String email, String uuid) throws ActiveLinkOutOfDateException, UserWaitToActiveNotFoundException, UUIDInvalidException, UserActivedException;

    int changePassword(String email, String newPassword);

    boolean checkEmail(String email);

    boolean checkPassword(String email,String password);

    boolean isAdmin(String email);
}
