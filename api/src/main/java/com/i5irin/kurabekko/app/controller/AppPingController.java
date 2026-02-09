package com.i5irin.kurabekko.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppPingController {

  @GetMapping("/app/ping")
  public String ping() {
    return "pong";
  }
}
