package cn.yionr.share.mapper;

import cn.yionr.share.entity.SFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SFileMapper {
    @Insert("insert into sfile values(#{fid},#{name},#{password},#{times},#{uid},#{filetype},#{uploaded_time})")
    int addSFile(SFile sf);

    @Select("select fid from sfile")
    List<String> listCodes();

    @Select("select * from sfile where fid=#{code}")
    SFile queryFile(String code);

    @Select("select name from sfile where fid=#{code}")
    String queryFileName(String code);

    @Update("update sfile set times = times - 1 where fid=#{code}")
    void decreaseTime(String code);

    @Select("select times from sfile where fid=#{code}")
    int queryTimes(String code);

    @Select("select uploaded_time from sfile where fid=#{code}")
    long queryUploaded_time(String code);

    @Select("select password from sfile where fid=#{code}")
    String queryPassword(String code);

    @Delete("delete from sfile where fid=#{code}")
    int delete(String code);

    @Select("select filetype from sfile where fid=#{code}")
    String queryFiletype(String code);

    @Select("select uid from sfile where fid=#{code}")
    int queryUID(String code);
}
