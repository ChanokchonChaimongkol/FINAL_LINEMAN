import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int id;
    private final String username;
    private final String address;
    private final Restaurant restaurant;
    private final List<CartItem> items;
    private final double subtotal;
    private final double deliveryFee;
    private final double discount;
    private final String paymentMethod;
    private final LocalDateTime createdAt;
    private OrderStatus status = OrderStatus.PENDING;
    private String riderName;

    /** New order (created now). */
    public Order(int id, String username, String address, Restaurant restaurant,
                 List<CartItem> items, double subtotal, double deliveryFee,
                 double discount, String paymentMethod) {
        this(id, username, address, restaurant, items, subtotal, deliveryFee,
                discount, paymentMethod, LocalDateTime.now());
    }

    /** Order restored from a saved file. */
    public Order(int id, String username, String address, Restaurant restaurant,
                 List<CartItem> items, double subtotal, double deliveryFee,
                 double discount, String paymentMethod, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.address = address;
        this.restaurant = restaurant;
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }

    /** Next status in the normal flow (null if finished). */
    public OrderStatus nextStatus() {
        switch (status) {
            case PENDING: return OrderStatus.PREPARING;
            case PREPARING: return OrderStatus.READY;
            case READY: return OrderStatus.PICKED_UP;
            case PICKED_UP: return OrderStatus.DELIVERED;
            default: return null;
        }
    }

    public boolean isFinished() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED;
    }

    public double getTotal() {
        return Math.max(0, subtotal + deliveryFee - discount);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedAtText() { return createdAt.format(FMT); }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getAddress() { return address; }
    public Restaurant getRestaurant() { return restaurant; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public double getSubtotal() { return subtotal; }
    public double getDeliveryFee() { return deliveryFee; }
    public double getDiscount() { return discount; }
    public String getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
}