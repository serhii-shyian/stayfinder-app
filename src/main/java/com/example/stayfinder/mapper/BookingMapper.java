package com.example.stayfinder.mapper;

import com.example.stayfinder.config.MapperConfig;
import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookingMapper {
    @Mapping(source = "user", target = "userName", qualifiedByName = "setUserName")
    @Mapping(source = "accommodation.id", target = "accommodationId")
    BookingDto toDto(Booking booking);

    Booking toEntity(CreateBookingRequestDto requestDto);

    void updateEntityFromDto(CreateBookingRequestDto requestDto,
                             @MappingTarget Booking booking);

    @Named("setUserName")
    default String setUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
