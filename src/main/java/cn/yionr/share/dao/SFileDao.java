package cn.yionr.share.dao;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SFileDao {
    @Insert("insert into sfile values(#{fid},#{name},#{password},#{times},#{uid})")
    boolean addSFile(SFile sf);

    @Select("select fid from sfile")
    List<String> listCodes();

    @Select("select name from sfile where fid=#{code}")
    String queryFile(String code);

    //TODO SQL语句有点问题，要将time-1 更新后的time返回
    @Update("update sfile set times = times - 1 where fid=#{code}")
    void decreaseTime(String code);

    @Select("select times from sfile where fid=#{code}")
    int queryTimes(String code);

    @Delete("delete from sfile where fid=#{code}")
    boolean delect(String code);
}
