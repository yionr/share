package cn.yionr.share.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WebResult {
    private String status; //状态码
    private String msg; //提示信息
}