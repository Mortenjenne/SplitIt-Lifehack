package app;

import app.config.ThymeleafConfig;
import app.controllers.*;
import app.persistence.*;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main 
{
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=public";
    private static final String DB = "split-it";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args)
    {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        }).start(7070);

        app.get("/", ctx -> ctx.render("index.html"));
        UserController.addRoutes(app);

        ExpenseMapper expenseMapper = new ExpenseMapper(connectionPool);
        GroupMapper groupMapper = new GroupMapper(connectionPool);
        GroupMemberMapper groupMemberMapper = new GroupMemberMapper(connectionPool);
        UserMapper userMapper = new UserMapper(connectionPool);

        AccountService accountService = new AccountServiceImpl(userMapper, groupMapper, groupMemberMapper);
        BalanceService balanceService = new BalanceServiceImpl(expenseMapper, groupMemberMapper);
        ExpenseService expenseService = new ExpenseServiceImpl(userMapper, expenseMapper, groupMapper);
        UserService userService = new UserServiceImpl(userMapper);

        UserController userController = new UserController(userService);
        GroupController splitItGroupController = new GroupController(accountService);
        ExpenseController splitItExpenseController = new ExpenseController(expenseService,balanceService,accountService);

        splitItGroupController.addRoutes(app);
        splitItExpenseController.addRoutes(app);
    }
}