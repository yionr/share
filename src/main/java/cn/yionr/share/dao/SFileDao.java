package cn.yionr.share.dao;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface SFileDao {
    @Insert("insert into sfile values(#{fid},#{name},#{password},#{time},,#{uid})")
    boolean addSFile(SFile sf);
}
