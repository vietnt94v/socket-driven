package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.ChatSearchResponse;
import com.socketdriven.chat.service.ChatSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatSearchController {

  private final ChatSearchService chatSearchService;

  public ChatSearchController(ChatSearchService chatSearchService) {
    this.chatSearchService = chatSearchService;
  }

  @GetMapping("/search")
  public ChatSearchResponse search(@RequestParam(required = false) String q) {
    return chatSearchService.search(CurrentUserId.get(), q);
  }
}
