package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.exception.*;
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

import javax.servlet.http.HttpServletRequest;
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
     * @return xxxx: 保存成功，得到取件码
     * status 状态码: -2: 密码已修改； -1: 非法提升权限; 0: 临时文件创建失败; 1: 随机取件码算法出错; 2/4: 目标文件创建失败; 3: 数据库记录存储失败; 5: 文件复制失败
     */
    @PostMapping("/upload.do")
    public String upload(MultipartFile file, String password, int times, HttpServletRequest request) throws JSONException {
        JSONObject json = new JSONObject();
        if (request.getAttribute("visitor") == null) {
            log.warn("密码不正确，已阻止用户上传文件");
            return json.put("status", -2).toString();
        }
        Boolean visitor = (Boolean) request.getAttribute("visitor");
        if (!visitor || times > 99) {
            if (times > 9) {
                log.warn("用户权限不足，无法下载这么多次数，驳回");
                return json.put("status", -1).toString();
            }
        }

        SFile sf = new SFile();
        sf.setName(file.getOriginalFilename());
        sf.setPassword(password);
        sf.setTimes(times);
        sf.setUid(-1);

        SFileWrapper sfw = new SFileWrapper();
        sfw.setsFile(sf);
        File tempf;
        try {
            tempf = File.createTempFile("tempfile", "temp");
            file.transferTo(tempf);
            sfw.setFile(tempf);
        } catch (IOException e) {
            log.warn("临时文件创建失败");
            return json.put("status", 0).toString();
        }

        try {
            json.put("status", fileService.upload(sfw));
        } catch (AlogrithmException e) {
            json.put("status", 1);
        } catch (FailedCreateFileException e) {
            json.put("status", 2);
        } catch (FailedSaveIntoDBException e) {
            json.put("status", 3);
        } catch (IOException e) {
            json.put("status", 4);
        } catch (CopyFailedException e) {
            json.put("status", 5);
        }

        return json.toString();

    }

    /**
     * @return -1: error; 0: code invalid; 1: success; 2: need password; 3: password incorrect
     */
    @GetMapping("/download.do")
    public String download(HttpServletResponse response, String code, String password, boolean check) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sFileWrapper;

        if (check) {
            log.info("开始检查取件码");
            try {
                fileService.download(code, password, true);
                log.info("取件码正常");
                return json.put("status", 1).toString();
            } catch (NeedPasswordException e) {
                log.info("该取件码需要密码");
                return json.put("status", 2).toString();
            } catch (WrongPasswordException e) {
                log.info("取件码密码错误");
                return json.put("status", 3).toString();
            } catch (CodeNotFoundException e) {
                log.info("取件码不存在");
                return json.put("status", 0).toString();
            }
        } else {
            log.info("准备下载文件");
            try {sendFile(response, fileService.download(code, password, false));} catch (Exception ignored) {}
            return "";
        }
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

//        @GetMapping("/show.do")
//        public String show() {
//            return fileService.show().toString();
//        }
}
