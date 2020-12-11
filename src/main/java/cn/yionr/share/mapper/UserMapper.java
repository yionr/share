package cn.yionr.share.mapper;

import cn.yionr.share.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {
    @Insert("insert into user values(#{uid},#{email},#{password})")
    int addUser(User user);
    @Select("select * from user where email=#{email}")
    User queryUser(String email);
}
