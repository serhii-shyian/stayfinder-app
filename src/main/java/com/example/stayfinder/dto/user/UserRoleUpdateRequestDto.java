package com.example.stayfinder.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.hibernate.validator.constraints.Length;

public record UserRoleUpdateRequestDto(
        @NotEmpty(message = "Roles list may not be empty")
        List<@NotBlank(message = "Role name may not be blank")
                @Length(min = 1, max = 10) String> roleNames) {
}
