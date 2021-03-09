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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.Map;

@Slf4j
@RestController
public class FileController {
    FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * @return xxxx: 取件码; 0: IO异常; -1:本系统暂时没有空余取件码
     */
    @PostMapping("/upload.do")
    public String upload(MultipartFile file, SFile sFile, HttpSession session, String text) throws JSONException {
        JSONObject json = new JSONObject();
        SFileWrapper sfw = new SFileWrapper();

        String email = (String) session.getAttribute("email");

        File temp;
        if (sFile.getFiletype().equals("text")) {
            try {
                temp = File.createTempFile("tempFile", "temp");
                FileWriter writer = new FileWriter(temp);
                writer.write(text);
                writer.close();
            } catch (IOException e) {
                return json.put("status", 0).toString();
            }
        } else {
            sFile.setName(file.getOriginalFilename());
            try {
                temp = File.createTempFile("tempFile", "temp");
                file.transferTo(temp);
            } catch (IOException e) {
                return json.put("status", 0).toString();
            }
        }

        sfw.setSFile(sFile);
        sfw.setFile(temp);


        try {
            json.put("status", fileService.upload(sfw, email));
        } catch (IOException e) {
            log.warn(e.getMessage());
            json.put("status", 0);
        } catch (NoLastsCodeException e) {
            log.error(e.getMessage());
            json.put("status",-1);
        }

        return json.toString();

    }


    /**
     * @return -3: 密码已修改;-2:文件容量超出上限 -1: 允许下载的次数超出上限; 1: 正常
     */
//    TODO 添加假图片的check
    @PostMapping("/checkFile")
    public String checkFile(String size, int times,String filetype, @RequestAttribute("visitor") Boolean visitor) throws JSONException {
        JSONObject json = new JSONObject();
        if (visitor == null) {
            log.warn("密码不正确，已阻止用户上传文件");
            return json.put("status", -3).toString();
        }
        log.warn("文件容量为： " + size);
        if (visitor) {
            if (times > 9) {
                log.warn("允许下载的次数超出上限");
                return json.put("status", -1).toString();
            }
            if (bigger(size, 1024 * 1024 * 100 + "") || ("image".equals(filetype) && bigger(size,1024 * 1024 * 7 + ""))) {
                log.warn("文件容量超出上限");
                return json.put("status", -2).toString();
            }
        } else {
//            注册用户，限制大于100次的上传，以及大于1G的文件上传
            if (times > 99) {
                log.warn("允许下载的次数超出上限");
                return json.put("status", -1).toString();
            }
            if (bigger(size, 1024 * 1024 * 1024 + "") || ("image".equals(filetype) && bigger(size,1024 * 1024 * 7 + ""))) {
                log.warn("文件容量超出上限");
                return json.put("status", -2).toString();
            }
        }
        return json.put("status", 1).toString();
    }

    public boolean bigger(String num1, String num2) {
        if (num1.length() > num2.length())
            return true;
        else if (num1.length() < num2.length())
            return false;
        else {
            char[] n1 = num1.toCharArray();
            char[] n2 = num2.toCharArray();
            for (int i = 0; i < n1.length; i++) {
                if (n1[i] > n2[i])
                    return true;
                else if (n1[i] < n2[i])
                    return false;
            }
//            如果相等return false;
            return false;
        }
    }

    /**
     * @return -3: IO异常; -2：客户端非法操作(改逻辑) -1:服务器文件丢了 0: 取件码不存在; 1: 取件码正常; 2: 需要密码; 3: 密码错误; 4: 文件过期;5: 文件下载次数用完了
     */
    @GetMapping("/download/{code}")
    public String download(@PathVariable("code") String code, String password, boolean check, HttpServletResponse response) throws JSONException {
        JSONObject json = new JSONObject();
        if (code.trim().matches("\\d{4}")) {
            if (check) {
                log.info("开始检查取件码");
                try {
                    Map<String, Object> fileInfo = fileService.check(code, password);
                    log.info("取件码正常");
                    for (Map.Entry<String, Object> entry : fileInfo.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                    return json.put("status", 1).toString();
                } catch (CodeNotFoundException e) {
                    log.info(e.getMessage());
                    return json.put("status", 0).toString();
                } catch (FileLostException e) {
                    log.error(e.getMessage());
                    return json.put("status", -1).toString();
                } catch (NeedPasswordException e) {
                    log.info(e.getMessage());
                    return json.put("status", 2).toString();
                } catch (WrongPasswordException e) {
                    log.info(e.getMessage());
                    return json.put("status", 3).toString();
                } catch (FileOutOfDateException e) {
                    log.info(e.getMessage());
                    return json.put("status", 4).toString();
                } catch (TimesRunOutException e) {
                    log.info(e.getMessage());
                    return json.put("status", 5).toString();
                } catch (IOException e) {
                    log.info(e.getMessage());
                    return json.put("status", -3).toString();
                }
            } else {
                log.info("准备下载文件");

                try {
                    sendFile(response, fileService.download(code, password));
                    return json.put("status", 1).toString();
                } catch (IOException e) {
                    log.error(e.getMessage());
                    return json.put("status", -3).toString();
                } catch (IllegalOperationException e) {
                    log.error(e.getMessage());
                    return json.put("status", -2).toString();
                }
            }
        } else {
            return json.put("status", 0).toString();
        }
    }

    @GetMapping("/release")
    public String release(HttpSession session){
        if ("yionr99@gmail.com".equals(session.getAttribute("email"))){
            try {
                return "释放了" + fileService.release() + "个取件码！";
            } catch (IOException e) {
                return "IO异常";
            }
        }else
            return "您没有该权限!";
    }

    //    FIXME 隐患： 要求所有其他接口不能为四位，否则都会到这里
    @GetMapping("/????")
    public String redir(HttpServletRequest request) {
        return "<meta http-equiv=\"Refresh\" content=\"0; URL=/?code=" + request.getRequestURI().substring(1) + "\" />";
    }

    public void sendFile(HttpServletResponse response, SFileWrapper sFileWrapper) throws IOException {
        response.reset();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) sFileWrapper.getFile().length());
        response.setHeader("Content-Disposition", "attachment;filename=" + sFileWrapper.getSFile().getName() + ";filename*=utf-8''" + URLEncoder.encode(sFileWrapper.getSFile().getName(), "UTF-8"));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sFileWrapper.getFile()));
        byte[] buff = new byte[1024];
        OutputStream os = response.getOutputStream();
        int i;
        while ((i = bis.read(buff)) != -1) {
            os.write(buff, 0, i);
            os.flush();
        }
        bis.close();
        sFileWrapper.getFile().delete();
    }
}
