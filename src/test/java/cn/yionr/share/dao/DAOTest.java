package cn.yionr.share.dao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class DAOTest {
    @Test
    public void show()throws Exception{
        Configuration conf=new Configuration();
        FileSystem fs=FileSystem.get(new URI("hdfs://47.112.169.45:9000"),conf,"root");
        FileStatus[] ls=fs.listStatus(new Path("/"));
        for(FileStatus status:ls){
            System.out.println(status);
        }
    }
}
