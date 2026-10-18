import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Shopping cart (one restaurant at a time). */
public class Cart {
    private Restaurant restaurant;
    private final List<CartItem> items = new ArrayList<>();

    public boolean isEmpty() { return items.isEmpty(); }
    public Restaurant getRestaurant() { return restaurant; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }

    public void add(Restaurant r, MenuItem item, int qty) {
        if (restaurant != null && restaurant.getId() != r.getId()) {
            throw new IllegalStateException("Cart already contains items from another restaurant");
        }
        restaurant = r;
        for (CartItem ci : items) {
            if (ci.getItem().getId() == item.getId()) {
                ci.addQty(qty);
                return;
            }
        }
        items.add(new CartItem(item, qty));
    }

    /** Remove by position (0-based). Returns null if the index is invalid. */
    public CartItem removeAt(int index) {
        if (index < 0 || index >= items.size()) return null;
        CartItem removed = items.remove(index);
        if (items.isEmpty()) restaurant = null;
        return removed;
    }

    public void clear() {
        items.clear();
        restaurant = null;
    }

    public double getSubtotal() {
        double sum = 0;
        for (CartItem ci : items) sum += ci.getTotal();
        return sum;
    }
}