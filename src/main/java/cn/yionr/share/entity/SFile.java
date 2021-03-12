package cn.yionr.share.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class SFile {
    private String fid;
    private String name;
    private String password;
    private int times;
    private String filetype;
    private int uid;
    private long uploaded_time;
    private String clientId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SFile sFile = (SFile) o;
        return fid.equals(sFile.fid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fid);
    }
}
