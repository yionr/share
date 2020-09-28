package cn.yionr.share.entity;

public class SFile {
    private int fid;
    private String name;
    private String password;
    private int times;
    private int uid;

    @Override
    public String toString() {
        return "SFile{" +
                "fid=" + fid +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", times=" + times +
                ", uid=" + uid +
                '}';
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public SFile() {
    }
}
