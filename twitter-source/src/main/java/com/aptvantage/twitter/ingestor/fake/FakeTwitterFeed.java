package com.aptvantage.twitter.ingestor.fake;

import com.aptvantage.twitter.ingestor.TwitterClientFactory;
import com.aptvantage.twitter.ingestor.TwitterFeed;
import com.google.gson.Gson;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Context
@Requires(env = "fake-twitter")
@Replaces(TwitterFeed.class)
public class FakeTwitterFeed extends TwitterFeed {

    private static final Logger logger = LoggerFactory.getLogger(FakeTwitterFeed.class);
    private FakeTwitterFeedMessageGenerator generator;

    public FakeTwitterFeed(TwitterClientFactory twitterClientFactory) {
        super(twitterClientFactory, false);
        this.generator = new FakeTwitterFeedMessageGenerator(this);
    }

    @PostConstruct
    void start() {
        new Thread(this.generator).start();
        logger.info("Fake TwitterFeed started");
    }

    @PreDestroy
    void shutdown() {
        this.generator.stop();
        logger.info("Fake TwitterFeed stopped");
    }

    static class FakeTwitterFeedMessageGenerator implements Runnable {

        private FakeTwitterFeed fakeTwitterFeed;
        private boolean shutdown = false;

        private List<User> fakeUsers;

        private AtomicLong tweetIndex = new AtomicLong(1234121436);

        FakeTwitterFeedMessageGenerator(FakeTwitterFeed fakeTwitterFeed) {
            this.fakeTwitterFeed = fakeTwitterFeed;
            this.fakeUsers = generateFakeUsers();
        }

        private List<User> generateFakeUsers() {
            List<User> users = new ArrayList<>(1000);
            NameGenerator nameGenerator = new NameGenerator();
            long idCounter = 10000L;
            for (Name name : nameGenerator.generateNames(1000)) {
                users.add(User.builder()
                        .name(name.toString())
                        .screen_name(generateUserName(name))
                        .id(tweetIndex.incrementAndGet())
                        .id_str(Long.toString(idCounter))
                        .description(MarkovChain.markov(1,5))
                        .build());
            }

            return users;
        }

        public void stop() {
            this.shutdown = true;
        }

        private String generateUserName(Name name) {
            String lastName = name.getLastName();
            int lastNameLength = lastName.length();
            if (lastNameLength > 5) {
                lastNameLength = 5;
            }
            return name.getFirstName().charAt(0) + name.getLastName().substring(0, lastNameLength);
        }

        @Override
        public void run() {
            while(!shutdown) {
                Instant before = Instant.now();
                fakeTwitterFeed.getMsgQueue().add(generateTweet());
                try {
                    Thread.sleep(1000 - (Instant.now().minusMillis(before.toEpochMilli())).toEpochMilli());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        }

        String generateTweet() {
            Tweet tweet = Tweet.builder()
                    .user(fakeUsers.get(new java.util.Random().nextInt(1000)))
                    .created_at(Instant.now().toString())
                    .id(tweetIndex.incrementAndGet())
                    .id_str(Long.toString(tweetIndex.get()))
                    .source("FakeTwitterGenerator")
                    .text(MarkovChain.markov(4, 8))
                    .build();
            return new Gson().toJson(tweet);
        }
    }
}
