package dev.baristop.portfolio.listingservice.listing.mapper;

import dev.baristop.portfolio.listingservice.listing.dto.ListingDto;
import dev.baristop.portfolio.listingservice.listing.entity.Listing;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListingMapper {

    ListingDto toDto(Listing listing);

    Listing toEntity(ListingDto listingDto);
}
