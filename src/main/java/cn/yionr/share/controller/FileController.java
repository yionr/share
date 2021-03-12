package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.service.UserService;
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
import java.util.*;

@Slf4j
@RestController
public class FileController {
    FileService fileService;
    UserService userService;

    @Autowired
    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    /**
     * @return xxxx: 取件码; 0: IO异常; -1:本系统暂时没有空余取件码
     */
    @PostMapping("/upload")
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
            json.put("status", -1);
        }

        return json.toString();

    }


    /**
     * @return -3: 密码已修改;-2:文件容量超出上限 -1: 允许下载的次数超出上限; 1: 正常
     */
//    TODO 添加假图片的check
//    TODO 有了valid，其实可以重新考虑所有contorller方法的权限校验
    @PostMapping("/checkFile")
    public String checkFile(String size, int times, String filetype, @RequestAttribute("visitor") Boolean visitor) throws JSONException {
        JSONObject json = new JSONObject();
        if (visitor == null) {
            log.warn("密码不正确，已阻止用户上传文件");
            return json.put("status", -3).toString();
        }
        log.warn("文件容量为：{} bit", size);
        if (visitor) {
            if (times > 9) {
                log.warn("允许下载的次数超出上限");
                return json.put("status", -1).toString();
            }
            if (bigger(size, 1024 * 1024 * 100 + "") || ("image".equals(filetype) && bigger(size, 1024 * 1024 * 7 + ""))) {
                log.warn("文件容量超出上限");
                return json.put("status", -2).toString();
            }
        } else {
//            注册用户，限制大于100次的上传，以及大于1G的文件上传
            if (times > 99) {
                log.warn("允许下载的次数超出上限");
                return json.put("status", -1).toString();
            }
            if (bigger(size, 1024 * 1024 * 1024 + "") || ("image".equals(filetype) && bigger(size, 1024 * 1024 * 7 + ""))) {
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
    @PostMapping("/download/{code:\\d+}")
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

    /**
     * @return -1：没有clientId，异常; 0:没内容 ; 1: 正常
     */
    @PostMapping("/listFiles")
    public String listFiles(String clientId, HttpSession session) throws JSONException {
        JSONObject json = new JSONObject();
        if (clientId == null || "".equals(clientId.trim()))
            return json.put("status", -1).toString();
        JSONObject files;
        List<SFile> sFiles;
        if (valid(session))
            sFiles = fileService.listFiles(clientId, (String) session.getAttribute("email"));
        else
            sFiles = fileService.listFiles(clientId, null);

        if (sFiles.size() == 0)
            return json.put("status", 0).toString();
        else
            json.put("status", 1);
        List<String> fileList = new ArrayList<>();
        for (SFile sFile : sFiles) {
            files = new JSONObject();
            files.put("fid", sFile.getFid());
            files.put("name", sFile.getName());
            files.put("password", sFile.getPassword());
            files.put("times", sFile.getTimes());
            files.put("uid", sFile.getUid());
            files.put("filetype", sFile.getFiletype());
            files.put("leftTime", new Date().getTime() - sFile.getUploaded_time());
            fileList.add(files.toString());
        }
        json.put("files", Arrays.toString(fileList.toArray()));
        return json.toString();
    }

    /**
     * @return -1:文件不属于你； 1：删除成功； 0： 删除失败; 2: 文件不存在
     */
    @PostMapping("/deleteFile")
    public String deleteFile(String fid, String clientId, HttpSession session) throws JSONException {
        log.info("客户端请求删除文件");
        JSONObject json = new JSONObject();
        boolean belong;
        if (!fileService.exists(fid))
            return json.put("status",2).toString();
        if (!valid(session))
            belong = fileService.checkBelong(fid, clientId);
        else
            belong = fileService.checkBelong(fid, clientId, (String) session.getAttribute("email"));
        if (belong) {
            try {
                fileService.delete(fid);
                log.info("删除成功");
                json.put("status",1);
            } catch (IOException e) {
                log.warn("删除失败");
                json.put("status",0);
            }
        } else{
            json.put("status", -1);
            log.warn("该文件不属于该用户");
        }
        return json.toString();
    }

    @GetMapping("/release")
    public String release(HttpSession session) {
        if (valid(session)) {
            String email = (String) session.getAttribute("email");
            if (userService.isAdmin(email)) {
                try {
                    return "释放了" + fileService.release() + "个取件码！";
                } catch (IOException e) {
                    return "IO异常";
                }
            }
            return "您没有该权限!";
        }
        return "无效session";
    }

    public boolean valid(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || "".equals(email.trim()))
            return false;
        else {
            String password = (String) session.getAttribute("password");
            return userService.checkPassword(email, password);
        }
    }

    @GetMapping("/{code:\\d+}")
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
