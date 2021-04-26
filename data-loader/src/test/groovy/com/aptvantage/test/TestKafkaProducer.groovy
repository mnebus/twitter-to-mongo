package com.aptvantage.test

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic

@KafkaClient
interface TestKafkaProducer {

    @Topic('${data-loader.source.topic:twitter-tweets}')
    void produceMessage(@KafkaKey String key, String message)

}
