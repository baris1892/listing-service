package dev.baristop.portfolio.listingservice.kafka;

import dev.baristop.portfolio.listingservice.kafka.dto.ListingStatusChangedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ListingStatusProducer {

    public static final String TOPIC = "listing-status-changed";

    private final KafkaTemplate<String, ListingStatusChangedEvent> kafkaTemplate;

    public void sendListingStatusEvent(ListingStatusChangedEvent event) {
        kafkaTemplate.send(TOPIC, event)
            .whenComplete((r, e) -> {
                if (e != null) log.error("Failed to send ListingStatusChangedEvent", e);
                else log.info("ListingStatusChangedEvent sent: {}", event);
            });
    }
}
