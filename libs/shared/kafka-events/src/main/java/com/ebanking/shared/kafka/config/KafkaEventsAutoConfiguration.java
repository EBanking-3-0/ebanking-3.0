package com.ebanking.shared.kafka.config;

import com.ebanking.shared.kafka.events.BaseEvent;
import com.ebanking.shared.kafka.producer.EventProducer;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Auto-configuration for Kafka Events library.
 * Automatically configures producers and consumers when Spring Kafka is available.
 */
@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@Import({KafkaProducerConfig.class, KafkaConsumerConfig.class})
public class KafkaEventsAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public EventProducer eventProducer(KafkaTemplate<String, BaseEvent> kafkaTemplate) {
        return new EventProducer(kafkaTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public TypedEventProducer typedEventProducer(EventProducer eventProducer) {
        return new TypedEventProducer(eventProducer);
    }
}

