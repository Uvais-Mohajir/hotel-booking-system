package com.hotelbooking.dto;

import com.hotelbooking.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LoginDTO", description = "Login payload")
public class LoginDTO {

    @Email
    @NotBlank
    @Schema(description = "Email used for login", example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Raw password for authentication", example = "secret123")
    private String password;

    @NotNull
    @Schema(description = "Role for role-based login", example = "ROLE_USER")
    private Role role;
}

