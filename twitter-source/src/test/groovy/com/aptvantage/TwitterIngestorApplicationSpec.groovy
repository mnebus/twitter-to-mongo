package com.aptvantage

import com.aptvantage.twitter.ingestor.TwitterFeed
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.util.concurrent.TimeUnit

@MicronautTest
class TwitterIngestorApplicationSpec extends Specification {

    @Inject
    TwitterFeed twitterFeed

    @Inject
    TestTwitterClientFactory testTwitterClientFactory

    @Inject
    FakeKafkaProducer fakeKafkaProducer

    void 'twitter client is created and connected when application is loaded'() {
        expect:
        twitterFeed.twitterClient != null
        testTwitterClientFactory.lastTestClient.connected
    }

    void 'messages from twitter to produced to a topic'() {
        when: 'a message is received from twitter'
        testTwitterClientFactory.lastTestClient.msgQueue.add("a-new-message")

        then: 'a message is sent to the producer'
        "a-new-message" == fakeKafkaProducer.messages.poll(2, TimeUnit.SECONDS)
    }

    void 'a failed worker restarts itself'() {
        given: 'a producer will explode'
        fakeKafkaProducer.explodeNextMessage = true

        when: 'the producer explodes'
        testTwitterClientFactory.lastTestClient.msgQueue.add("an-exploding-message")

        and: 'a non-exploding message is received'
        testTwitterClientFactory.lastTestClient.msgQueue.add("a-non-exploding-message")

        then: 'the non-exploding message is produced'
        "a-non-exploding-message" == fakeKafkaProducer.messages.poll(2, TimeUnit.SECONDS)
    }

}
