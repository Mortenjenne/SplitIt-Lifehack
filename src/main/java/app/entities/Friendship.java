package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Friendship
{
    private int friendshipId;
    private int userId;
    private int friendId;
    private Status status;
    private Timestamp createdAt;
}
