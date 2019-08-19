package com.pop.kafka.kafkapractice.spring;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Pop
 * @date 2019/8/20 0:47
 */
public class MyPartition implements Partitioner {

    @Override
    public int partition(String s, Object o, byte[] bytes, Object o1, byte[] bytes1, Cluster cluster) {
        /**
         * 这个方法为核心方法，返回值代表会落到具体哪个分区
         * 如果是0 就是 1号分区，依次类推
         */
        System.out.println("enter");
        //获得这个topic的所有分区
        List<PartitionInfo> list=cluster.partitionsForTopic(s);
        int len = list.size();
        if(o==null){//我们在发送消息的时候会指定key和value，key如果指定了的话
            //kafka会根据key，计算，如果没有，也会有默认的计算方式
            Random random = new Random();
            return random.nextInt(len);
        }
        return Math.abs(o.hashCode())%len;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
