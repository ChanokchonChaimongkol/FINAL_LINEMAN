import java.util.ArrayList;
import java.util.List;

public class RestaurantService {
    private final List<Restaurant> restaurants = new ArrayList<>();
    private int nextId = 1;

    public void add(Restaurant r) {
        restaurants.add(r);
        nextId = Math.max(nextId, r.getId() + 1);
    }

    /** ID for the next new restaurant. */
    public int nextId() {
        return nextId;
    }

    /** Restores the ID counter when loading from file (never goes backwards). */
    public void setNextId(int value) {
        nextId = Math.max(nextId, value);
    }

    public boolean remove(Restaurant r) {
        return restaurants.remove(r);
    }

    /** All restaurants (including closed ones), for admin. */
    public List<Restaurant> getAll() {
        return restaurants;
    }

    /** Open restaurants only, for customers. */
    public List<Restaurant> getOpen() {
        List<Restaurant> list = new ArrayList<>();
        for (Restaurant r : restaurants) {
            if (r.isOpen()) list.add(r);
        }
        return list;
    }

    public Restaurant findById(int id) {
        for (Restaurant r : restaurants) {
            if (r.getId() == id) return r;
        }
        return null;
    }
}