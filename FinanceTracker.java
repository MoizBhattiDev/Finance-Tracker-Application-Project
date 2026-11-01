import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinanceTracker {
    private final List<Transaction> transactions = new ArrayList<>();
    private final Path storagePath;
    private int nextId = 1;

    public FinanceTracker(Path storagePath) throws IOException {
        this.storagePath = storagePath;
        load();
    }

    public Transaction add(Transaction.Type type, String description, String category, BigDecimal amount)
            throws IOException {
        Transaction transaction = new Transaction(nextId, LocalDate.now(), type, description, category, amount);
        transactions.add(transaction);
        nextId++;
        save();
        return transaction;
    }

    public boolean delete(int id) throws IOException {
        boolean removed = transactions.removeIf(t -> t.getId() == id);
        if (removed) save();
        return removed;
    }

    public List<Transaction> getTransactions() { return List.copyOf(transactions); }

    public BigDecimal total(Transaction.Type type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == type) sum = sum.add(t.getAmount());
        }
        return sum;
    }

    public BigDecimal balance() {
        return total(Transaction.Type.INCOME).subtract(total(Transaction.Type.EXPENSE));
    }

    public Map<String, BigDecimal> expensesByCategory() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.Type.EXPENSE) {
                totals.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }
        return totals;
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Transaction t : transactions) {
            lines.add(t.getId() + "\t" + t.getDate() + "\t" + t.getType() + "\t"
                + encode(t.getDescription()) + "\t" + encode(t.getCategory()) + "\t" + t.getAmount());
        }
        Files.write(storagePath, lines, StandardCharsets.UTF_8);
    }

    private void load() throws IOException {
        if (!Files.exists(storagePath)) return;
        int lineNumber = 0;
        for (String line : Files.readAllLines(storagePath, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) continue;
            try {
                String[] parts = line.split("\t", -1);
                if (parts.length != 6) throw new IllegalArgumentException("Wrong number of fields");
                Transaction t = new Transaction(Integer.parseInt(parts[0]), LocalDate.parse(parts[1]),
                    Transaction.Type.valueOf(parts[2]), decode(parts[3]), decode(parts[4]),
                    new BigDecimal(parts[5]));
                if (transactions.stream().anyMatch(existing -> existing.getId() == t.getId()))
                    throw new IllegalArgumentException("Duplicate transaction ID");
                transactions.add(t);
                nextId = Math.max(nextId, t.getId() + 1);
            } catch (RuntimeException ex) {
                throw new IOException("Invalid data in " + storagePath + " at line " + lineNumber, ex);
            }
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
