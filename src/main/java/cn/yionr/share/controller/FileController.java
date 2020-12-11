package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.exception.NeedPasswordException;
import cn.yionr.share.service.intf.FileService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * @return 0: 非法提升权限; -1: 保存文件出错; xxxx: 保存成功，得到取件码
     */
    @PostMapping("/upload")
    public String upload(MultipartFile file, String password, int times, String email) throws IOException, JSONException {
        log.info("上传文件的附加信息： 次数: " + times + "; 密码: " + password + "; 邮箱: " + email + ";");
        JSONObject json = new JSONObject();
        if (email.trim().equals("") || times > 99) {
            if (times > 9) {
                log.info(email + " , " + times);
//            未登录的情况下篡改下载次数
                return json.put("result", 0).toString();
            }
        }
        SFile sf = new SFile();
        sf.setName(file.getOriginalFilename());
        sf.setPassword(password);
        sf.setTimes(times);
        sf.setUid(-1);

        SFileWrapper sfw = new SFileWrapper();
        sfw.setsFile(sf);
        File tempf = File.createTempFile("tempfile", "temp");
        file.transferTo(tempf);
        sfw.setFile(tempf);

        return json.put("result",fileService.upload(sfw)).toString();

    }

    /**
     * @return -1: error; 0: code invalid; 1: success; 2: need password; 3: password incorrect
     */
    @GetMapping("/download")
    public String download(HttpServletResponse response, String code, String password, boolean check) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sFileWrapper;

        if (check) {
            if (password == null) {
                try {
                    sFileWrapper = fileService.download(code);
                } catch (NeedPasswordException e) {
                    return json.put("result", 2).toString();
                }
                if (sFileWrapper == null) {
                    return json.put("result", 0).toString();
                }
                if (!sFileWrapper.getFile().exists()) {
//            这里应该是过期了
                    return json.put("result", 0).toString();
                }
                return json.put("result", 1).toString();
            } else {
                sFileWrapper = fileService.download(code, password);
                if (sFileWrapper == null) {
//            密码错误
                    return json.put("result", 3).toString();
                }
//        进入这的说明已经访问过一次了，不需要验证code了，直接验证密码是否正确即可。
                return json.put("result", 1).toString();
            }
        } else {
            log.info("password: " + password);
            if (password == null) {
                try {
                    sFileWrapper = fileService.download(code);
                } catch (NeedPasswordException e) {
                    return json.put("result", 2).toString();
                }
                if (sFileWrapper == null) {
                    return json.put("result", 0).toString();
                }
                if (!sFileWrapper.getFile().exists()) {
//            这里应该是过期了
                    return json.put("result", 0).toString();
                }
                return json.put("result", sendFile(response, sFileWrapper)).toString();
            } else {
                sFileWrapper = fileService.download(code, password);
                if (sFileWrapper == null) {
//            密码错误
                    return json.put("result", 3).toString();
                }
//        进入这的说明已经访问过一次了，不需要验证code了，直接验证密码是否正确即可。
                return json.put("result", sendFile(response, sFileWrapper)).toString();
            }
        }

    }

    @GetMapping("/show")
    public String show() {
        return fileService.show().toString();
    }

    public int sendFile(HttpServletResponse response, SFileWrapper sFileWrapper) {
        response.reset();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) sFileWrapper.getFile().length());
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + sFileWrapper.getsFile().getName() + ";filename*=utf-8''" + URLEncoder.encode(sFileWrapper.getsFile().getName(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return -1;
        }

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sFileWrapper.getFile()));
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
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
