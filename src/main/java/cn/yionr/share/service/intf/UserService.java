package cn.yionr.share.service.intf;

import cn.yionr.share.entity.User;

public interface UserService {

    /**
     * 0 shows error with email exsits
     * 1 shows success
     *
     * @return
     */
    int regedit(User user);

    /**
     * -1 shows error username
     * 0 shows error password
     * 1 shows success
     * @return
     */
    int login(User user);

}
