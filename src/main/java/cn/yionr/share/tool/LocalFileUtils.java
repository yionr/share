package cn.yionr.share.tool;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class LocalFileUtils {
    String localBaseDir;
    String localFileDir;
    String localTextDir;
    String localImageDir;
    String localTrashDir;
    File localFile;
    File localText;
    File localImage;
    File localTrash;

    @Autowired
    public LocalFileUtils(@Value("${localBase.dir}") String localBaseDir, @Value("${localFile.dir}") String localFileDir, @Value("${localText.dir}") String localTextDir, @Value("${localImage.dir}") String localImageDir, @Value("${localTrash.dir}") String localTrashDir) {
        this.localBaseDir = localBaseDir;
        this.localFileDir = localFileDir;
        this.localTextDir = localTextDir;
        this.localImageDir = localImageDir;
        this.localTrashDir = localTrashDir;

        localFile = new File(localFileDir);
        localText = new File(localTextDir);
        localImage = new File(localImageDir);
        localTrash = new File(localTrashDir);

        if (!localFile.exists())
            localFile.mkdirs();
        if (!localText.exists())
            localText.mkdirs();
        if (!localImage.exists())
            localImage.mkdirs();
        if (!localTrash.exists())
            localTrash.mkdirs();
    }


    public void releaseTrash() throws IOException {
        FileUtils.deleteDirectory(localTrash);
        localTrash.mkdirs();
    }

    public byte[] read(String code, String filetype) throws IOException {
        File dir = new File(localBaseDir, filetype);
        return FileUtils.readFileToByteArray(new File(dir, code));
    }

    public List<String> listFiles() {
        List<String> fileList = new ArrayList<>();
        fileList.addAll(Arrays.asList(localFile.list()));
        fileList.addAll(Arrays.asList(localText.list()));
        fileList.addAll(Arrays.asList(localImage.list()));
        return fileList;
    }

    public boolean exists(String code) {
        return new File(localFile, code).exists() || new File(localText, code).exists() || new File(localImage, code).exists() || inTrash(code);
    }

    public boolean inTrash(String code) {
        return new File(localTrash, code).exists();
    }

    public void save(String fid, String filetype, File file) throws IOException {
        File dir = new File(localBaseDir, filetype);
        File target = new File(dir, fid);
        if ("image".equals(filetype)) {
            String s = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file));
            FileUtils.writeStringToFile(target, s, StandardCharsets.UTF_8);
        } else
            FileUtils.copyFile(file, target);

    }

    public void moveToTrash(String code, String filetype) throws IOException {
        File dir = new File(localBaseDir, filetype);
        FileUtils.moveFileToDirectory(new File(dir, code), localTrash, false);
    }

    public void delete(String code) {
        if (new File(localFile, code).exists())
            new File(localFile, code).delete();
        else if (new File(localText, code).exists())
            new File(localText, code).delete();
        else if (new File(localImage, code).exists())
            new File(localImage, code).delete();
    }

    public File get(String code) {
        if (new File(localFile, code).exists())
            return new File(localFile, code);
        else if (new File(localText, code).exists())
            return new File(localText, code);
        else if (new File(localImage, code).exists())
            return new File(localImage, code);
        return null;
    }
}
