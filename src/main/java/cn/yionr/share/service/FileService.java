package cn.yionr.share.service;

import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.exception.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileService {

    String upload(SFileWrapper sfw,String email) throws IOException, AlogrithmException, FailedCreateFileException, FailedSaveIntoDBException, CopyFailedException;

    Object download(String code,String password,Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException, IOException;

    boolean deleteInfo(String code);

    boolean deleteFile(File file) throws FileNotFoundException;

    boolean delete(File file,String code) throws FileNotFoundException;
//    List<String> show();
}
