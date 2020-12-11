package cn.yionr.share.dao;

import cn.yionr.share.entity.SFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SFileDao {
    @Insert("insert into sfile values(#{fid},#{name},#{password},#{times},#{uid})")
    int addSFile(SFile sf);

    @Select("select fid from sfile")
    List<String> listCodes();

    @Select("select name from sfile where fid=#{code}")
    String queryFile(String code);

    @Update("update sfile set times = times - 1 where fid=#{code}")
    void decreaseTime(String code);

    @Select("select times from sfile where fid=#{code}")
    int queryTimes(String code);

    @Select("select password from sfile where fid=#{code}")
    String queryPassword(String code);

    @Delete("delete from sfile where fid=#{code}")
    boolean delect(String code);
}
