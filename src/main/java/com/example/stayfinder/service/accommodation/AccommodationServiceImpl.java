package com.example.stayfinder.service.accommodation;

import com.example.stayfinder.dto.accommodation.AccommodationDto;
import com.example.stayfinder.dto.accommodation.AccommodationRequestDto;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.mapper.AccommodationMapper;
import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Address;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.accommodation.AccommodationRepository;
import com.example.stayfinder.repository.address.AddressRepository;
import com.example.stayfinder.service.notification.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final AddressRepository addressRepository;
    private final NotificationService notificationService;

    @Override
    public AccommodationDto save(AccommodationRequestDto requestDto, User user) {
        Accommodation accommodationFromDto = accommodationMapper.toEntity(requestDto);
        Address address = addressRepository.findByAddress(requestDto.location())
                .orElseGet(() -> addressRepository.save(getAddress(requestDto.location())));
        accommodationFromDto.setLocation(address);
        Accommodation accommodationFromDb = accommodationRepository.save(accommodationFromDto);
        notificationService.sendCreateAccommodationMessage(accommodationFromDb, user);
        return accommodationMapper.toDto(accommodationFromDb);
    }

    @Override
    public List<AccommodationDto> findAll(Pageable pageable) {
        return accommodationMapper.toDtoList(
                accommodationRepository.findAll(pageable).getContent());
    }

    @Override
    public AccommodationDto findById(Long id) {
        Accommodation accommodationFromDb = findAccommodationById(id);
        return accommodationMapper.toDto(accommodationFromDb);
    }

    @Override
    public AccommodationDto updateById(Long id, AccommodationRequestDto requestDto) {
        Accommodation accommodationFromDb = findAccommodationById(id);
        addressRepository.findById(accommodationFromDb.getLocation().getId())
                .ifPresent(address -> address.setAddress(requestDto.location()));
        accommodationMapper.updateEntityFromDto(requestDto, accommodationFromDb);
        return accommodationMapper.toDto(accommodationRepository.save(accommodationFromDb));
    }

    @Override
    public void deleteById(Long id) {
        Accommodation accommodationFromDb = findAccommodationById(id);
        accommodationRepository.delete(accommodationFromDb);
    }

    private Address getAddress(String location) {
        return new Address()
                .setAddress(location);
    }

    private Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find accommodation by id: " + id));
    }
}
