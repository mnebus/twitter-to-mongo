package com.aptvantage.data.loader;

import com.google.common.flogger.FluentLogger;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.List;
import java.util.concurrent.TimeUnit;

@KafkaListener(groupId = "twitter-message-consumer", offsetReset = OffsetReset.EARLIEST, batch = true, threads = 2)
public class TwitterMessageConsumer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private AtlasLoader loader;

    public TwitterMessageConsumer(AtlasLoader loader) {
        this.loader = loader;
    }

    @Topic("${data-loader.source.topic}")
    void receive(List<String> tweets) {
        logger.atInfo().atMostEvery(10, TimeUnit.SECONDS).log("consumed %s record from kafka", tweets.size());
        this.loader.loadIntoDatabase(tweets);
    }
}
