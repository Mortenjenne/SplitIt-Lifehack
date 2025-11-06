package app.controllers;

import app.dto.UserDTO;
import app.entities.User;
import app.entities.Group;
import app.exceptions.DatabaseException;
import app.services.AccountService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class GroupController {
    private AccountService accountService;

    public GroupController(AccountService accountService) {
        this.accountService = accountService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/", ctx -> index(ctx));
        app.get("/create-group", ctx -> showGroup(ctx));
        app.get("/create-group/cancel", ctx -> cancel(ctx));

        app.post("/create-group/setName", ctx -> createGroupName(ctx));
        app.post("/create-group/addMember", ctx -> addMemberToGroup(ctx));
        app.post("/create-group/create", ctx -> createGroup(ctx));
        app.post("/create-group/removeMember", ctx -> deleteMemberFromGroup(ctx));
    }

    private void deleteMemberFromGroup(Context ctx)
    {
        List<User> members = ctx.sessionAttribute("addedMembers");
        String userIdParam = ctx.formParam("userId");

        int userId = 0;
        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            ctx.sessionAttribute("errorMessage",e.getMessage());
        }
        try {
            User user = accountService.getUserById(userId);
            User tmpMember = null;
            for(User member: members)
            {
                if(user.getUserId() == member.getUserId()){
                    tmpMember = member;
                }
            }
            members.remove(tmpMember);
            ctx.attribute("addedMembers",members);
        } catch (DatabaseException e) {
            ctx.sessionAttribute("errorMessage",e.getMessage());
        }
        ctx.redirect("/create-group");

    }


    private void createGroup(Context ctx)
    {
        List<User> members = ctx.sessionAttribute("addedMembers");
        User currentUser = ctx.sessionAttribute("currentUser");
        String groupName = ctx.sessionAttribute("groupName");
        Group group = null;

        try
        {
            group = accountService.createGroup(groupName);
        } catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.sessionAttribute("errorMessage", e.getMessage());
        }

        try
        {
            accountService.addMemberToGroup(currentUser.getUserId(),group.getGroupId());
            for(User member: members){
                accountService.addMemberToGroup(member.getUserId(),group.getGroupId());
            }
        }catch (DatabaseException e){
            ctx.sessionAttribute("errorMessage", e.getMessage());
        }
        ctx.redirect("/");
    }

    private void addMemberToGroup(Context ctx)
    {
        String userIdParam = ctx.formParam("userId");
        if (userIdParam == null) {
            ctx.sessionAttribute("errorMessage", "No user selected");
            ctx.render("creategroup");
            return;
        }
        int userId = Integer.parseInt(userIdParam);

        try {
            User user = accountService.getUserById(userId);

            List<User> addedMembers = ctx.sessionAttribute("addedMembers");
            if (addedMembers == null) {
                addedMembers = new ArrayList<>();
            }

            if (!accountService.isUserInGroup(addedMembers, userId)){
                addedMembers.add(user);
            } else {
                ctx.sessionAttribute("errorMessage","User is already in group!");
            }

            ctx.sessionAttribute("addedMembers", addedMembers);

        } catch (DatabaseException e) {
            ctx.sessionAttribute("errorMessage", e.getMessage());
        }

        ctx.redirect("/create-group");
    }

    private void createGroupName(Context ctx)
    {
        String groupName = ctx.formParam("groupName");
        List<User> groupMembers = new ArrayList<>();
        ctx.sessionAttribute("groupName",groupName);
        ctx.sessionAttribute("addedMembers",groupMembers);

        ctx.redirect("/create-group");
    }

    private void showGroup(Context ctx)
    {
        UserDTO currentUser = ctx.sessionAttribute("currentUser");
        if (currentUser == null) {
            ctx.redirect("/");
            return;
        }

        try {
            String groupName = ctx.sessionAttribute("groupName");
            List<UserDTO> addedMembers = ctx.sessionAttribute("addedMembers");
            if(addedMembers == null){
                addedMembers = new ArrayList<>();
                addedMembers.add(currentUser);
            }

            List<User> allUsers = accountService.getAllUsers();
            List<User> users = new ArrayList<>();

            for (User user : allUsers) {
                if (user.getUserId() != currentUser.getUserId()) {
                    boolean alreadyAdded = false;
                    for (UserDTO addedMember : addedMembers) {
                        if (user.getUserId() == addedMember.getUserId()) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        users.add(user);
                    }
                }
            }

            ctx.attribute("availableUsers",users);
            ctx.attribute("groupName",groupName);
            ctx.attribute("addedMembers",addedMembers);

        } catch (DatabaseException e) {
            ctx.attribute("errorMessage",e.getMessage());
        }
        ctx.render("creategroup");
    }

    private void index(Context ctx)
    {
        UserDTO currentUser = ctx.sessionAttribute("currentUser");
        if (currentUser != null) {
            try {
                List<Group> groups = accountService.getUserGroups(currentUser.getUserId());
                ctx.attribute("groups", groups);

            } catch (DatabaseException e) {
                ctx.attribute("errorMessage", e.getMessage());
            }
        }

        ctx.render("index");
    }

    public void cancel(Context ctx)
    {
        ctx.sessionAttribute("groupName", null);
        ctx.sessionAttribute("addedMembers", null);
        ctx.redirect("/");
    }
}
