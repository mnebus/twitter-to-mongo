package com.aptvantage

import com.aptvantage.test.TestDatabaseClient
import com.aptvantage.test.TestKafkaProducer
import com.aptvantage.test.TestResources
import io.micronaut.core.util.CollectionUtils
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.inject.Inject
import java.util.stream.Stream

@MicronautTest
class DataLoaderSpec extends Specification implements TestPropertyProvider {

    PollingConditions conditions = new PollingConditions()

    @Shared
    MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo")
            .withTag('4.0.10'))

    @Shared
    KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))

    @Shared
    boolean testClassInitialized = false

    @Inject
    TestKafkaProducer testKafkaProducer

    @Inject
    TestDatabaseClient testDatabaseClient

    def setupSpec() {
        if (!testClassInitialized) {
            testClassInitialized = initializeTestClass()
        }
    }

    boolean initializeTestClass() {
        Stream<GenericContainer>.of(mongoDBContainer, kafkaContainer)
                .parallel()
                .forEach(container -> container.start())
    }

    def cleanupSpec() {
        Stream<GenericContainer>.of(mongoDBContainer, kafkaContainer)
                .parallel()
                .forEach(container -> container.stop())
    }

    @Override
    Map<String, String> getProperties() {
        if (!testClassInitialized) {
            this.initializeTestClass()
        }
        return CollectionUtils.mapOf(
                'mongodb.uri', mongoDBContainer.getReplicaSetUrl("tweets"),
                "kafka.bootstrap.servers", kafkaContainer.getBootstrapServers()
        )
    }

    void 'the app consumes messages from kafka and stores them in a database'() {
        given: 'the id value from the tweet-1.json resource'
        long tweetId = 1381751340494295042

        expect: 'start with an empty database'
        testDatabaseClient.numberOfRecords() == 0

        when: 'tweets are produced on the source topic'
        Stream.of('tweet-1.json', 'tweet-2.json', 'tweet-3.json', 'tweet-4.json')
                .forEach(resource -> testKafkaProducer.produceMessage(null, TestResources.getResource(resource)))

        then: 'the tweets are stored in the database'
        conditions.eventually {
            testDatabaseClient.numberOfRecords() == 4
        }
        testDatabaseClient.findRecord(tweetId) != null
    }
}
