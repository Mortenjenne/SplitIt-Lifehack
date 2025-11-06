package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User
{
    private int userId;
    private String email;
    private String userName;
    private String password;
    private String role;

}
