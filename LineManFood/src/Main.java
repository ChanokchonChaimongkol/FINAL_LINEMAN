public class Main {
    public static void main(String[] args) {
        RestaurantService restaurantService = new RestaurantService();
        OrderService orderService = new OrderService();
        PricingService pricing = new PricingService();
        AuthService auth = new AuthService();

        // Load saved data (uses SampleData the first time), then write it back
        DataStore store = new DataStore("data", auth, restaurantService, orderService);
        store.load();
        store.save();

        System.out.println("============================================================");
        System.out.println("        Welcome to LineMan Food Ordering System ");
        System.out.println("============================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n[Start]");
            System.out.println("  1. Login");
            System.out.println("  2. Register");
            System.out.println("  0. Exit");

            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: {
                    User user = login(auth);
                    if (user != null) {
                        if (user.getRole() == Role.ADMIN) {
                            new AdminMenu(user, restaurantService, orderService, auth, store).run();
                        } else {
                            new CustomerMenu(user, restaurantService, orderService, pricing, auth, store).run();
                        }
                    }
                    break;
                }
                case 2: register(auth); break;
                case 0: running = false; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
        System.out.println("Thank you for using our service");
    }

    /** Up to 3 attempts. Returns null if login fails. */
    private static User login(AuthService auth) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String username = ConsoleUtil.readLine("Username: ");
            String password = ConsoleUtil.readLine("Password: ");
            try {
                User u = auth.login(username, password);
                System.out.println("  Login successful");
                return u;
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage() + " (attempt " + attempt + "/3)");
            }
        }
        System.out.println("  Too many failed attempts. Returning to start menu");
        return null;
    }

    private static void register(AuthService auth) {
        String username = ConsoleUtil.readLine("Username (a-z, 0-9, _ ; 4-20 characters): ");
        String password = ConsoleUtil.readLine("Password (at least 6 characters): ");
        String fullName = ConsoleUtil.readLine("Full name: ");
        String address = ConsoleUtil.readLine("Delivery address: ");
        try {
            User u = auth.register(username, password, fullName, address);
            System.out.println("  Registration successful! You can now login as " + u.getUsername());
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }
}