package dev.baristop.portfolio.listingservice.command.config;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "app",
    description = "Root command for all CLI jobs"
)
public class RootCommand {
}