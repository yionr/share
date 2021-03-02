package cn.yionr.share.mapper;

import cn.yionr.share.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    @Insert("insert into user values(#{uid},#{email},#{password},#{created_time},#{active})")
    int addUser(User user);
    @Select("select * from user where email=#{email}")
    User queryUser(String email);
    @Update("update user set active=1 where email=#{email}")
    void active(String email);
    @Update("update user set created_time=#{created_time} where email=#{email}")
    int updateActiveTime(User user);
    @Update("update user set password=#{newPassword} where email=#{email}")
    int changePassword(String email, String newPassword);
    @Select("select active from user where email=#{email}")
    int queryActive(String email);
}
