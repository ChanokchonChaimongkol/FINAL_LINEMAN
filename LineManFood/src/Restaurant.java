import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private final int id;
    private String name;
    private String category;
    private double distanceKm;
    private boolean open = true;
    private final List<MenuItem> menu = new ArrayList<>();
    private int nextMenuId = 1;

    public Restaurant(int id, String name, String category, double distanceKm) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.distanceKm = distanceKm;
    }

    public MenuItem addMenu(String name, double price) {
        return addMenu(nextMenuId, name, price);
    }

    /** Adds an item with a specific ID (used when loading from file). */
    public MenuItem addMenu(int id, String name, double price) {
        MenuItem m = new MenuItem(id, name, price);
        menu.add(m);
        nextMenuId = Math.max(nextMenuId, id + 1);
        return m;
    }

    public boolean removeMenu(int menuId) {
        return menu.removeIf(m -> m.getId() == menuId);
    }

    public MenuItem findMenu(int menuId) {
        for (MenuItem m : menu) {
            if (m.getId() == menuId) return m;
        }
        return null;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public List<MenuItem> getMenu() { return menu; }
}