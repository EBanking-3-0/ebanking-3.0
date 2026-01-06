package com.ebanking.assistant.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ai.assistant")
public class AiModelProperties {

  @NotBlank private String provider = "openai";

  @NotBlank private String model = "gpt-4o-mini";

  private String apiKey = "";

  @Min(0)
  @Max(2)
  private double temperature = 0.7;

  @Positive private int maxTokens = 1000;
}
