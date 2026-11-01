import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private final int id;
    private final LocalDate date;
    private final Type type;
    private final String description;
    private final String category;
    private final BigDecimal amount;

    public Transaction(int id, LocalDate date, Type type, String description, String category, BigDecimal amount) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive");
        if (date == null || type == null) throw new IllegalArgumentException("Date and type are required");
        if (description == null || description.isBlank() || category == null || category.isBlank())
            throw new IllegalArgumentException("Description and category are required");
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2)
            throw new IllegalArgumentException("Amount must be positive and have at most two decimal places");
        this.id = id;
        this.date = date;
        this.type = type;
        this.description = description.trim();
        this.category = category.trim();
        this.amount = amount.setScale(2);
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public Type getType() { return type; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
}
