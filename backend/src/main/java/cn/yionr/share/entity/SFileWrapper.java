package cn.yionr.share.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class SFileWrapper {
    private SFile sFile;
    private File file;
}
