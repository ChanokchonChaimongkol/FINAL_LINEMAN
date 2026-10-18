import java.util.List;
import java.util.Scanner;

/** Keyboard input and terminal output helpers. */
public final class ConsoleUtil {
    private static final Scanner SC = new Scanner(System.in);

    private ConsoleUtil() {}

    // ---------- Input ----------

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.hasNextLine() ? SC.nextLine().trim() : "";
    }

    public static int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a valid number");
                if (!SC.hasNextLine()) return 0;
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                double v = Double.parseDouble(s);
                if (v >= 0) return v;
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  ! Please enter a valid number (not negative)");
            if (!SC.hasNextLine()) return 0;
        }
    }

    public static boolean confirm(String prompt) {
        return readLine(prompt + " (y/n): ").equalsIgnoreCase("y");
    }

    // ---------- Formatting ----------

    public static String baht(double v) {
        return String.format("%,.2f THB", v);
    }

    public static void line() {
        System.out.println("------------------------------------------------------------");
    }

    // ---------- Display ----------

    /** Restaurant list for customers. */
    public static void showRestaurants(List<Restaurant> list, PricingService pricing) {
        line();
        System.out.printf("%-4s %-26s %-22s %s%n", "ID", "Restaurant", "Category", "Distance / Delivery fee");
        line();
        if (list.isEmpty()) System.out.println("  (No restaurants open right now)");
        for (Restaurant r : list) {
            System.out.printf("%-4d %-26s %-22s %.1f km / %.0f THB%n",
                    r.getId(), r.getName(), r.getCategory(), r.getDistanceKm(),
                    pricing.calculateDeliveryFee(r));
        }
        line();
    }

    /** Restaurant list for admin (shows open/closed). */
    public static void showRestaurantsAdmin(List<Restaurant> list) {
        line();
        System.out.printf("%-4s %-26s %-20s %-10s %-8s %s%n",
                "ID", "Restaurant", "Category", "Distance", "Status", "Menu items");
        line();
        if (list.isEmpty()) System.out.println("  (No restaurants yet)");
        for (Restaurant r : list) {
            System.out.printf("%-4d %-26s %-20s %-10.1f %-8s %d%n",
                    r.getId(), r.getName(), r.getCategory(), r.getDistanceKm(),
                    r.isOpen() ? "Open" : "Closed", r.getMenu().size());
        }
        line();
    }

    public static void showMenu(Restaurant r) {
        line();
        System.out.println("Menu - " + r.getName());
        line();
        if (r.getMenu().isEmpty()) System.out.println("  (No menu items yet)");
        for (MenuItem m : r.getMenu()) {
            System.out.printf("  %-3d %-32s %8.2f THB%n", m.getId(), m.getName(), m.getPrice());
        }
        line();
    }

    public static void showCart(Cart cart, PricingService pricing) {
        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty");
            return;
        }
        line();
        System.out.println("Cart from " + cart.getRestaurant().getName());
        line();
        int i = 1;
        for (CartItem ci : cart.getItems()) {
            System.out.printf("  %d. %-28s x%-3d %9.2f THB%n",
                    i++, ci.getItem().getName(), ci.getQty(), ci.getTotal());
        }
        line();
        System.out.println("  Food:     " + baht(cart.getSubtotal()));
        System.out.println("  Delivery: " + baht(pricing.calculateDeliveryFee(cart.getRestaurant())));
        line();
    }

    public static void printOrder(Order o) {
        line();
        System.out.println("Order #" + o.getId() + " | " + o.getCreatedAtText()
                + " | Customer: " + o.getUsername());
        System.out.println("Restaurant: " + o.getRestaurant().getName());
        System.out.println("Deliver to: " + o.getAddress());
        for (CartItem ci : o.getItems()) {
            System.out.printf("   - %-28s x%d%n", ci.getItem().getName(), ci.getQty());
        }
        System.out.println("Total: " + baht(o.getTotal()) + " (" + o.getPaymentMethod() + ")");
        System.out.println("Status: " + o.getStatus().getLabel());
        if (o.getRiderName() != null) {
            System.out.println("Rider: " + o.getRiderName());
        }
        line();
    }
}