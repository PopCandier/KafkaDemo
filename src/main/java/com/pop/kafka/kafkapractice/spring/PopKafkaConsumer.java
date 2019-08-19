package com.pop.kafka.kafkapractice.spring;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Pop
 * @date 2019/8/19 22:57
 */
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
