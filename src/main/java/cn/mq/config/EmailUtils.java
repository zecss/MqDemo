package cn.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @Description: 发邮件工具类
 * @Param
 * @return
 */
@Slf4j
@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private Environment env;

    //获取配置文件的方式
    //1.@value("spring.mail.title")
    //2.@configuartionproperties("spring.mail")     批量绑定
    //3.Environment  env.getProperty("spring.mail.title")
    @Value("${spring.mail.from}")
    private String from;




    public boolean send(String to){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(env.getProperty("spring.mail.title"));
        simpleMailMessage.setText(env.getProperty("spring.mail.content"));
        try{
            javaMailSender.send(simpleMailMessage);
            log.info("发送成功");
            return true;
        }catch (Exception e){
            log.info("发送失败");
            return false;
        }
    }
}
