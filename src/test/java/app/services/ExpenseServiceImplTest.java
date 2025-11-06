package app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpenseServiceImplTest {

        private ExpenseServiceImpl expenseService;

        @BeforeEach
        void setUp() {
            expenseService = new ExpenseServiceImpl(null,null,null);
        }

        @Test
        void createExpenseWithEmptyDescription() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                expenseService.createExpense(1, 1, "", 100);
            });
            assertEquals("Description cannot be empty", ex.getMessage());
        }

        @Test
        void createExpenseWithNegativeAmount() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                expenseService.createExpense(1, 1, "Pizza", -50);
            });
            assertEquals("Amount cannot be below zero", ex.getMessage());
        }
    }
