package com.pop.kafka.kafkapractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.pop.kafka.kafkapractice.spring.PopKafkaProducer;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class KafkapracticeApplication {

    public static void main(String[] args) throws InterruptedException {
//        SpringApplication.run(KafkapracticeApplication.class, args);
        ConfigurableApplicationContext context
                 = SpringApplication.run(KafkapracticeApplication.class, args);
        PopKafkaProducer kp = context.getBean(PopKafkaProducer.class);
        for (int i = 0; i < 10; i++) {
            kp.send();
            TimeUnit.SECONDS.sleep(2);
        }

    }

}
