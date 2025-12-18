package com.ebanking.assistant.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "analytics-service")
public interface AnalyticsServiceClient {

  @GetMapping("/api/analytics/user/{userId}/summary")
  Map<String, Object> getUserSummary(@PathVariable("userId") Long userId);
}
