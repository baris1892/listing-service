package dev.baristop.portfolio.listingservice.command.jobs;

import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(
    name = "listings:disable",
    description = "Disables expired listings"
)
@RequiredArgsConstructor
@Slf4j
public class DisableListingsCommand implements Runnable {

    private final ListingService listingService;
    public static final String OLDER_THAN_DEFAULT = "14";

    @Option(
        names = {"--older-than"},
        description = "Disable listings older than given days",
        defaultValue = OLDER_THAN_DEFAULT
    )
    private int olderThanDays;

    @Override
    public void run() {
        log.info("Starting DisableListingsCommand for listings older than {} days", olderThanDays);
        int affected = listingService.disableListingsOlderThanDays(olderThanDays);
        log.info("Disabled {} listings", affected);
    }
}
