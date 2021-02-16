package ru.morozov.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends BaseUserDto {
    private static final long serialVersionUID = 1L;

    private Long id;
}
