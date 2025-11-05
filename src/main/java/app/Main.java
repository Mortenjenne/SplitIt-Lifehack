package app;

import app.config.ThymeleafConfig;
import app.controllers.*;
import app.persistence.ConnectionPool;
import app.persistence.ExpenseMapper;
import app.persistence.GroupMapper;
import app.persistence.GroupMemberMapper;
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

        AccountService accountService = new AccountServiceImpl(groupMapper,groupMemberMapper);
        BalanceService balanceService = new BalanceServiceImpl(expenseMapper, groupMemberMapper);
        ExpenseService expenseService = new ExpenseServiceImpl(expenseMapper, groupMapper);

        SplitItGroupController splitItGroupController = new SplitItGroupController(accountService);
        SplitItExpenseController splitItExpenseController = new SplitItExpenseController(expenseService,balanceService,accountService);

        splitItGroupController.addRoutes(app);
        splitItExpenseController.addRoutes(app);
    }
}