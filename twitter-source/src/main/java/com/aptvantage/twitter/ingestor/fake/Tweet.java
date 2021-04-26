package com.aptvantage.twitter.ingestor.fake;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Tweet {

    private String created_at;
    private Long id;
    private String id_str;
    private String text;
    private String source;

    private User user;
}
