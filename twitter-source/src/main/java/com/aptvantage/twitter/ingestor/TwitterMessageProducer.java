package com.aptvantage.twitter.ingestor;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.context.annotation.Value;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Singleton;

@Singleton
public class TwitterMessageProducer {

    private Producer<String, String> kafkaProducer;
    private String topic;

    public TwitterMessageProducer(@KafkaClient("tweet-producer") Producer<String, String> kafkaProducer,
                                  @Value("${twitter-source.topic:twitter-tweets}") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    public void sendMessage(String tweet) {
        kafkaProducer.send(new ProducerRecord<>(this.topic,null, tweet));
    }
}
