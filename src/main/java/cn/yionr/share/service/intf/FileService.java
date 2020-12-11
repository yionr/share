package cn.yionr.share.service.intf;

import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.exception.*;

import java.io.IOException;
import java.util.List;

public interface FileService {
    String upload(SFileWrapper sfw) throws IOException, AlogrithmException, FailedCreateFileException, FailedSaveIntoDBException, CopyFailedException;
    SFileWrapper download(String code,String password,Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException;
//    List<String> show();
}
