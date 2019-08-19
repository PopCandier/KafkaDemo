### Kafka的安装

官网 kafka.apache.org

启动前，修改kafka的配置文件，也就是修改server.properties，位于config文件夹下。将zk的连接调整到已经启动zk服务的端口状态。

启动的时候，请确保zk已经启动，然后使用命令

```
sh kafka-server-start.sh ../config/server.properties
```

不过出现的最常见的就是超时操作，这个时间我们需要把超时的时间改长一点就好。

```
zookeeper.connection.timeout.ms=6000000
```

不过需要注意的是，如果你是在本机启动的zk与本地启动的kafka的情况下，以上配置已经可以满足你需求，但是如果，zk与kafka部署在不同的机器上，你就需要配置额外的参数

```properties
# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
#listeners=PLAINTEXT://:9092  <--此为本机上启动的配置，如果你是不同节点的zk那么就需要另外配置
listeners=PLAINTEXT://192.168.0.102:9092
# 9092 就是kafka默认端口号。
```

从zooView上也可以看到这个的变化

![1566204197362](./img/1566204197362.png)

那如果我们先命令行去创建一个消息的话，可以输入以下指令。

```
[root@localhost bin]# sh kafka-topics.sh  --create --zookeeper 192.168.0.102:2182 --replication-factor 1 --partitions 1 --topic test
```

前面是创建zk连接，后面是副本的一些信息，然后topic是组的名字，kafka中topic意味组别，也就是表示分类的意思。

![1566204922108](./img/1566204922108.png)

但实际业务中，应该是具体的类别，例如体育或者新闻之类的信息来搞定。

然后我们要消费这个信息，也就是意味着对这个节点进行监听。

```
[root@localhost bin]# sh kafka-console-consumer.sh --bootstrap-server 192.168.0.102:9092 --topic test --from-beginning
```

这样就监听完成。然后我们就开始生产消息给这个监听者。

```
[root@localhost bin]# sh kafka-console-producer.sh  --broker-list 192.168.0.102:9092 --topic test
```

![1566205333438](./img/1566205333438.png)

然后消费端也就收到了消息。

![1566205352438](./img/1566205352438.png)

```
ps -ef|grep kafka
kill -9 xxx
```

#### 关于kafka的集群

首先三个kafka我先保证三个文件的内容保持一致，即Server.properties

然后我们需要修改

```
broker.id=1
```

和zk的myid一样，要保证每个节点不一样，然后就是

```
listeners=PLAINTEXT://192.168.0.102:9092
```

每个节点请改成自己的本地的ip，和端口号，如果是一台机器，请注意端口号的冲突问题。

### Kafka 的应用

引入依赖

```xml
<dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.0.0</version>
        </dependency>
```

然后生产者的代码

```java
package com.pop.kafka.kafkapractice;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Pop
 * @date 2019/8/19 17:52
 */
public class PopKafkaProducer extends Thread{

    KafkaProducer<Integer,String> producer;
    String topic;//主题

    public PopKafkaProducer(String topic) {

        Properties properties = new Properties();

        //集群条件用逗号隔开，和原本的server.properties的提示一样
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "192.168.0.102:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG,"pop-producer");
        //申明序列化的方式， 分别是key和value
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<Integer, String>(properties);

        this.topic = topic;
    }

    @Override
    public void run() {

        int num = 0;
        String msg = "";
        while(num<20){
            try {
                msg = "pop kafka practice msg :"+num;
                //get 会拿到执行结果。
                RecordMetadata recordMetadata=producer.send(new ProducerRecord<>(topic,msg)).get();
                System.out.println(recordMetadata.offset()+"->"+recordMetadata.partition()+"->"+recordMetadata.topic());
                TimeUnit.SECONDS.sleep(4);
                ++num;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new PopKafkaProducer("test").start();
    }
}

```

消费端的代码

```java
package com.pop.kafka.kafkapractice;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Pop
 * @date 2019/8/19 17:52
 */
public class PopKafkaConsumer extends Thread{

    KafkaConsumer<Integer,String> consumer;
    String topic;

    public PopKafkaConsumer(String topic) {

        Properties properties = new Properties();

        //集群条件用逗号隔开，和原本的server.properties的提示一样
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "192.168.0.102:9092");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG,"pop-consumer");
        //分组
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"pop-gid");
        //超时时间
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,"30000");
        //自动提交的间隔
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");


          //申明饭序列化的方式， 分别是key和value
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        consumer = new KafkaConsumer<Integer, String>(properties);


        this.topic = topic;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singleton(this.topic));
        while(true){//不停的拿服务
            ConsumerRecords<Integer,String> consumerRecords=consumer.poll(Duration.ofSeconds(1));
            consumerRecords.forEach(record->{
                System.out.println(record.key()+"->"+
                        record.value()+"->"+record.offset());
            });
        }
    }

    public static void main(String[] args) {
        new PopKafkaConsumer("test").start();
    }

}

```

你也可以用异步的方法获得请求，kafka默认是异步的，只不过你可以通过get方法来获得同步的方式。

```java
 //异步的操作可以调用回调方法
                producer.send(new ProducerRecord<>(topic, msg), (metadata, execption) -> {
                    System.out.println(recordMetadata.offset()+"->"+recordMetadata.partition()+"->"+recordMetadata.topic());
                });
```

同时，在设置异步的时候，我们还需要配置一些其他参数，就是批处理。

```java
 //批量发送
      properties.put(ProducerConfig.BATCH_SIZE_CONFIG,"");
        //如果不可达会如何 间隔时间
        properties.put(ProducerConfig.LINGER_MS_CONFIG,"");
```

当然，以上方法是属于p2p，也就是点对点，如果我们启动了两个消费者是否都可以收到订阅后的消息呢，答案是否定的。

如果你希望两个消费者都可以收到订阅的消息，就需要按组区别开来

```java
 properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "192.168.0.102:9092");
//这里就需要修改了
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG,"pop-consumer");
```

不同的组才可以都收到消息，同一个组有一个收到消息。



### SpringBoot 与 kafka的整合

```XML
 <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
            <version>2.2.0.RELEASE</version>
        </dependency>
```

配置文件

```properties
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.IntegerSerializer

spring.kafka.bootstrap-servers=192.168.0.102:9092

spring.kafka.consumer.group-id=springboot-groupid
spring.kafka.consumer.auto.offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=true

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
```

生产者

```java
@Component
public class PopKafkaProducer {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void send(){
        kafkaTemplate.send("test","msgkey");
    }
}
```

消费者。

```java
@Component
public class PopKafkaConsumer {

    @KafkaListener(topics = {"test"})
    public void listener(ConsumerRecord record){
        Optional msg = Optional.ofNullable(record.value());
        if(msg.isPresent()){
            System.out.println(msg.get());
        }
    }

}
```

