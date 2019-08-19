package com.pop.kafka.kafkapractice;

import org.apache.kafka.clients.producer.*;
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

        //批量发送
//        properties.put(ProducerConfig.BATCH_SIZE_CONFIG,"");
        //如果不可达会如何 间隔时间
//        properties.put(ProducerConfig.LINGER_MS_CONFIG,"");

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
                //get 会拿到执行结果。这里同步的操作
                RecordMetadata recordMetadata=producer.send(new ProducerRecord<>(topic,msg)).get();

                //异步的操作可以调用回调方法
                producer.send(new ProducerRecord<>(topic, msg), (metadata, execption) -> {
                    System.out.println(recordMetadata.offset()+"->"+recordMetadata.partition()+"->"+recordMetadata.topic());
                });

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
