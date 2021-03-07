package cn.yionr.share.service;

import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.exception.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface FileService {

    String upload(SFileWrapper sfw,String email) throws IOException, AlogrithmException, FailedCreateFileException, FailedSaveIntoDBException, CopyFailedException;

    Map<String,Object> download(String code, String password, Boolean check) throws NeedPasswordException, WrongPasswordException, CodeNotFoundException, IOException, FileLostException, FileOutOfDateException, TimesRunOutException;

    boolean deleteInfo(String code);

    boolean deleteFile(String code) throws FileNotFoundException;

    boolean delete(String code) throws FileNotFoundException;
}
