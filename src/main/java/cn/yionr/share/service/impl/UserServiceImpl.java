package cn.yionr.share.service.impl;

import cn.yionr.share.entity.MailVo;
import cn.yionr.share.mapper.UserMapper;
import cn.yionr.share.entity.User;
import cn.yionr.share.service.exception.*;
import cn.yionr.share.service.UserService;
import cn.yionr.share.tool.MailTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j @Service
public class UserServiceImpl implements UserService {
    UserMapper userMapper;
    MailTool mailTool;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, MailTool mailTool) {
        this.userMapper = userMapper;
        this.mailTool = mailTool;
    }

    @Override
    public int regedit(User user) throws UserAlreadyExsitException, UserNotActiveException {

        user.setActive(false);
        user.setCreated_time(new Date().getTime());

        User userInDB = userMapper.queryUser(user.getEmail());
        if (userInDB != null) {
            if (userInDB.isActive()) {
                throw new UserAlreadyExsitException("用户已存在");
            } else {
                if (Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(userInDB.getCreated_time()), ZoneId.systemDefault()), LocalDateTime.now()).toDays() >= 2) {
//                    刷新数据库中该用户的创建时间，重新给用户发送激活信息，要求用户重新注册。
                    sendMail(user);
                    return userMapper.updateActiveTime(user);
                } else {
//                    提醒用户去邮箱内激活
                    throw new UserNotActiveException("用户任在激活期间内，但还未激活");
                }
            }
        }
        else{
//        用户注册邮箱，有效性验证要在两天内完成，为此，数据库要添加几个字段 注册时间； 是否已经注册：已注册-待激活 两个状态；
//        如果有新的注册请求，去查询用户的时候如果查到了但是发现是待激活状态的，判断日期是否超过两天，
//        如果两天内则提醒用户去邮箱查看；
//        如果超过两天则删除记录，并重新开始注册流程（递归）
            sendMail(user);
            return userMapper.addUser(user);

        }
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

    /**
     * @param uuid 激活码为创建日期进行加密的结果
     */
    @Override
    public User active(String email, String uuid) throws ActiveLinkOutOfDateException, UserWaitToActiveNotFoundException, UUIDInvalidException, UserActivedException {

        User user = userMapper.queryUser(email);
        if (user != null) {
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getCreated_time()), ZoneId.systemDefault());
            if (Duration.between(time, LocalDateTime.now()).toDays() >= 2) {
                throw new ActiveLinkOutOfDateException("激活链接失效了");
            } else {
//                根据time计算uuid是否和传来的uuid一致，不一致的话报错
                if (!user.isActive()) {
                    if (DigestUtils.md5DigestAsHex((user.getCreated_time() + "").getBytes(StandardCharsets.UTF_8)).equals(uuid)) {
                        log.info("激活成功");
                        userMapper.active(email);
                        return user;
                    } else {
                        throw new UUIDInvalidException("该激活码无效");
                    }
                } else {
                    throw new UserActivedException("用户已注册");
                }
            }
        } else {
            throw new UserWaitToActiveNotFoundException("没有此待激活用户");
        }
    }

    public void sendMail(User user) {
        log.info("生成激活码为: " + DigestUtils.md5DigestAsHex((user.getCreated_time() + "").getBytes(StandardCharsets.UTF_8)));
        new Thread(() -> mailTool.sendMail(MailVo.builder().to(user.getEmail()).subject("账号激活").email(user.getEmail()).uuid(DigestUtils.md5DigestAsHex((user.getCreated_time() + "").getBytes(StandardCharsets.UTF_8))).build())).start();
    }
}
