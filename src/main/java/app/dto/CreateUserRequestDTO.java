package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateUserRequestDTO
{
    private String email;
    private String userName;
    private String password1;
    private String password2;
}
