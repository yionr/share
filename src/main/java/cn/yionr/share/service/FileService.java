package cn.yionr.share.service;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.exception.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService {

    String upload(SFileWrapper sfw,String email) throws IOException, NoLastsCodeException;

    Map<String, Object> check(String code, String password) throws FileOutOfDateException, TimesRunOutException, IOException, FileLostException, NeedPasswordException, WrongPasswordException, CodeNotFoundException;

    SFileWrapper download(String code, String password) throws IOException, IllegalOperationException;

    int release() throws IOException;

    List<SFile> listFiles(String clientId,String email);

    boolean checkBelong(String fid, String clientId);

    boolean checkBelong(String fid, String clientId,String email);

    void delete(String fid) throws IOException;

    boolean exists(String fid);
}
