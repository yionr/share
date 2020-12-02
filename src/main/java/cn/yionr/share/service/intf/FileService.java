package cn.yionr.share.service.intf;

import cn.yionr.share.entity.SFileWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public interface FileService {
    public String upload(SFileWrapper sfw) throws IOException;
    public List<String> show();

}
