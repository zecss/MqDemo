package cn.mq.service.Impl;

import cn.mq.config.EmailUtils;
import cn.mq.config.MqConfig;
import cn.mq.domain.User;
import cn.mq.mapper.UserMapper;
import cn.mq.service.UserService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * @Description: TODO
 * @Param
 * @return
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public String reg(User user) {
        int count = userMapper.reg(user);
        if (count>0){
            //同步发送邮件
            // emailUtils.send(user.getEmail());
            //异步发送邮件(轮询分发 点对点)
            // rabbitTemplate.convertAndSend("mq_email",user.getEmail());
            //异步发送邮件(发布订阅 交换机)
            rabbitTemplate.convertAndSend(MqConfig.MAIL_EXCHANGE_NAME,MqConfig.MAIL_ROUTING_KEY_NAME,user.getEmail());
            return "注册成功";
        }else {
            return "注册失败";
        }

    }
}
