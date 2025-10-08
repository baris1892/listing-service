package dev.baristop.portfolio.listingservice.listing.service;

import dev.baristop.portfolio.listingservice.kafka.ListingStatusProducer;
import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import dev.baristop.portfolio.listingservice.listing.entity.ListingStatus;
import dev.baristop.portfolio.listingservice.listing.repository.ListingRepository;
import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import dev.baristop.portfolio.listingservice.security.entity.User;
import dev.baristop.portfolio.listingservice.security.repository.UserRepository;
import dev.baristop.portfolio.listingservice.testdata.ListingTestFactory;
import dev.baristop.portfolio.listingservice.testdata.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
class ListingServiceIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private ListingService listingService;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingTestFactory listingTestFactory;

    @Autowired
    private UserTestFactory userTestFactory;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @MockitoBean
    private ListingStatusProducer listingStatusProducer;

    private User owner;
    private UserPrincipal ownerPrincipal;

    @BeforeEach
    void setUp() {
        // Clean DB before each test
        listingRepository.deleteAll();
        userRepository.deleteAll();

        try (var connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
        }

        owner = userTestFactory.createDefaultUser();
        userRepository.saveAndFlush(owner);
        ownerPrincipal = userTestFactory.asPrincipal(owner);
    }

    @Test
    void shouldReturnCachedListing_OnSecondInvocation() {
        Listing listing = listingTestFactory.createListing("Redis Test");
        listing.setOwner(owner);
        listingRepository.saveAndFlush(listing);

        Long id = listing.getId();

        // 1st call -> should hit DB
        ListingDto dto1 = listingService.getListingById(id, ownerPrincipal);
        assertNotNull(dto1);
        assertEquals("Redis Test", dto1.getTitle());

        // 2nd call -> should hit Redis cache
        ListingDto dto2 = listingService.getListingById(id, ownerPrincipal);
        assertNotNull(dto2);
        assertEquals(dto1.getId(), dto2.getId());

        // Verify same content from cache
        assertEquals(dto1.getTitle(), dto2.getTitle());
    }

    @Test
    void testUpdateListingStatus_CachePutEviction() {
        Listing listing = listingTestFactory.createListing("Status Test");
        listing.setOwner(owner);
        listingRepository.saveAndFlush(listing);

        Long id = listing.getId();

        // Prime cache
        ListingDto dto = listingService.getListingById(id, ownerPrincipal);
        assertEquals("Status Test", dto.getTitle());

        // Update status (CachePut should update cache)
        listingService.updateListingStatus(id, ListingStatus.APPROVED);

        // Fetch again -> should reflect updated status from cache
        ListingDto updatedDto = listingService.getListingById(id, null);
        assertEquals(ListingStatus.APPROVED, updatedDto.getStatus());
    }
}
