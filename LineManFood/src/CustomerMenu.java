import java.util.List;

/** Ordering menu for a logged-in customer. */
public class CustomerMenu {
    private final User user;
    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final PricingService pricing;
    private final AuthService auth;
    private final DataStore store;

    public CustomerMenu(User user, RestaurantService restaurantService,
                        OrderService orderService, PricingService pricing,
                        AuthService auth, DataStore store) {
        this.user = user;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.pricing = pricing;
        this.auth = auth;
        this.store = store;
    }

    public void run() {
        Cart cart = new Cart();
        boolean running = true;
        while (running) {
            System.out.println("\n[Hello, " + user.getFullName() + "]");
            System.out.println("  1. Browse restaurants");
            System.out.println("  2. Choose restaurant / add food to cart");
            System.out.println("  3. View cart");
            System.out.println("  4. Remove item from cart");
            System.out.println("  5. Checkout / place order");
            System.out.println("  6. Track my orders");
            System.out.println("  7. Cancel an order");
            System.out.println("  8. My profile (change address / password)");
            System.out.println("  0. Logout");

            switch (ConsoleUtil.readInt("Choose: ")) {
                case 1: ConsoleUtil.showRestaurants(restaurantService.getOpen(), pricing); break;
                case 2: addToCart(cart); break;
                case 3: ConsoleUtil.showCart(cart, pricing); break;
                case 4: removeFromCart(cart); break;
                case 5: checkout(cart); break;
                case 6: trackOrders(); break;
                case 7: cancelOrder(); break;
                case 8: editProfile(); break;
                case 0: running = false; break;
                default: System.out.println("  ! Invalid option");
            }
            store.save();
        }
        System.out.println("Logged out");
    }

    private void addToCart(Cart cart) {
        ConsoleUtil.showRestaurants(restaurantService.getOpen(), pricing);
        int rid = ConsoleUtil.readInt("Choose restaurant (ID, 0 = cancel): ");
        if (rid == 0) return;
        Restaurant r = restaurantService.findById(rid);
        if (r == null || !r.isOpen()) {
            System.out.println("  ! Restaurant not found or closed");
            return;
        }

        Restaurant current = cart.getRestaurant();
        if (current != null && current.getId() != r.getId()) {
            if (!ConsoleUtil.confirm("Your cart has items from another restaurant. Clear the cart?")) return;
            cart.clear();
        }

        while (true) {
            ConsoleUtil.showMenu(r);
            int mid = ConsoleUtil.readInt("Choose item (ID, 0 = done): ");
            if (mid == 0) break;
            MenuItem item = r.findMenu(mid);
            if (item == null) {
                System.out.println("  ! Item not found");
                continue;
            }
            int qty = ConsoleUtil.readInt("Quantity: ");
            if (qty <= 0) {
                System.out.println("  ! Quantity must be greater than 0");
                continue;
            }
            cart.add(r, item, qty);
            System.out.println("  + Added " + item.getName() + " x" + qty + " to cart");
        }
    }

    private void removeFromCart(Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty");
            return;
        }
        int i = 1;
        for (CartItem ci : cart.getItems()) {
            System.out.println("  " + (i++) + ". " + ci.getItem().getName() + " x" + ci.getQty());
        }
        int idx = ConsoleUtil.readInt("Remove item no. (0 = cancel): ");
        CartItem removed = cart.removeAt(idx - 1);
        if (removed != null) {
            System.out.println("  - Removed " + removed.getItem().getName());
        }
    }

    private void checkout(Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("  ! Your cart is empty");
            return;
        }
        if (!cart.getRestaurant().isOpen()) {
            System.out.println("  ! This restaurant is now closed. Your cart has been cleared");
            cart.clear();
            return;
        }
        ConsoleUtil.showCart(cart, pricing);

        double subtotal = cart.getSubtotal();
        double fee = pricing.calculateDeliveryFee(cart.getRestaurant());

        double discount;
        while (true) {
            String code = ConsoleUtil.readLine("Promo code (LINEMAN50 / FREEDEL / WELCOME10, leave blank if none): ");
            discount = pricing.calculateDiscount(code, subtotal, fee);
            if (discount >= 0) break;
            System.out.println("  ! Invalid code or conditions not met (LINEMAN50 requires a minimum of 200 THB)");
        }

        String address = ConsoleUtil.readLine("Deliver to (Enter = " + user.getAddress() + "): ");
        if (address.isEmpty()) address = user.getAddress();

        System.out.println("Payment method: 1. Cash  2. PromptPay  3. Credit card");
        String payment;
        switch (ConsoleUtil.readInt("Choose: ")) {
            case 2: payment = "PromptPay"; break;
            case 3: payment = "Credit card"; break;
            default: payment = "Cash";
        }

        double total = Math.max(0, subtotal + fee - discount);
        ConsoleUtil.line();
        System.out.println("  Food:       " + ConsoleUtil.baht(subtotal));
        System.out.println("  Delivery:   " + ConsoleUtil.baht(fee));
        System.out.println("  Discount:  -" + ConsoleUtil.baht(discount));
        System.out.println("  Total:      " + ConsoleUtil.baht(total));
        System.out.println("  Payment:    " + payment);
        System.out.println("  Deliver to: " + address);
        ConsoleUtil.line();

        if (!ConsoleUtil.confirm("Confirm order?")) {
            System.out.println("  Order cancelled");
            return;
        }
        Order o = orderService.placeOrder(user, address, cart, fee, discount, payment);
        cart.clear();
        System.out.println("  [OK] Order placed! Order number: #" + o.getId());
    }

    private void trackOrders() {
        List<Order> list = orderService.findByUser(user.getUsername());
        if (list.isEmpty()) {
            System.out.println("  You have no orders yet");
            return;
        }
        for (Order o : list) ConsoleUtil.printOrder(o);
    }

    private void cancelOrder() {
        int id = ConsoleUtil.readInt("Order number to cancel: ");
        Order o = orderService.findById(id);
        if (o == null || !o.getUsername().equalsIgnoreCase(user.getUsername())) {
            System.out.println("  ! Order not found");
        } else if (orderService.cancelByCustomer(o, user.getUsername())) {
            System.out.println("  Order #" + id + " has been cancelled");
        } else {
            System.out.println("  ! Cannot cancel (status: " + o.getStatus().getLabel() + ")");
        }
    }

    private void editProfile() {
        System.out.println("\n[My profile] Username: " + user.getUsername()
                + " | Address: " + user.getAddress());
        System.out.println("  1. Change address");
        System.out.println("  2. Change password");
        System.out.println("  0. Back");
        switch (ConsoleUtil.readInt("Choose: ")) {
            case 1: {
                String addr = ConsoleUtil.readLine("New address: ");
                if (addr.isEmpty()) {
                    System.out.println("  ! Address cannot be empty");
                } else {
                    user.setAddress(addr);
                    System.out.println("  Address updated");
                }
                break;
            }
            case 2: {
                String old = ConsoleUtil.readLine("Current password: ");
                if (!auth.verify(user, old)) {
                    System.out.println("  ! Current password is incorrect");
                    break;
                }
                String pw = ConsoleUtil.readLine("New password: ");
                try {
                    auth.changePassword(user, pw);
                    System.out.println("  Password changed");
                } catch (IllegalArgumentException e) {
                    System.out.println("  ! " + e.getMessage());
                }
                break;
            }
            default:
        }
    }
}