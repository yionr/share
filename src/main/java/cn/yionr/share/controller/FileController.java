package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.exception.*;
import cn.yionr.share.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;

@Slf4j @RestController
public class FileController {
    FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * @return xxxx: 取件码; -2: 密码已修改; -1: 非法提升权限; 0: 临时文件创建失败; 1: 随机取件码算法出错; 2/4: 目标文件创建失败; 3: 数据库记录存储失败; 5: 文件复制失败
     */
    @PostMapping("/upload.do")
    public String upload(MultipartFile file, SFile sFile, @RequestAttribute("visitor") Boolean visitor, HttpSession session) throws JSONException {
//      TODO 根据UID设置可上传的文件容量
        JSONObject json = new JSONObject();
        String email = (String) session.getAttribute("email");
        if (visitor == null) {
            log.warn("密码不正确，已阻止用户上传文件");
            return json.put("status", -2).toString();
        }
        if (visitor || sFile.getTimes() > 99) {
            if (sFile.getTimes() > 9) {
                log.warn("用户权限不足，无法下载这么多次数，驳回");
                return json.put("status", -1).toString();
            }
        }

        sFile.setName(file.getOriginalFilename());

        SFileWrapper sfw = new SFileWrapper();
        sfw.setSFile(sFile);
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
            json.put("status", fileService.upload(sfw, email));
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
     * @return -2: 文件传输失败 -1: 文件编码错误 0: 取件码不存在; 1: 取件码正常; 2: 需要密码; 3: 密码错误
     */
    @GetMapping("/download.do")
    public String download(HttpServletResponse response, SFile sFile, boolean check) throws JSONException {
        JSONObject json = new JSONObject();
        if (check) {
            log.info("开始检查取件码");
            try {
                fileService.download(sFile.getFid(), sFile.getPassword(), true);
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
            try {
                return sendFile(response, fileService.download(sFile.getFid(), sFile.getPassword(), false)) + "";
            } catch (Exception ignored) {
            }
            return "-2";
        }
    }

    @GetMapping("/{code}")
    public void redir(@PathVariable("code")String code,HttpServletResponse response) throws IOException {
        log.info(code.trim());
        switch (code.trim()){
            case "":
                response.sendRedirect("/");
                break;
            case "/\\d{4}/" :
                response.sendRedirect("/download.do/code=" + code);
                break;
        }

    }


    public int sendFile(HttpServletResponse response, SFileWrapper sFileWrapper) {
        response.reset();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) sFileWrapper.getFile().length());
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + sFileWrapper.getSFile().getName() + ";filename*=utf-8''" + URLEncoder.encode(sFileWrapper.getSFile().getName(), "UTF-8"));
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
