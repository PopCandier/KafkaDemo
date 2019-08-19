package com.pop.kafka.kafkapractice;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
        //自动提交的间隔 批处理的提交
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");


        //申明饭序列化的方式， 分别是key和value
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        //配置了earliest可以消费过去的消息
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        consumer = new KafkaConsumer<Integer, String>(properties);


        this.topic = topic;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singleton(this.topic));
        while(true){//不停的拿服务,没拿到会阻塞
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
