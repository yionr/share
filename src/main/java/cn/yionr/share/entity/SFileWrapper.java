package cn.yionr.share.entity;

import java.io.File;

public class SFileWrapper {
    private SFile sFile;
    private File file;

    public SFile getsFile() {
        return sFile;
    }

    public void setsFile(SFile sFile) {
        this.sFile = sFile;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "SFileWrapper{" +
                "sFile=" + sFile +
                ", file=" + file +
                '}';
    }

    public SFileWrapper() {
    }
}
