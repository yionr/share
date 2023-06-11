package cn.yionr.share.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class User {
    private int uid;
    private String email;
    private String password;
    private long created_time;
    private boolean active;
    private boolean admin;
}
