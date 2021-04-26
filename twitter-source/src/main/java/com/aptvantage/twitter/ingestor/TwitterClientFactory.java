package com.aptvantage.twitter.ingestor;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Singleton
public class TwitterClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClientFactory.class);

    private TwitterClientConfig twitterClientConfig;

    public TwitterClientFactory(@Value("${twitter.api-key}") String apiKey,
                                @Value("${twitter.api-key-secret}") String apiKeySecret,
                                @Value("${twitter.access-token}") String accessToken,
                                @Value("${twitter.access-token-secret}") String accessTokenSecret,
                                @Value("${twitter.keywords}") String keywords) {
        this.twitterClientConfig = TwitterClientConfig.builder()
                .apiKey(apiKey)
                .apiKeySecret(apiKeySecret)
                .accessToken(accessToken)
                .accessTokenSecret(accessTokenSecret)
                .keywords(Arrays.stream(
                        keywords.split(","))
                        .map(String::trim)
                        .filter( s -> !"".equals(s))
                        .collect(Collectors.toList()))
                .build();
        logger.info("Twitter config created with keywords: {}", this.twitterClientConfig.getKeywords());
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue, BlockingQueue<Event> eventQueue) {
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        hosebirdEndpoint.trackTerms(twitterClientConfig.getKeywords());

        Authentication hosebirdAuth = new OAuth1(
                twitterClientConfig.getApiKey(),
                twitterClientConfig.getApiKeySecret(),
                twitterClientConfig.getAccessToken(),
                twitterClientConfig.getAccessTokenSecret());

        ClientBuilder builder = new ClientBuilder()
                .name("twitter-client")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);

        return builder.build();

    }

}
