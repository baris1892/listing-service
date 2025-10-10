package dev.baristop.portfolio.listingservice.command.jobs;

import dev.baristop.portfolio.listingservice.listing.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.mockito.Mockito.*;

class DisableListingsCommandTest {

    private ListingService listingService;
    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        listingService = mock(ListingService.class);
        DisableListingsCommand command = new DisableListingsCommand(listingService);
        cmd = new CommandLine(command);
    }

    @Test
    void shouldCallServiceWithDefaultDaysWhenNoArgumentProvided() {
        // Arrange
        when(listingService.disableListingsOlderThanDays(anyInt())).thenReturn(5);

        // Act
        int exitCode = cmd.execute();

        // Assert
        verify(listingService).disableListingsOlderThanDays(
            Integer.parseInt(DisableListingsCommand.OLDER_THAN_DEFAULT)
        );
        assert (exitCode == 0);
    }

    @Test
    void shouldCallServiceWithProvidedDaysArgument() {
        // Arrange
        when(listingService.disableListingsOlderThanDays(anyInt())).thenReturn(3);

        // Act
        int exitCode = cmd.execute("--older-than", "7");

        // Assert
        verify(listingService).disableListingsOlderThanDays(7);
        assert (exitCode == 0);
    }

    @Test
    void shouldReturnNonZeroExitCodeOnException() {
        // Arrange
        when(listingService.disableListingsOlderThanDays(anyInt()))
            .thenThrow(new RuntimeException("Something went wrong"));

        // Act
        int exitCode = cmd.execute();

        // Assert
        verify(listingService).disableListingsOlderThanDays(Integer.parseInt(DisableListingsCommand.OLDER_THAN_DEFAULT));
        assert (exitCode != 0);
    }
}
