package com.example.stayfinder.mapper;

import com.example.stayfinder.config.MapperConfig;
import com.example.stayfinder.dto.accommodation.AccommodationDto;
import com.example.stayfinder.dto.accommodation.AccommodationRequestDto;
import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {
    @Mapping(target = "location", ignore = true)
    Accommodation toEntity(AccommodationRequestDto requestDto);

    @Mapping(target = "location", source = "location", qualifiedByName = "addressToString")
    AccommodationDto toDto(Accommodation accommodation);

    @Mapping(target = "location", ignore = true)
    void updateEntityFromDto(AccommodationRequestDto requestDto,
                             @MappingTarget Accommodation accommodation);

    @Named("addressToString")
    default String mapAddressToString(Address location) {
        return location != null ? location.getAddress() : null;
    }
}
