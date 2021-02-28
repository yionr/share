package cn.yionr.share.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class SFile {
    private String fid;
    private String name;
    private String password;
    private int times;
    private String filetype;
    private int uid;
}
