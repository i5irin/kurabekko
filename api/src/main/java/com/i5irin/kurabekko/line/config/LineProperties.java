package com.i5irin.kurabekko.line.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "line")
public record LineProperties(String channelSecret) {
}
