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
     * @return xxxx: 取件码; 0: 临时文件创建失败; 1: 随机取件码算法出错; 2/4: 目标文件创建失败; 3: 数据库记录存储失败; 5: 文件复制失败
     */
    @PostMapping("/upload.do")
    public String upload(MultipartFile file, SFile sFile, HttpSession session, String text) throws Exception {
        JSONObject json = new JSONObject();
        String email = (String) session.getAttribute("email");

        SFileWrapper sfw = new SFileWrapper();
        File tempf;

        if (sFile.getFiletype().equals("text")) {
//            如果是text的话，没有file，也灭有name，其他几项 fid uid times password 都搞定， name由fid决定，放在service中加就行
            try {
                tempf = File.createTempFile("tempfile", "temp");
                log.info("文本为：" + text);
                FileWriter writer = new FileWriter(tempf);
                writer.write(text);
                log.info("承载文本的临时文件已创建成功");
                writer.close();
            } catch (IOException e) {
                log.error("临时文件创建失败");
                return json.put("status", 0).toString();
            }
        } else if (sFile.getFiletype().equals("image") || sFile.getFiletype().equals("file")) {
//            设置描述
            sFile.setName(file.getOriginalFilename());
//            将文件流保存到临时文件
            try {
                tempf = File.createTempFile("tempfile", "temp");
                file.transferTo(tempf);
                log.info("临时文件创建成功");
            } catch (IOException e) {
                log.warn("临时文件创建失败");
                return json.put("status", 0).toString();
            }

        } else {
            throw new Exception("前台filetype遭到篡改！");
        }
        sfw.setSFile(sFile);
        sfw.setFile(tempf);
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
     * @return -3: 密码已修改;-2:文件容量超出上限 -1: 允许下载的次数超出上限;
     */
    @PostMapping("/checkFile")
    public String checkFile(String size, int times, @RequestAttribute("visitor") Boolean visitor) throws JSONException {
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
            if (bigger(size, 1024 * 1024 * 100 + "")) {
                log.warn("文件容量超出上限");
                return json.put("status", -2).toString();
            }
        } else {
//            注册用户，限制大于100次的上传，以及大于1G的文件上传
            if (times > 99) {
                log.warn("允许下载的次数超出上限");
                return json.put("status", -1).toString();
            }
            if (bigger(size, 1024 * 1024 * 1024 + "")) {
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
     * 要做code下载文件的方式的话，得摒弃之前的下载方式，全权由这一种方式来下载
     *
     * @return -2： 用户跳过了check；-1:服务器文件丢了，而且是几乎不可能的情况下丢的 0: 取件码不存在; 1: 取件码正常; 2: 需要密码; 3: 密码错误; 4: 文件过期;5: 文件下载次数用完了
     */
    @GetMapping("/download/{code}")
    public String download(@PathVariable("code") String code, String password, boolean check, HttpServletResponse response) throws IOException, JSONException {
        JSONObject json = new JSONObject();
        if (code.trim().matches("\\d{4}")) {
            if (check) {
                log.info("开始检查取件码");
                try {
                    Map<String,Object> fileInfo =fileService.download(code, password, true);
                    log.info("取件码正常，如果是及时展示的文件类型则会直接返回数据");
                    for (Map.Entry<String, Object> entry : fileInfo.entrySet()) {
                        json.put(entry.getKey(),entry.getValue());
                    }
                    return json.put("status", 1).toString();
                } catch (CodeNotFoundException e) {
                    log.info(e.getMessage());
                    return json.put("status", 0).toString();
                } catch (FileLostException e) {
                    log.error(e.getMessage());
                    fileService.deleteInfo(code);
                    return json.put("status",-1).toString();
                } catch (NeedPasswordException e) {
                    log.info(e.getMessage());
                    return json.put("status", 2).toString();
                } catch (WrongPasswordException e) {
                    log.info(e.getMessage());
                    return json.put("status", 3).toString();
                } catch (FileOutOfDateException e) {
                    log.info(e.getMessage());
                    return json.put("status",4).toString();
                } catch (TimesRunOutException e) {
                    log.info(e.getMessage());
                    return json.put("status",5).toString();
                }
            } else {
                log.info("准备下载文件");
                try {
                    SFileWrapper sfw = (SFileWrapper)(fileService.download(code, password, false).get("content"));
                    return sendFile(response, sfw) + ""; //TODO 这里以后可以改动一下
                } catch (CodeNotFoundException | WrongPasswordException | NeedPasswordException e) {
                    log.error(e.getMessage());
                    return json.put("status", -2).toString();
                } catch (FileLostException e) {
                    log.error(e.getMessage());
                    fileService.deleteInfo(code);
                    return json.put("status",-1).toString();
                } catch (FileOutOfDateException e) {
                    log.info(e.getMessage());
                    return json.put("status",4).toString();
                } catch (TimesRunOutException e) {
                    log.info(e.getMessage());
                    return json.put("status",5).toString();
                }
            }
        } else {
            return json.put("status", 0).toString();
        }
    }

//    FIXME 隐患： 要求所有其他接口不能为四位，否则都会到这里
    @GetMapping("/????")
    public String redir(HttpServletRequest request) {
        return "<meta http-equiv=\"Refresh\" content=\"0; URL=/?code=" + request.getRequestURI().substring(1) + "\" />";
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
            bis.close();
            log.info("文件已传输完毕，即将删除临时文件");
            if (sFileWrapper.getFile().delete()) {
                log.info("临时文件删除成功");
            }
            else{
                log.warn("临时文件删除失败，文件存放在：" + sFileWrapper.getFile().getAbsolutePath());
            }
            return 1;
        } catch (IOException e) {
//            TODO 下载连接被客户端中断则会在这里打印，并抛异常
            log.info("???");
            return -1;
        }
    }
}
