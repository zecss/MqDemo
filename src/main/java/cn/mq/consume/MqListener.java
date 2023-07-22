package cn.mq.consume;

import cn.mq.config.EmailUtils;
import cn.mq.config.MqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: 邮件消费
 * @Param
 * @return
 */
@Component
public class MqListener {

    @Autowired
    private EmailUtils emailUtils;

    //轮询分发
    // @RabbitListener(queues = "mq_email")
    // public void consume(String m) {
    //     emailUtils.send(m);
    // }

    //发布订阅
    @RabbitListener(queues = MqConfig.MAIL_QUEUE_NAME)
    public void consume1(Message message, Channel channel){
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //消费成功，手动确认
        try {
            emailUtils.send(new String(message.getBody(), "UTF-8"));
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
