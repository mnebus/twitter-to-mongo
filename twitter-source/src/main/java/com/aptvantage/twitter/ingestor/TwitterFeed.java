package com.aptvantage.twitter.ingestor;

import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.event.Event;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Context
public class TwitterFeed {

    private static final Logger logger = LoggerFactory.getLogger(TwitterFeed.class);

    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);
    private Client twitterClient;
    private TwitterClientFactory twitterClientFactory;

    private boolean start = true;

    public TwitterFeed(TwitterClientFactory twitterClientFactory, @Value("${twitter-source.feed.startOnLoad:true}") boolean start) {
        this.twitterClientFactory = twitterClientFactory;
        this.start = start;
    }
    public TwitterFeed(TwitterClientFactory twitterClientFactory)
    {
        this(twitterClientFactory,true);
    }

    @PostConstruct
    void start() {
        if (start) {
            this.twitterClient = this.twitterClientFactory.createTwitterClient(msgQueue, eventQueue);
            this.twitterClient.connect();
            logger.info("TwitterFeed started");
        }
    }

    @PreDestroy
    void shutdown() {
        if (twitterClient != null) {
            twitterClient.stop();
            logger.info("TwitterFeed stopped");
        }
    }

    public BlockingQueue<String> getMsgQueue() {
        return msgQueue;
    }

    public BlockingQueue<Event> getEventQueue() {
        return eventQueue;
    }
}
