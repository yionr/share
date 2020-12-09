package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.exception.NeedPasswordException;
import cn.yionr.share.service.intf.FileService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@RestController
public class FileController {

    FileService fileService;

    @Autowired
    public FileController(FileService fileService){
        this.fileService = fileService;
    }
    /**
     *
     * @param file 用户上传的文件
     * @param password 用户设置的二级密码
     * @param times 该文件可供下载的次数
     * @return 0 shows error ;
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
        File tempf = File.createTempFile("tempfile","temp");
        file.transferTo(tempf);
        sfw.setFile(tempf);
        return fileService.upload(sfw);

    }

    /**
     *
     * @param response 通过response获得输出流给用户传输文件
     * @param code 取件码
     * @return -1: error; 0: code invalid; 1: success; 2: need password;
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
    public String check(String code) throws JSONException {
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
     * @param response 通过response获得输出流给用户传输文件
     * @param code 取件码
     * @param password 二级密码
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
    public String check(String code,String password) throws JSONException {
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
            int i;
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
