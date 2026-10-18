import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private int nextOrderId = 1001;

    public Order placeOrder(User user, String address, Cart cart,
                            double deliveryFee, double discount, String payment) {
        Order o = new Order(nextOrderId++, user.getUsername(), address, cart.getRestaurant(),
                cart.getItems(), cart.getSubtotal(), deliveryFee, discount, payment);
        orders.add(o);
        return o;
    }

    /** Replaces all orders with the ones loaded from file. */
    public void load(List<Order> loaded) {
        orders.clear();
        orders.addAll(loaded);
        for (Order o : loaded) {
            nextOrderId = Math.max(nextOrderId, o.getId() + 1);
        }
    }

    public List<Order> getAll() {
        return orders;
    }

    public Order findById(int id) {
        for (Order o : orders) {
            if (o.getId() == id) return o;
        }
        return null;
    }

    public List<Order> findByUser(String username) {
        List<Order> list = new ArrayList<>();
        for (Order o : orders) {
            if (o.getUsername().equalsIgnoreCase(username)) list.add(o);
        }
        return list;
    }

    public List<Order> findByStatus(OrderStatus status) {
        List<Order> list = new ArrayList<>();
        for (Order o : orders) {
            if (o.getStatus() == status) list.add(o);
        }
        return list;
    }

    /** A customer can cancel only their own order while it is still pending. */
    public boolean cancelByCustomer(Order o, String username) {
        if (o == null || !o.getUsername().equalsIgnoreCase(username)) return false;
        if (o.getStatus() != OrderStatus.PENDING) return false;
        o.setStatus(OrderStatus.CANCELLED);
        return true;
    }

    /** Admin moves the order to the next status (pick-up step requires a rider name). */
    public boolean advance(Order o, String riderName) {
        if (o == null) return false;
        OrderStatus next = o.nextStatus();
        if (next == null) return false;
        if (next == OrderStatus.PICKED_UP) {
            if (riderName == null || riderName.trim().isEmpty()) return false;
            o.setRiderName(riderName.trim());
        }
        o.setStatus(next);
        return true;
    }

    public boolean cancelByAdmin(Order o) {
        if (o == null || o.isFinished()) return false;
        o.setStatus(OrderStatus.CANCELLED);
        return true;
    }
}