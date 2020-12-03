package cn.yionr.share.service.intf;

import cn.yionr.share.entity.SFileWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public interface FileService {
    String upload(SFileWrapper sfw) throws IOException;
    SFileWrapper download(String code);
    List<String> show();
}
