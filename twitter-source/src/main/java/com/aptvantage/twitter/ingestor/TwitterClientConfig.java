package com.aptvantage.twitter.ingestor;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class TwitterClientConfig {
    private String apiKey;
    private String apiKeySecret;
    private String accessToken;
    private String accessTokenSecret;
    private List<String> keywords;
}
