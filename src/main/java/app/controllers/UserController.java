package app.controllers;

import app.services.UserService;
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
        app.get("logout", ctx -> logout(ctx));
        app.get("createuser", ctx -> ctx.render("creategroup.html"));

        app.post("login", ctx -> login(ctx));
        app.post("createuser", ctx -> createUser(ctx));
    }

    private static void createUser(Context ctx)
    {
        String email = ctx.formParam("email");
        String username = ctx.formParam("username");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

                ctx.attribute("message", "Du er hermed oprettet med brugernavn: " + username + ". Nu skal du logge på.");
                ctx.render("index.html");
                ctx.attribute("message", "Dit brugernavn findes allerede. Prøv igen, eller log ind");
                ctx.render("creategroup.html");
            ctx.attribute("message", "Dine to passwords matcher ikke! Prøv igen");
            ctx.render("creategroup.html");


    }

    private static void logout(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }


    public static void login(Context ctx)
    {

        String username = ctx.formParam("username");
        String password = ctx.formParam("password");


    }
}
