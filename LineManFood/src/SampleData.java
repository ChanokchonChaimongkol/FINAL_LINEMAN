/** Sample restaurants (add or edit restaurants and menus here). */
public final class SampleData {
    private SampleData() {}

    public static void load(RestaurantService service) {
        Restaurant r1 = new Restaurant(1, "Mom's Kitchen", "Thai Food", 1.2);
        r1.addMenu("Basil Pork with Fried Egg", 60);
        r1.addMenu("Shrimp Fried Rice", 70);
        r1.addMenu("Tom Yum Goong", 120);
        r1.addMenu("Pad Thai with Fresh Shrimp", 80);

        Restaurant r2 = new Restaurant(2, "Burger Garage", "Fast Food", 3.5);
        r2.addMenu("Beef Cheeseburger", 149);
        r2.addMenu("French Fries", 59);
        r2.addMenu("Chicken Nuggets (6 pcs)", 79);
        r2.addMenu("Cola", 25);

        Restaurant r3 = new Restaurant(3, "Sakura Sushi", "Japanese", 5.8);
        r3.addMenu("Salmon Sushi (4 pcs)", 129);
        r3.addMenu("Tonkotsu Ramen", 189);
        r3.addMenu("Chicken Teriyaki Rice Bowl", 99);
        r3.addMenu("Iced Green Tea", 35);

        Restaurant r4 = new Restaurant(4, "Famous Bubble Tea", "Drinks & Desserts", 0.8);
        r4.addMenu("Bubble Milk Tea", 45);
        r4.addMenu("Matcha Green Tea", 55);
        r4.addMenu("Iced Cocoa", 50);

        service.add(r1);
        service.add(r2);
        service.add(r3);
        service.add(r4);
    }
}