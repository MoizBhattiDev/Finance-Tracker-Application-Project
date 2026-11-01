import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            FinanceTracker tracker = new FinanceTracker(Path.of("transactions.txt"));
            while (true) {
                System.out.println("\n===== FINANCE TRACKER =====");
                System.out.println("1. Add Income");
                System.out.println("2. Add Expense");
                System.out.println("3. View Transactions");
                System.out.println("4. View Summary");
                System.out.println("5. Spending by Category");
                System.out.println("6. Delete Transaction");
                System.out.println("0. Exit");
                String choice = ask("Choose an option: ");
                try {
                    switch (choice) {
                        case "1" -> add(tracker, Transaction.Type.INCOME);
                        case "2" -> add(tracker, Transaction.Type.EXPENSE);
                        case "3" -> showTransactions(tracker);
                        case "4" -> showSummary(tracker);
                        case "5" -> showCategories(tracker);
                        case "6" -> delete(tracker);
                        case "0" -> { System.out.println("Goodbye!"); return; }
                        default -> System.out.println("Please choose a valid option.");
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Invalid input: " + ex.getMessage());
                } catch (IOException ex) {
                    System.out.println("Could not save changes: " + ex.getMessage());
                    return;
                }
            }
        } catch (IOException ex) {
            System.out.println("Could not open transaction data: " + ex.getMessage());
        }
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static void add(FinanceTracker tracker, Transaction.Type type) throws IOException {
        String description = ask("Description: ");
        String category = ask("Category: ");
        BigDecimal amount = new BigDecimal(ask("Amount (e.g. 12.50): "));
        Transaction t = tracker.add(type, description, category, amount);
        System.out.println("Saved transaction #" + t.getId());
    }

    private static void showTransactions(FinanceTracker tracker) {
        if (tracker.getTransactions().isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : tracker.getTransactions()) {
            System.out.printf("#%d | %s | %s | %s | %s | $%s%n", t.getId(), t.getDate(),
                t.getType(), t.getDescription(), t.getCategory(), t.getAmount());
        }
    }

    private static void showSummary(FinanceTracker tracker) {
        System.out.println("Total Income:   $" + tracker.total(Transaction.Type.INCOME).setScale(2));
        System.out.println("Total Expenses: $" + tracker.total(Transaction.Type.EXPENSE).setScale(2));
        System.out.println("Balance:        $" + tracker.balance().setScale(2));
    }

    private static void showCategories(FinanceTracker tracker) {
        Map<String, BigDecimal> totals = tracker.expensesByCategory();
        if (totals.isEmpty()) System.out.println("No expenses yet.");
        else totals.forEach((category, amount) -> System.out.println(category + ": $" + amount));
    }

    private static void delete(FinanceTracker tracker) throws IOException {
        int id = Integer.parseInt(ask("Transaction ID to delete: "));
        System.out.println(tracker.delete(id) ? "Transaction deleted." : "Transaction not found.");
    }
}
