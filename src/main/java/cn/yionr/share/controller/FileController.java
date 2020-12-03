package cn.yionr.share.controller;

import cn.yionr.share.entity.SFile;
import cn.yionr.share.entity.SFileWrapper;
import cn.yionr.share.entity.User;
import cn.yionr.share.service.impl.FileServiceImpl;
import cn.yionr.share.service.intf.FileService;
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
        File tempf = new File("tempfile");
        if(!tempf.exists())
            tempf.createNewFile();
        System.out.println(tempf.getAbsoluteFile());
        file.transferTo(tempf.getAbsoluteFile());
        System.out.println(tempf.length());
        sfw.setFile(tempf);
        return fileService.upload(sfw);

    }

//FIXME bug:次数一次-2
    @GetMapping("/download")
    public String download(HttpServletResponse response, String code) throws UnsupportedEncodingException {
        SFileWrapper sFileWrapper = fileService.download(code);
        if(!sFileWrapper.getFile().exists()){
            return "已过期(服务器中保存的文件丢失了！怎么办！！！)";
        }
        response.reset();
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) sFileWrapper.getFile().length());
        response.setHeader("Content-Disposition", "attachment;filename=" + sFileWrapper.getsFile().getName()+";filename*=utf-8''"+ URLEncoder.encode(sFileWrapper.getsFile().getName(),"UTF-8") );

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sFileWrapper.getFile()));) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            return "下载失败";
        }
        return "下载成功";
    }

    @GetMapping("/show")
    public String show(){
        return fileService.show().toString();
    }
}
