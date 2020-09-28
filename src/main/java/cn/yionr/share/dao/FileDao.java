package cn.yionr.share.dao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

@Repository
public class FileDao {


    Configuration conf;
    URI uri;
    String user;

    {
        conf = new Configuration();
        conf.set("dfs.replication","3");
        try {
            uri = new URI("hdfs://47.112.169.45:9000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        user = "root";
    }

    public void download(String remotePath,String localPath) throws Exception{
        FileSystem fs=FileSystem.get(uri, conf);
        InputStream in=fs.open(new Path(remotePath));
        OutputStream out=new FileOutputStream(localPath);
        IOUtils.copyBytes(in, out, conf);
    }
    public void upload(InputStream in,String remotePath) throws Exception{
	    FileSystem fs=FileSystem.get(uri,conf,user);
	    OutputStream out=fs.create(new Path(remotePath));
	    IOUtils.copyBytes(in, out, conf);
    }
    public void delete(String remoteFile)throws Exception{
        Configuration conf=new Configuration();
        FileSystem fs=FileSystem.get(uri,conf,user);
        fs.delete(new Path(remoteFile),true);
        fs.close();
    }

    public void show(String remotePath)throws Exception{
        Configuration conf=new Configuration();
        FileSystem fs=FileSystem.get(uri,conf,user);
        FileStatus[] ls=fs.listStatus(new Path(remotePath));
        for(FileStatus status:ls){
            System.out.println(status);
        }
    }
}
