import java.util.List;

/** Back-office menu for admin. */
public class AdminMenu {
    private final User admin;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final AuthService auth;
    private final DataStore store;

    //Username: admin
    //Password: admin123

    public AdminMenu(User admin, RestaurantService restaurantService,
                     OrderService orderService, AuthService auth, DataStore store) {
        this.admin = admin;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.auth = auth;
        this.store = store;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== ADMIN BACK OFFICE (" + admin.getFullName() + ") ==========");
            System.out.println("  1. Manage restaurants");
            System.out.println("  2. Manage menus");
            System.out.println("  3. Manage orders");
            System.out.println("  4. Manage users");
            System.out.println("  5. Sales report");
            System.out.println("  0. Logout");

            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: manageRestaurants(); break;
                case 2: manageMenu(); break;
                case 3: manageOrders(); break;
                case 4: manageUsers(); break;
                case 5: showReport(); break;
                case 0: running = false; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
        System.out.println("Logged out");
    }

    // ===================== Restaurants =====================

    private void manageRestaurants() {
        boolean back = false;
        while (!back) {
            System.out.println("\n[Manage restaurants]");
            System.out.println("  1. List all restaurants");
            System.out.println("  2. Add restaurant");
            System.out.println("  3. Edit restaurant");
            System.out.println("  4. Open / close restaurant");
            System.out.println("  5. Delete restaurant");
            System.out.println("  0. Back");
            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: ConsoleUtil.showRestaurantsAdmin(restaurantService.getAll()); break;
                case 2: addRestaurant(); break;
                case 3: editRestaurant(); break;
                case 4: toggleRestaurant(); break;
                case 5: deleteRestaurant(); break;
                case 0: back = true; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
    }

    private Restaurant pickRestaurant() {
        ConsoleUtil.showRestaurantsAdmin(restaurantService.getAll());
        int id = ConsoleUtil.readInt("Choose restaurant (ID, 0 = cancel): ");
        if (id == 0) return null;
        Restaurant r = restaurantService.findById(id);
        if (r == null) System.out.println("  ! Restaurant not found");
        return r;
    }

    private void addRestaurant() {
        String name = ConsoleUtil.readLine("Restaurant name: ");
        if (name.isEmpty()) {
            System.out.println("  ! Name cannot be empty");
            return;
        }
        String category = ConsoleUtil.readLine("Food category: ");
        double km = ConsoleUtil.readDouble("Distance from customer (km): ");
        Restaurant r = new Restaurant(restaurantService.nextId(), name,
                category.isEmpty() ? "General" : category, km);
        restaurantService.add(r);
        System.out.println("  + Added " + r.getName() + " (ID " + r.getId() + ")");
    }

    private void editRestaurant() {
        Restaurant r = pickRestaurant();
        if (r == null) return;
        System.out.println("(Leave blank to keep the current value)");
        String name = ConsoleUtil.readLine("New name [" + r.getName() + "]: ");
        if (!name.isEmpty()) r.setName(name);
        String category = ConsoleUtil.readLine("New category [" + r.getCategory() + "]: ");
        if (!category.isEmpty()) r.setCategory(category);
        String km = ConsoleUtil.readLine("New distance in km [" + r.getDistanceKm() + "]: ");
        if (!km.isEmpty()) {
            try {
                double d = Double.parseDouble(km);
                if (d >= 0) r.setDistanceKm(d);
                else System.out.println("  ! Distance cannot be negative (not changed)");
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid distance (not changed)");
            }
        }
        System.out.println("  Restaurant updated");
    }

    private void toggleRestaurant() {
        Restaurant r = pickRestaurant();
        if (r == null) return;
        r.setOpen(!r.isOpen());
        System.out.println("  " + r.getName() + " is now: " + (r.isOpen() ? "Open" : "Closed"));
    }

    private void deleteRestaurant() {
        Restaurant r = pickRestaurant();
        if (r == null) return;
        if (ConsoleUtil.confirm("Delete " + r.getName() + "?")) {
            restaurantService.remove(r);
            System.out.println("  - Restaurant deleted (past orders are kept in history)");
        }
    }

    // ===================== Menus =====================

    private void manageMenu() {
        Restaurant r = pickRestaurant();
        if (r == null) return;

        boolean back = false;
        while (!back) {
            System.out.println("\n[Manage menu: " + r.getName() + "]");
            System.out.println("  1. View menu");
            System.out.println("  2. Add item");
            System.out.println("  3. Edit item");
            System.out.println("  4. Delete item");
            System.out.println("  0. Back");
            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: ConsoleUtil.showMenu(r); break;
                case 2: addMenuItem(r); break;
                case 3: editMenuItem(r); break;
                case 4: deleteMenuItem(r); break;
                case 0: back = true; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
    }

    private void addMenuItem(Restaurant r) {
        String name = ConsoleUtil.readLine("Item name: ");
        if (name.isEmpty()) {
            System.out.println("  ! Name cannot be empty");
            return;
        }
        double price = ConsoleUtil.readDouble("Price (THB): ");
        r.addMenu(name, price);
        System.out.println("  + Item added");
    }

    private void editMenuItem(Restaurant r) {
        ConsoleUtil.showMenu(r);
        int id = ConsoleUtil.readInt("Edit item ID (0 = cancel): ");
        if (id == 0) return;
        MenuItem m = r.findMenu(id);
        if (m == null) {
            System.out.println("  ! Item not found");
            return;
        }
        System.out.println("(Leave blank to keep the current value)");
        String name = ConsoleUtil.readLine("New name [" + m.getName() + "]: ");
        if (!name.isEmpty()) m.setName(name);
        String price = ConsoleUtil.readLine("New price [" + m.getPrice() + "]: ");
        if (!price.isEmpty()) {
            try {
                double p = Double.parseDouble(price);
                if (p >= 0) m.setPrice(p);
                else System.out.println("  ! Price cannot be negative (not changed)");
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid price (not changed)");
            }
        }
        System.out.println("  Item updated");
    }

    private void deleteMenuItem(Restaurant r) {
        ConsoleUtil.showMenu(r);
        int id = ConsoleUtil.readInt("Delete item ID (0 = cancel): ");
        if (id == 0) return;
        System.out.println(r.removeMenu(id) ? "  - Item deleted" : "  ! Item not found");
    }

    // ===================== Orders =====================

    private void manageOrders() {
        boolean back = false;
        while (!back) {
            System.out.println("\n[Manage orders]");
            System.out.println("  1. List all orders");
            System.out.println("  2. List orders by status");
            System.out.println("  3. Advance order status");
            System.out.println("  4. Cancel order");
            System.out.println("  0. Back");
            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: printOrders(orderService.getAll()); break;
                case 2: showByStatus(); break;
                case 3: advanceOrder(); break;
                case 4: cancelOrder(); break;
                case 0: back = true; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
    }

    private void printOrders(List<Order> list) {
        if (list.isEmpty()) {
            System.out.println("  No orders");
            return;
        }
        for (Order o : list) ConsoleUtil.printOrder(o);
    }

    private void showByStatus() {
        OrderStatus[] all = OrderStatus.values();
        for (int i = 0; i < all.length; i++) {
            System.out.println("  " + (i + 1) + ". " + all[i].getLabel());
        }
        int idx = ConsoleUtil.readInt("Choose status: ");
        if (idx < 1 || idx > all.length) {
            System.out.println("  ! Invalid status");
            return;
        }
        printOrders(orderService.findByStatus(all[idx - 1]));
    }

    private void advanceOrder() {
        Order o = orderService.findById(ConsoleUtil.readInt("Order number: "));
        if (o == null) {
            System.out.println("  ! Order not found");
            return;
        }
        OrderStatus next = o.nextStatus();
        if (next == null) {
            System.out.println("  ! This order is already finished (" + o.getStatus().getLabel() + ")");
            return;
        }
        String rider = null;
        if (next == OrderStatus.PICKED_UP) {
            rider = ConsoleUtil.readLine("Rider name: ");
        }
        if (orderService.advance(o, rider)) {
            System.out.println("  Status updated to: " + o.getStatus().getLabel());
        } else {
            System.out.println("  ! Cannot update (a rider name is required at the pick-up step)");
        }
    }

    private void cancelOrder() {
        Order o = orderService.findById(ConsoleUtil.readInt("Order number to cancel: "));
        if (o == null) {
            System.out.println("  ! Order not found");
        } else if (!ConsoleUtil.confirm("Cancel order #" + o.getId() + "?")) {
            System.out.println("  Not cancelled");
        } else if (orderService.cancelByAdmin(o)) {
            System.out.println("  Order cancelled");
        } else {
            System.out.println("  ! Cannot cancel (order is already finished)");
        }
    }

    // ===================== Users =====================

    private void manageUsers() {
        boolean back = false;
        while (!back) {
            System.out.println("\n[Manage users]");
            System.out.println("  1. List all users");
            System.out.println("  2. Suspend / activate account");
            System.out.println("  3. Reset password");
            System.out.println("  0. Back");
            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: listUsers(); break;
                case 2: toggleUser(); break;
                case 3: resetPassword(); break;
                case 0: back = true; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
    }

    private void listUsers() {
        ConsoleUtil.line();
        System.out.printf("%-16s %-24s %-10s %-10s %s%n",
                "Username", "Full name", "Role", "Status", "Orders");
        ConsoleUtil.line();
        for (User u : auth.getAll()) {
            System.out.printf("%-16s %-24s %-10s %-10s %d%n",
                    u.getUsername(), u.getFullName(), u.getRole(),
                    u.isActive() ? "Active" : "Suspended",
                    orderService.findByUser(u.getUsername()).size());
        }
        ConsoleUtil.line();
    }

    private void toggleUser() {
        User u = auth.find(ConsoleUtil.readLine("Username: "));
        if (u == null) {
            System.out.println("  ! User not found");
        } else if (u == admin) {
            System.out.println("  ! You cannot suspend your own account");
        } else {
            u.setActive(!u.isActive());
            System.out.println("  Account " + u.getUsername() + " is now: "
                    + (u.isActive() ? "Active" : "Suspended"));
        }
    }

    private void resetPassword() {
        User u = auth.find(ConsoleUtil.readLine("Username: "));
        if (u == null) {
            System.out.println("  ! User not found");
            return;
        }
        String pw = ConsoleUtil.readLine("New password: ");
        try {
            auth.changePassword(u, pw);
            System.out.println("  Password for " + u.getUsername() + " has been changed");
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }

    // ===================== Sales report =====================

    private void showReport() {
        List<Order> all = orderService.getAll();
        double revenue = 0;
        int delivered = 0;
        for (Order o : all) {
            if (o.getStatus() == OrderStatus.DELIVERED) {
                revenue += o.getTotal();
                delivered++;
            }
        }

        ConsoleUtil.line();
        System.out.println("Sales report (delivered orders only)");
        ConsoleUtil.line();
        System.out.println("Total orders: " + all.size());
        for (OrderStatus s : OrderStatus.values()) {
            System.out.println("  - " + s.getLabel() + ": " + orderService.findByStatus(s).size());
        }
        System.out.println("Total sales: " + ConsoleUtil.baht(revenue) + " (" + delivered + " orders)");
        if (delivered > 0) {
            System.out.println("Average per order: " + ConsoleUtil.baht(revenue / delivered));
        }

        System.out.println("\nSales by restaurant:");
        for (Restaurant r : restaurantService.getAll()) {
            double sum = 0;
            int count = 0;
            for (Order o : all) {
                if (o.getStatus() == OrderStatus.DELIVERED && o.getRestaurant() == r) {
                    sum += o.getTotal();
                    count++;
                }
            }
            System.out.printf("  %-26s %3d orders  %s%n", r.getName(), count, ConsoleUtil.baht(sum));
        }
        ConsoleUtil.line();
    }
}