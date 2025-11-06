package app.controllers;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.exceptions.DatabaseException;
import app.services.UserService;
import app.routes.Path;
import app.views.ViewPaths;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class UserController
{
    private UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    public void addRoutes(Javalin app)
    {
        app.get(Path.LOGIN, ctx -> renderLoginPage(ctx));
        app.get(Path.CREATE_USER, ctx -> renderCreateUserPage(ctx));
        app.get(Path.LOGOUT, ctx -> handleLogout(ctx));


        app.post(Path.LOGIN, ctx -> handleLoginSubmit(ctx));
        app.post(Path.CREATE_USER, ctx -> handleCreateUserSubmit(ctx));
    }

    private void renderCreateUserPage(Context ctx)
    {
        ctx.render(ViewPaths.CREATE_USER);
    }

    private void renderLoginPage(Context ctx)
    {
        ctx.render(ViewPaths.LOGIN);
    }

    private void handleCreateUserSubmit(Context ctx)
    {
        String email = ctx.formParam("email").trim().toLowerCase();
        String username = ctx.formParam("username").trim();
        String password1 = ctx.formParam("password1").trim();
        String password2 =  ctx.formParam("password2").trim();

        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                email,
                username,
                password1,
                password2
        );

        try
        {
             if(userService.registerUser(createUserRequestDTO))
             {
                 ctx.sessionAttribute("successMessage", "Du har oprettet en bruger. Log ind for at bruge appen.");
                 ctx.redirect("/login");
             }else
             {
                 ctx.sessionAttribute("errorMessage","Kunne ikke oprette en bruger prøv igen");
                 ctx.render("createuser");
             }

        } catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage",e.getMessage());
            ctx.render("createuser");
        } catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute(e.getMessage());
            ctx.render("createuser");
        }
    }

    private void handleLogout(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }


    private void handleLoginSubmit(Context ctx)
    {
        String email = ctx.formParam("email").trim().toLowerCase();
        String password = ctx.formParam("password").trim();

        System.out.println(email);
        System.out.println(password);

        try
        {
            UserDTO currentUser = userService.authenticate(email, password);
            if(currentUser != null)
            {
                ctx.sessionAttribute("currentUser",currentUser);
                ctx.redirect("/");
            }
            else
            {
                ctx.attribute("errorMessage", "Forkert email eller kodeord");
                ctx.render("login.html");
            }
        } catch (DatabaseException e) {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.render("login");
        }


    }
}
