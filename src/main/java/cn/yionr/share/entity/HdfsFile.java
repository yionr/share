package cn.yionr.share.entity;

public class HdfsFile {
    private String fileName;
    private int fileSize;

    public HdfsFile() {
    }

    @Override
    public String toString() {
        return "HdfsFile{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
