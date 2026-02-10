package com.i5irin.kurabekko.logs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter f = new CommonsRequestLoggingFilter();
    f.setIncludeClientInfo(true);
    f.setIncludeQueryString(true);
    f.setIncludeHeaders(false);
    f.setIncludePayload(false);
    f.setMaxPayloadLength(10000);
    return f;
  }
}
