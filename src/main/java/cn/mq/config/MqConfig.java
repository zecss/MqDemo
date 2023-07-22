package cn.mq.config;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @Param
 * @return
 */
@Slf4j
@Configuration
public class MqConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 消息是否成功发送到Exchange
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送到Exchange成功");
            } else {
                log.info("消息发送到Exchange失败");
            }
        });
        // 消息是否从Exchange路由到Queue,失败回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("消息从Exchange路由到Queue失败");
        });
        return rabbitTemplate;
    }



    // 交换机 队列名称
    public static final String MAIL_QUEUE_NAME = "mail.queue";
    public static final String MAIL_EXCHANGE_NAME = "mail.exchange";
    public static final String MAIL_ROUTING_KEY_NAME = "mail.routing.key";


    //创建一个交换机
    @Bean
    public DirectExchange exchange(){
        return ExchangeBuilder.directExchange(MAIL_EXCHANGE_NAME).build();
    }
    //创建一个队列
    @Bean
    public Queue queue(){
        return QueueBuilder.durable(MAIL_QUEUE_NAME).build();
    }
    //绑定 交换机 队列 通过routing_key
    @Bean
    public Binding fanoutBinding(){
        return BindingBuilder.bind(queue()).to(exchange()).with(MAIL_ROUTING_KEY_NAME);
    }

}
