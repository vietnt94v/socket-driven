package com.socketdriven.chat.api.dto;

import java.util.List;

public record ChatSearchResponse(
    List<UserSummaryDto> users, List<GroupSearchHitDto> groups) {}
