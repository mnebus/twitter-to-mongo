package com.aptvantage

import com.aptvantage.twitter.ingestor.TwitterClientFactory
import com.twitter.hbc.core.Client
import com.twitter.hbc.core.StatsReporter
import com.twitter.hbc.core.endpoint.StreamingEndpoint
import com.twitter.hbc.core.event.Event
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Value

import javax.inject.Singleton
import java.util.concurrent.BlockingQueue

@Replaces(TwitterClientFactory)
@Singleton
class TestTwitterClientFactory extends TwitterClientFactory {

    TestClient lastTestClient

    TestTwitterClientFactory(@Value('${twitter.api-key}') String apiKey,
                             @Value('${twitter.api-key-secret}') String apiKeySecret,
                             @Value('${twitter.access-token}') String accessToken,
                             @Value('${twitter.access-token-secret}') String accessTokenSecret,
                             @Value('${twitter.keywords}') String keywords) {
        super(apiKey, apiKeySecret, accessToken, accessTokenSecret, keywords)
    }

    @Override
    Client createTwitterClient(BlockingQueue<String> msgQueue, BlockingQueue<Event> eventQueue) {
        this.lastTestClient = new TestClient(msgQueue,eventQueue)
        return lastTestClient
    }

    static class TestClient implements Client {

        boolean connected = false
        BlockingQueue<String> msgQueue
        BlockingQueue<Event> eventQueue

        TestClient(BlockingQueue<String> msgQueue, BlockingQueue<Event> eventQueue) {
            this.eventQueue = eventQueue
            this.msgQueue = msgQueue
        }

        @Override
        void connect() {
            connected = true
        }

        @Override
        void reconnect() {}

        @Override
        void stop() {}

        @Override
        void stop(int waitMillis) {}

        @Override
        boolean isDone() { return false }

        @Override
        String getName() { return null }

        @Override
        StreamingEndpoint getEndpoint() { return null }

        @Override
        StatsReporter.StatsTracker getStatsTracker() { return null }
    }
}
