package com.aptvantage.twitter.ingestor.fake;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class User {
    Long id;
    String id_str;
    String name;
    String screen_name;
    String description;
}
