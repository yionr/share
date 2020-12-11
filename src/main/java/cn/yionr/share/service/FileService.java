package cn.yionr.share.service;

import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.exception.*;

import java.io.IOException;

public interface FileService {

    String upload(SFileWrapper sfw) throws IOException, AlogrithmException, FailedCreateFileException, FailedSaveIntoDBException, CopyFailedException;

    SFileWrapper download(String code,String password,Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException;

//    List<String> show();
}
