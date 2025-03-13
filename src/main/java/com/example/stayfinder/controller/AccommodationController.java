package com.example.stayfinder.controller;

import com.example.stayfinder.dto.accommodation.AccommodationDto;
import com.example.stayfinder.dto.accommodation.AccommodationRequestDto;
import com.example.stayfinder.model.User;
import com.example.stayfinder.service.accommodation.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accommodations")
@Tag(name = "Accommodation management", description = "Endpoint for managing accommodations")
@Validated
public class AccommodationController {
    private final AccommodationService accommodationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new accommodation",
            description = "Creating a new accommodation according to the parameters")
    @PreAuthorize("hasRole('ADMIN')")
    public AccommodationDto create(Authentication authentication,
                                   @RequestBody AccommodationRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return accommodationService.save(requestDto, user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all accommodation",
            description = "Getting a list of all accommodation")
    public Page<AccommodationDto> getAll(Pageable pageable) {
        return accommodationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get accommodation by id",
            description = "Getting an accommodation by id if available")
    public AccommodationDto getById(@PathVariable Long id) {
        return accommodationService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update accommodation by id",
            description = "Updating an accommodation by id if available")
    @PreAuthorize("hasRole('ADMIN')")
    public AccommodationDto updateById(@PathVariable Long id,
                                       @RequestBody AccommodationRequestDto requestDto) {
        return accommodationService.updateById(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete accommodation by id",
            description = "Deleting an accommodation by id if available")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(@PathVariable Long id) {
        accommodationService.deleteById(id);
    }
}
