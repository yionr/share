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
    public String upload(MultipartFile file, SFile sFile, @RequestAttribute("visitor") Boolean visitor, HttpSession session,String text) throws Exception {
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


        SFileWrapper sfw = new SFileWrapper();
        File tempf;

        if (sFile.getFiletype().equals("text")){
//            如果是text的话，没有file，也灭有name，其他几项 fid uid times password 都搞定， name由fid决定，放在service中加就行
            try {
                tempf = File.createTempFile("tempfile", "temp");
                log.info("文本为：" + text);
                FileWriter writer = new FileWriter(tempf);
                writer.write(text);
                writer.close();
            } catch (IOException e) {
                log.warn("临时文件创建失败");
                return json.put("status", 0).toString();
            }
        }
        else if (sFile.getFiletype().equals("image") || sFile.getFiletype().equals("file")){
//            设置描述
            sFile.setName(file.getOriginalFilename());
//            将文件流保存到临时文件
            try {
                tempf = File.createTempFile("tempfile", "temp");
                file.transferTo(tempf);
            } catch (IOException e) {
                log.warn("临时文件创建失败");
                return json.put("status", 0).toString();
            }

        }else{
            throw new Exception("前台filetype遭到篡改！");
        }
        sfw.setSFile(sFile);
        sfw.setFile(tempf);
        log.info(sFile.toString());
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
     * 要做code下载文件的方式的话，得摒弃之前的下载方式，全权由这一种方式来下载
     *
     * @param response
     * @param
     * @return ”非法的下载请求“:非法的下载请求 0: 取件码不存在; 1: 取件码正常; 2: 需要密码; 3: 密码错误
     */
    //FIXME 目前只有不带密码的文件可以用直链的方式下载
    @GetMapping("/download/{code}")
    public String download(@PathVariable("code") String code, String password, boolean check, HttpServletResponse response) throws IOException, JSONException {
        if (code.trim().matches("\\d{4}")) {
            JSONObject json = new JSONObject();
            if (check) {
                log.info("开始检查取件码");
                try {
                    String[] fileinfo = (String[]) fileService.download(code, password, true);
                    log.info("取件码正常");
                    return json.put("status", 1).put("filetype",fileinfo[0]).put("content",fileinfo[1]).toString();
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
                    return sendFile(response, (SFileWrapper) fileService.download(code, password, false)) + "";
                }catch (Exception e) {
                    return json.put("status", "非法的下载请求！").toString();
                }
            }
        } else {
            return null;
        }
    }
    @GetMapping("/????")
    public String redir(HttpServletRequest request){
        log.info("redir!");

        return "<meta http-equiv=\"Refresh\" content=\"0; URL=/?code=" + request.getRequestURI().substring(1) +"\" />";
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
}
