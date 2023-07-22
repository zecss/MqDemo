## Rabbitmq发送邮件

### 一、练手介绍

注册成功后通过消息队列发送邮箱提醒

一个简简单单的Demo

1. EmailUtils         邮件工具类
2. MqConfig         Mq配置类
3. MqListener      消费消息
4. UserService     生产消息

### 二、知识理论

#### 1. RabbitMq的基本操作

#### 2. 消息发送确认机制

##### 2.1 事务机制

##### 2.2 Confirm模式

#### 3. 消息消费确认机制

##### 3.1 手动确认manual

需先置为手动确认：手动确认又分为**肯定确认**和**否定确认**。

```java
basicAck()
basicNack()
```

##### 3.2自动确认auto

当消费者收到消息后，消息就会被 RabbitMQ 从队列中删除掉。这种模式认为 “发送即成功”。

#### 4. SpringBoot读取配置的方式

```xml
1. 使用 @Value 读取配置文件
2. 使用 @ConfigurationProperties 读取配置文件
3. 使用 Environment 读取配置文件
```

###  三、代码实现

#### 1. 数据库

```sql
CREATE TABLE USER(
	id INT PRIMARY KEY AUTO_INCREMENT,
	email VARCHAR(64),
	PASSWORD VARCHAR(64)
)
```

#### 1. pom.xml

```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--mybatis-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.0.0</version>
        </dependency>
        <!--mysql connector-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!--lombok-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.16.10</version>
        </dependency>
        <!--mq-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <!--mail-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
    </dependencies>
```

#### 2. application.yml

```yml
server:
  port: 8080
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mqdemo?serverTimezone=GMT%2B8&autoR&useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
# rabbitmq
  rabbitmq:
    host: 192.168.235.50
    port: 5672
    virtual-host: study
    username: admin
    password: 123456
    # 开启confirms回调 P -> Exchange
    publisher-confirms: true
    # 开启returnedMessage回调 Exchange -> Queue
    publisher-returns: true
    # 设置手动确认(ack) Queue -> C
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 100
  mail:
    host: smtp.163.com
    username: xxxxxxxxxxxxxxxx
    password: xxxxxxxxxxxxxxxx
    from: xxxxxxxxxxxxx
    title: "新消息"
    content: "成功注册"
```

#### 3. 生产消息

```java
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
```

#### 4. 消费消息

```java
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
```

#### 5. 配置

```java
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
```

