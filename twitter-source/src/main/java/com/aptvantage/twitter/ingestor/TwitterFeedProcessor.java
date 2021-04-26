package com.aptvantage.twitter.ingestor;

import com.twitter.hbc.core.event.Event;
import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Context
public class TwitterFeedProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TwitterFeedProcessor.class);

    TwitterFeedIngestionWorker ingestionWorker;
    TwitterFeedIngestionExecutorService executorService;

    TwitterFeedProcessor(TwitterFeed twitterFeed, TwitterMessageProducer twitterProducer) {
        this.ingestionWorker = new TwitterFeedIngestionWorker(twitterFeed, twitterProducer);
        this.executorService = new TwitterFeedIngestionExecutorService(this);
    }

    @PostConstruct
    void start() {
        this.executorService.submit(ingestionWorker);
        logger.info("TwitterFeedProcessor started");
    }

    @PreDestroy
    void stop() {
        ingestionWorker.shutdown = true;
        this.executorService.shutdown();
        logger.info("TwitterFeedProcessor stopped");
    }

    static class TwitterFeedIngestionExecutorService extends ThreadPoolExecutor {
        TwitterFeedProcessor feedProcessor;

        TwitterFeedIngestionExecutorService(TwitterFeedProcessor feedProcessor) {
            super(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(), new TwitterFeedIngestionThreadFactory());
            this.feedProcessor = feedProcessor;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (!feedProcessor.ingestionWorker.shutdown) {
                try {
                    FutureTask ft = (FutureTask)r;
                    ft.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("WorkerThread exception:",e);
                }  finally {
                    logger.info("Restarting Failed Worker");
                    feedProcessor.executorService.submit(feedProcessor.ingestionWorker);
                }

            }
        }
    }

    static class TwitterFeedIngestionThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.format("twitter-ingestion-worker-%s", threadNumber.getAndIncrement()));
        }
    }

    static class TwitterFeedIngestionWorker implements Runnable {

        boolean shutdown = false;
        TwitterFeed twitterFeed;
        TwitterMessageProducer producer;

        TwitterFeedIngestionWorker(TwitterFeed twitterFeed, TwitterMessageProducer producer) {
            this.producer = producer;
            this.twitterFeed = twitterFeed;
        }

        @Override
        public void run() {
            logger.info("TwitterIngestionWorker started");
            while (!shutdown) {
                try {
                    Event event = this.twitterFeed.getEventQueue().peek();
                    if (event != null) {
                        event = this.twitterFeed.getEventQueue().poll(50, TimeUnit.MILLISECONDS);
                        logger.info("received twitter event [{}]", event);
                        logger.info("event type: {}",event.getEventType());
                        logger.info("event message: {}",event.getMessage());
                        logger.error("underlying exception",event.getUnderlyingException());
                    } else {
                        String msg = this.twitterFeed.getMsgQueue().poll(500, TimeUnit.MILLISECONDS);
                        if (msg != null) {
                            producer.sendMessage(msg);
                            logger.info(msg);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
            logger.info("TwitterIngestionWorker shutting down gracefully");
        }
    }
}
