package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.entity.User;
import cn.yionr.share.exception.NeedPasswordException;
import cn.yionr.share.service.impl.FileServiceImpl;
import cn.yionr.share.service.intf.FileService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

@RestController
public class FileController {

    @Autowired
    FileService fileService;

    /**
     *
     * @param file
     * @param password
     * @param times
     * @return 0 shows error ;
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(MultipartFile file, String password, int times) throws IOException {
        SFile sf = new SFile();
        sf.setName(file.getOriginalFilename());
        sf.setPassword(password);
        sf.setTimes(times);
        sf.setUid(-1);

        SFileWrapper sfw = new SFileWrapper();
        sfw.setsFile(sf);
//        TODO 不要上传到这个临时文件中，直接传到目标文件中。
        File tempf = File.createTempFile("tempfile","temp");
        file.transferTo(tempf);
        sfw.setFile(tempf);
        return fileService.upload(sfw);

    }

    /**
     *
     * @param response
     * @param code
     * @return -1: error; 0: code invalid; 1: success; 2: need password;
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/download")
    public String download(HttpServletResponse response, String code) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sFileWrapper;
        try {
            sFileWrapper = fileService.download(code);
        } catch (NeedPasswordException e) {
            return json.put("result",2).toString();
        }
        if (sFileWrapper == null){
            return json.put("result",0).toString();
        }
        if(!sFileWrapper.getFile().exists()){
//            这里应该是过期了
            return json.put("result",0).toString();
        }
        return json.put("result",sendFile(response,sFileWrapper)).toString();
    }
    @GetMapping("/check")
    public String check(HttpServletResponse response, String code) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sFileWrapper;
        try {
            sFileWrapper = fileService.download(code);
        } catch (NeedPasswordException e) {
            return json.put("result",2).toString();
        }
        if (sFileWrapper == null){
            return json.put("result",0).toString();
        }
        if(!sFileWrapper.getFile().exists()){
//            这里应该是过期了
            return json.put("result",0).toString();
        }
        return json.put("result",1).toString();
    }

    /**
     *
     * @param response
     * @param code
     * @param password
     * @return   3: password incorrect
     */
    @PostMapping("/download")
    public String download(HttpServletResponse response, String code,String password) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sFileWrapper = fileService.download(code,password);
        if (sFileWrapper == null){
//            密码错误
            return json.put("result",3).toString();
        }
//        进入这的说明已经访问过一次了，不需要验证code了，直接验证密码是否正确即可。
        return json.put("result",sendFile(response,sFileWrapper)).toString();
    }
    @PostMapping("/check")
    public String check(HttpServletResponse response, String code,String password) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sFileWrapper = fileService.download(code,password);
        if (sFileWrapper == null){
//            密码错误
            return json.put("result",3).toString();
        }
//        进入这的说明已经访问过一次了，不需要验证code了，直接验证密码是否正确即可。
        return json.put("result",1).toString();
    }

    @GetMapping("/show")
    public String show(){
        return fileService.show().toString();
    }

    public int sendFile(HttpServletResponse response,SFileWrapper sFileWrapper){
        response.reset();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) sFileWrapper.getFile().length());
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + sFileWrapper.getsFile().getName()+";filename*=utf-8''"+ URLEncoder.encode(sFileWrapper.getsFile().getName(),"UTF-8") );
        } catch (UnsupportedEncodingException e) {
            return -1;
        }

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sFileWrapper.getFile()));
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
            return 1;
        } catch (IOException e) {
            return -1;
        }
    }
}
