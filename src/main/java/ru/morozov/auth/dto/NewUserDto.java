package ru.morozov.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewUserDto extends BaseUserDto {
    private String password;
}
