package cn.yionr.share.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

    private TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(String email,String uuid) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("uuid",uuid);
        return templateEngine.process("emailTemplate", context);
    }

}