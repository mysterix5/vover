package com.github.mysterix5.vover.model;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String passwordRepeat;
}
