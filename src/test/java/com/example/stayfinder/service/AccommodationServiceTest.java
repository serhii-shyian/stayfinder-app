package com.example.stayfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.stayfinder.dto.accommodation.AccommodationDto;
import com.example.stayfinder.dto.accommodation.AccommodationRequestDto;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.mapper.AccommodationMapper;
import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Address;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.accommodation.AccommodationRepository;
import com.example.stayfinder.repository.address.AddressRepository;
import com.example.stayfinder.service.accommodation.AccommodationServiceImpl;
import com.example.stayfinder.service.notification.NotificationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceTest {
    @InjectMocks
    private AccommodationServiceImpl accommodationService;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("""
            Save accommodation to database from valid DTO
            """)
    public void saveAccommodation_ValidRequestDto_ReturnsAccommodationDto() {
        // Given
        AccommodationRequestDto requestDto = getCreateAccommodationRequestDto();
        Accommodation accommodation = getAccommodationFromDto(requestDto);
        AccommodationDto expected = getDtoFromAccommodation(accommodation);
        User user = getUser();

        when(accommodationMapper.toEntity(requestDto)).thenReturn(accommodation);
        when(addressRepository.findByAddress(requestDto.location()))
                .thenReturn(Optional.of(createAddress(requestDto.location())));
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        // When
        AccommodationDto actual = accommodationService.save(requestDto, user);

        // Then
        assertEquals(expected, actual);
        verify(notificationService).sendCreateAccommodationMessage(accommodation, user);
        verifyNoMoreInteractions(
                accommodationRepository, accommodationMapper, notificationService);
    }

    @Test
    @DisplayName("""
            Find all accommodations with valid pageable
            """)
    public void findAllAccommodations_ValidPageable_ReturnsAccommodationDtoList() {
        // Given
        Accommodation firstAccommodation = getAccommodationList().get(0);
        Accommodation secondAccommodation = getAccommodationList().get(1);
        List<Accommodation> accommodationList = List.of(
                firstAccommodation, secondAccommodation);
        List<AccommodationDto> expected = List.of(
                getDtoFromAccommodation(firstAccommodation),
                getDtoFromAccommodation(secondAccommodation));
        Pageable pageable = PageRequest.of(0, 5);

        when(accommodationRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(accommodationList));
        when(accommodationMapper.toDtoList(accommodationList)).thenReturn(expected);

        // When
        List<AccommodationDto> actual = accommodationService.findAll(pageable);

        // Then
        assertEquals(expected, actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper);
    }

    @Test
    @DisplayName("""
            Find accommodation by id when accommodation exists
            """)
    public void findAccommodation_ExistingAccommodationId_ReturnsAccommodationDto() {
        // Given
        Accommodation accommodation = getAccommodationList().get(0);
        AccommodationDto expected = getDtoFromAccommodation(accommodation);

        when(accommodationRepository.findById(accommodation.getId()))
                .thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation))
                .thenReturn(expected);

        // When
        AccommodationDto actual = accommodationService.findById(accommodation.getId());

        // Then
        assertEquals(expected, actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper);
    }

    @Test
    @DisplayName("""
            Find accommodation by id when accommodation does not exist
            """)
    public void findAccommodation_NonExistingAccommodationId_ThrowsException() {
        // Given
        when(accommodationRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> accommodationService.findById(99L));
        verifyNoMoreInteractions(accommodationRepository);
    }

    @Test
    @DisplayName("""
            Update accommodation by id when id exists
            """)
    void updateAccommodation_ExistingId_ReturnsAccommodationDto() {
        // Given
        Accommodation accommodation = getAccommodationList().get(0);
        accommodation.setLocation(createAddress("Updated Location"));
        AccommodationRequestDto requestDto = getCreateAccommodationRequestDto();
        AccommodationDto expected = getDtoFromAccommodation(accommodation);

        when(accommodationRepository.findById(accommodation.getId()))
                .thenReturn(Optional.of(accommodation));
        doNothing().when(accommodationMapper).updateEntityFromDto(
                requestDto, accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        // When
        AccommodationDto actual = accommodationService.updateById(
                accommodation.getId(), requestDto);

        // Then
        assertEquals(expected, actual);
        verifyNoMoreInteractions(accommodationRepository, accommodationMapper);
    }

    @Test
    @DisplayName("""
            Update accommodation by id when id does not exist
            """)
    void updateAccommodation_NonExistingId_ThrowsException() {
        // Given
        AccommodationRequestDto requestDto = getCreateAccommodationRequestDto();

        when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> accommodationService.updateById(99L, requestDto));
        verifyNoMoreInteractions(accommodationRepository);
    }

    @Test
    @DisplayName("""
            Delete accommodation by id when id exists
            """)
    void deleteAccommodation_ExistingId_ReturnsNothing() {
        // Given
        Accommodation accommodation = getAccommodationList().get(0);

        when(accommodationRepository.findById(accommodation.getId()))
                .thenReturn(Optional.of(accommodation));

        // When
        accommodationService.deleteById(accommodation.getId());

        // Then
        verify(accommodationRepository).delete(accommodation);
    }

    @Test
    @DisplayName("""
            Delete accommodation by id when id does not exist
            """)
    void deleteAccommodation_NonExistingId_ThrowsException() {
        // Given
        when(accommodationRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> accommodationService.deleteById(99L));
        verifyNoMoreInteractions(accommodationRepository);
    }

    private Address createAddress(String address) {
        return new Address()
                .setAddress(address);
    }

    private AccommodationDto getDtoFromAccommodation(Accommodation accommodation) {
        return new AccommodationDto(
                accommodation.getId(),
                accommodation.getType().name(),
                accommodation.getLocation().getAddress(),
                accommodation.getSize(),
                accommodation.getAmenities().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()),
                accommodation.getDailyRate(),
                accommodation.getAvailability());
    }

    private Accommodation getAccommodationFromDto(AccommodationRequestDto requestDto) {
        return new Accommodation()
                .setType(Accommodation.Type.valueOf(requestDto.type()))
                .setLocation(createAddress(requestDto.location()))
                .setSize(requestDto.size())
                .setDailyRate(requestDto.dailyRate())
                .setAvailability(requestDto.availability())
                .setAmenities(requestDto.amenities().stream()
                        .map(Accommodation.Amenities::valueOf)
                        .collect(Collectors.toSet()));
    }

    private AccommodationRequestDto getCreateAccommodationRequestDto() {
        return new AccommodationRequestDto(
                "HOUSE",
                "City Center",
                "Large",
                Set.of("AIR_CONDITIONING", "WIFI"),
                BigDecimal.valueOf(150.0),
                10
        );
    }

    private List<Accommodation> getAccommodationList() {
        return List.of(
                new Accommodation()
                        .setId(1L)
                        .setType(Accommodation.Type.APARTMENT)
                        .setLocation(createAddress("Downtown"))
                        .setSize("Medium")
                        .setDailyRate(BigDecimal.valueOf(120.0))
                        .setAvailability(5)
                        .setAmenities(Set.of(
                                Accommodation.Amenities.BBQ_GRILL,
                                Accommodation.Amenities.PARKING)),
                new Accommodation()
                        .setId(2L)
                        .setType(Accommodation.Type.VACATION_HOME)
                        .setLocation(createAddress("Beachside"))
                        .setSize("Large")
                        .setDailyRate(BigDecimal.valueOf(250.0))
                        .setAvailability(3)
                        .setAmenities(Set.of(
                                Accommodation.Amenities.SWIMMING_POOL,
                                Accommodation.Amenities.BALCONY))
        );
    }

    private User getUser() {
        return new User()
                .setId(1L)
                .setUsername("testUser");
    }
}
