package com.i5irin.kurabekko.line.controller;

import com.i5irin.kurabekko.line.applications.usecases.LineUnitCalcUsecase;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
@LineMessageHandler
public class LineWebhookController {

  private final LineUnitCalcUsecase unitCalcUsecase;
  private final MessagingApiClient messagingApiClient;

  public LineWebhookController(
      MessagingApiClient messagingApiClient, LineUnitCalcUsecase unitCalcUsecase) {
    this.messagingApiClient = messagingApiClient;
    this.unitCalcUsecase = unitCalcUsecase;
  }

  @EventMapping
  public void handle(Event event) {
    if (event instanceof MessageEvent me && me.message() instanceof TextMessageContent textMsg) {

      String text = textMsg.text();

      try {
        var result = this.unitCalcUsecase.execute(text);
        var req =
            new ReplyMessageRequest(
                me.replyToken(),
                result.success()
                    ? List.of(
                        new TextMessage(result.unitPriceMessage()),
                        new TextMessage(result.cheapestMessage()))
                    : List.of(new TextMessage(result.errorMessage())),
                false);
        messagingApiClient.replyMessage(req);
      } catch (IllegalArgumentException e) {
        // Not a Kurabekko command, do nothing
        return;
      }
    }
  }
}
