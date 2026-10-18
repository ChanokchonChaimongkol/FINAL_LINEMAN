import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Saves and loads users, restaurants and orders as tab-separated text files. */
public class DataStore {
    private static final String USERS = "users.txt";
    private static final String RESTAURANTS = "restaurants.txt";
    private static final String ORDERS = "orders.txt";

    private final Path dir;
    private final AuthService auth;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public DataStore(String folder, AuthService auth,
                     RestaurantService restaurantService, OrderService orderService) {
        this.dir = Paths.get(folder);
        this.auth = auth;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    public String getFolderPath() {
        return dir.toAbsolutePath().toString();
    }

    // =============================== SAVE ===============================

    public void save() {
        try {
            Files.createDirectories(dir);
            writeAtomic(USERS, userLines());
            writeAtomic(RESTAURANTS, restaurantLines());
            writeAtomic(ORDERS, orderLines());
        } catch (IOException e) {
            System.out.println("  ! Could not save data: " + e.getMessage());
        }
    }

    private List<String> userLines() {
        List<String> lines = new ArrayList<>();
        for (User u : auth.getAll()) {
            lines.add(row("USER", u.getUsername(), u.getPasswordHash(), u.getFullName(),
                    u.getAddress(), u.getRole().name(), String.valueOf(u.isActive())));
        }
        return lines;
    }

    private List<String> restaurantLines() {
        List<String> lines = new ArrayList<>();
        lines.add(row("NEXTID", String.valueOf(restaurantService.nextId())));
        for (Restaurant r : restaurantService.getAll()) {
            lines.add(row("REST", String.valueOf(r.getId()), r.getName(), r.getCategory(),
                    String.valueOf(r.getDistanceKm()), String.valueOf(r.isOpen())));
            for (MenuItem m : r.getMenu()) {
                lines.add(row("MENU", String.valueOf(m.getId()), m.getName(),
                        String.valueOf(m.getPrice())));
            }
        }
        return lines;
    }

    private List<String> orderLines() {
        List<String> lines = new ArrayList<>();
        for (Order o : orderService.getAll()) {
            lines.add(row("ORDER", String.valueOf(o.getId()), o.getUsername(), o.getAddress(),
                    String.valueOf(o.getRestaurant().getId()), o.getRestaurant().getName(),
                    String.valueOf(o.getSubtotal()), String.valueOf(o.getDeliveryFee()),
                    String.valueOf(o.getDiscount()), o.getPaymentMethod(),
                    o.getCreatedAt().toString(), o.getStatus().name(),
                    o.getRiderName() == null ? "" : o.getRiderName()));
            for (CartItem ci : o.getItems()) {
                lines.add(row("ITEM", String.valueOf(ci.getItem().getId()), ci.getItem().getName(),
                        String.valueOf(ci.getItem().getPrice()), String.valueOf(ci.getQty())));
            }
        }
        return lines;
    }

    /** Writes to a temp file first, then replaces the real file. */
    private void writeAtomic(String name, List<String> lines) throws IOException {
        Path target = dir.resolve(name);
        Path tmp = dir.resolve(name + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // =============================== LOAD ===============================

    /** Loads everything. Uses SampleData when no restaurant file exists yet. */
    public void load() {
        loadUsers();
        loadRestaurants();   // must run before loadOrders
        loadOrders();
        System.out.println("Data folder: " + getFolderPath());
    }

    private void loadUsers() {
        List<String[]> rows = readRows(USERS);
        if (rows == null) return;
        List<User> list = new ArrayList<>();
        for (String[] f : rows) {
            try {
                if (!f[0].equals("USER") || f.length < 7) throw new IllegalArgumentException();
                User u = new User(f[1], f[2], f[3], f[4], Role.valueOf(f[5]));
                u.setActive(Boolean.parseBoolean(f[6]));
                list.add(u);
            } catch (RuntimeException e) {
                warn(USERS);
            }
        }
        auth.replaceAll(list);
    }

    private void loadRestaurants() {
        List<String[]> rows = readRows(RESTAURANTS);
        if (rows == null) {
            SampleData.load(restaurantService);
            return;
        }
        Restaurant current = null;
        int nextId = 1;
        for (String[] f : rows) {
            try {
                switch (f[0]) {
                    case "NEXTID":
                        nextId = Integer.parseInt(f[1]);
                        break;
                    case "REST":
                        current = null;
                        Restaurant r = new Restaurant(Integer.parseInt(f[1]), f[2], f[3],
                                Double.parseDouble(f[4]));
                        r.setOpen(Boolean.parseBoolean(f[5]));
                        restaurantService.add(r);
                        current = r;
                        break;
                    case "MENU":
                        if (current == null) throw new IllegalArgumentException();
                        current.addMenu(Integer.parseInt(f[1]), f[2], Double.parseDouble(f[3]));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } catch (RuntimeException e) {
                warn(RESTAURANTS);
            }
        }
        restaurantService.setNextId(nextId);
    }

    private void loadOrders() {
        List<String[]> rows = readRows(ORDERS);
        if (rows == null) return;
        List<Order> list = new ArrayList<>();
        String[] header = null;
        List<CartItem> items = new ArrayList<>();

        for (String[] f : rows) {
            if (f[0].equals("ORDER")) {
                addOrder(list, header, items);
                header = f;
                items = new ArrayList<>();
            } else if (f[0].equals("ITEM") && header != null) {
                try {
                    MenuItem m = new MenuItem(Integer.parseInt(f[1]), f[2], Double.parseDouble(f[3]));
                    items.add(new CartItem(m, Integer.parseInt(f[4])));
                } catch (RuntimeException e) {
                    warn(ORDERS);
                }
            } else {
                warn(ORDERS);
            }
        }
        addOrder(list, header, items);
        orderService.load(list);
    }

    private void addOrder(List<Order> list, String[] h, List<CartItem> items) {
        if (h == null) return;
        try {
            int restaurantId = Integer.parseInt(h[4]);
            Restaurant r = restaurantService.findById(restaurantId);
            if (r == null) {
                // restaurant was deleted: keep a read-only copy so the order history still shows it
                r = new Restaurant(restaurantId, h[5], "-", 0);
            }
            Order o = new Order(Integer.parseInt(h[1]), h[2], h[3], r, items,
                    Double.parseDouble(h[6]), Double.parseDouble(h[7]), Double.parseDouble(h[8]),
                    h[9], LocalDateTime.parse(h[10]));
            o.setStatus(OrderStatus.valueOf(h[11]));
            o.setRiderName(h[12].isEmpty() ? null : h[12]);
            list.add(o);
        } catch (RuntimeException e) {
            warn(ORDERS);
        }
    }

    // =============================== helpers ===============================

    /** Returns null if the file does not exist. Exits if it exists but cannot be read. */
    private List<String[]> readRows(String file) {
        Path p = dir.resolve(file);
        if (!Files.exists(p)) return null;
        try {
            List<String[]> rows = new ArrayList<>();
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) rows.add(split(line));
            }
            return rows;
        } catch (IOException e) {
            System.out.println("! Could not read " + p + ": " + e.getMessage());
            System.out.println("! Exiting to avoid overwriting your data.");
            System.exit(1);
            return null;
        }
    }

    private void warn(String file) {
        System.out.println("  ! Skipped a corrupted row in " + file);
    }

    private static String row(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('\t');
            sb.append(escape(fields[i] == null ? "" : fields[i]));
        }
        return sb.toString();
    }

    private static String[] split(String line) {
        String[] f = line.split("\t", -1);
        for (int i = 0; i < f.length; i++) f[i] = unescape(f[i]);
        return f;
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '\t': sb.append("\\t"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 't': sb.append('\t'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    default: sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}