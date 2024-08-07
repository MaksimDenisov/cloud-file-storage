package ru.denisovmaksim.cloudfilestorage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {

    @NotNull
    @Size(min = 3, message = "Length of name must be min 3 character")
    private String name;

    @NotNull
    @Size(min = 6, message = "Length of password must be min 6 character")
    private String password;

}
