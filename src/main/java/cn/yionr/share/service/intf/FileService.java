package cn.yionr.share.service.intf;

import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.exception.NeedPasswordException;

import java.io.IOException;
import java.util.List;

public interface FileService {
    String upload(SFileWrapper sfw) throws IOException;
    SFileWrapper download(String code) throws NeedPasswordException;
    SFileWrapper download(String code,String password);
    List<String> show();
}
