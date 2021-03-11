package cn.yionr.share.tool;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class HadoopUtils {
    public static boolean CLEARED = false;

    Configuration configuration;
    FileSystem fileSystem;
    Path hadoopFilePath;
    Path hadoopTrashPath;
    String tempFileDir;

    @Autowired
    public HadoopUtils(@Value("${hadoopFilePath}") String hadoopFilePath, @Value("${hadoopTrashPath}") String hadoopTrashPath,@Value("${tempFile.dir}") String tempFileDir) throws IOException {

        configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://127.0.0.1:9000");
        configuration.set("dfs.client.use.datanode.hostname", "true");
        configuration.set("dfs.datanode.use.datanode.hostname", "true");
        // HDFS文件系统的操作对象
        fileSystem = FileSystem.get(configuration);

        this.hadoopFilePath = new Path(hadoopFilePath);
        this.hadoopTrashPath = new Path(hadoopTrashPath);
        this.tempFileDir = tempFileDir;
        File dir = new File(tempFileDir);
        if (!dir.exists())
            dir.mkdirs();

        if (!fileSystem.exists(this.hadoopFilePath))
            fileSystem.mkdirs(this.hadoopFilePath);
    }

    public void save(File file) throws IOException {
        fileSystem.copyFromLocalFile(false, new Path(file.getAbsolutePath()), hadoopFilePath);
    }

    private void save(File file, Path path) throws IOException {
        fileSystem.copyFromLocalFile(false, new Path(file.getAbsolutePath()), path);
    }

    public void delete(String code) throws IOException {
        fileSystem.delete(new Path(hadoopFilePath, code), true);
    }

    public void releaseTrash() throws IOException {
        fileSystem.delete(hadoopTrashPath, true);
        fileSystem.mkdirs(hadoopTrashPath);
        CLEARED = true;
    }

    public List<String> listFiles() throws IOException {
        List<String> fileLists = new ArrayList<>();
        RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator = fileSystem.listFiles(hadoopFilePath, false);
        while (locatedFileStatusRemoteIterator.hasNext()) {
            LocatedFileStatus next = locatedFileStatusRemoteIterator.next();
            fileLists.add(next.getPath().getName());
        }
        return fileLists;
    }

    public boolean exists(String code) throws IOException {
        return fileSystem.exists(new Path(hadoopFilePath, code)) || fileSystem.exists(new Path(hadoopTrashPath, code));
    }
    public boolean inTrash(String code) throws IOException {
        return fileSystem.exists(new Path(hadoopTrashPath, code));
    }

    public File get(String code) throws IOException {
        File file = new File(tempFileDir,code);
        file.createNewFile();
        fileSystem.copyToLocalFile(new Path(hadoopFilePath, code), new Path(file.getAbsolutePath()));
        return file;
    }

    public void moveToTrash(String code) throws IOException {
        File file = get(code);
        save(file, hadoopTrashPath);
        file.delete();
        delete(code);
    }


}
