package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDTO
{
    private int userId;
    private String userName;
    private String email;
}
