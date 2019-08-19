package com.pop.kafka.kafkapractice.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Pop
 * @date 2019/8/19 22:55
 */
@Component
public class PopKafkaProducer {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void send(){
        kafkaTemplate.send("test","msgkey");
    }
}
