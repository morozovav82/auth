package ru.morozov.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseUserDto {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
}
