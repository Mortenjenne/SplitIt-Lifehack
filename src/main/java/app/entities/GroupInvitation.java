package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class GroupInvitation
{
    private int invitationId;
    private int groupId;
    private int invitedBy;
    private int recipientId;
    private Status status;
    private Timestamp createdAt;
}
