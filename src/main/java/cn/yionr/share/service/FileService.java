package cn.yionr.share.service;

import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.exception.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface FileService {

    String upload(SFileWrapper sfw,String email) throws IOException, NoLastsCodeException;

    Map<String, Object> check(String code, String password) throws FileOutOfDateException, TimesRunOutException, IOException, FileLostException, NeedPasswordException, WrongPasswordException, CodeNotFoundException;

    SFileWrapper download(String code, String password) throws IOException, IllegalOperationException;

    int release() throws IOException;
}
