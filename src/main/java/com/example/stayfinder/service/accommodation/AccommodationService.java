package com.example.stayfinder.service.accommodation;

import com.example.stayfinder.dto.accommodation.AccommodationDto;
import com.example.stayfinder.dto.accommodation.AccommodationRequestDto;
import com.example.stayfinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationDto save(AccommodationRequestDto requestDto, User user);

    Page<AccommodationDto> findAll(Pageable pageable);

    AccommodationDto findById(Long id);

    AccommodationDto updateById(Long id, AccommodationRequestDto requestDto);

    void deleteById(Long id);
}
