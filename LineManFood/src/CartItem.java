public class CartItem {
    private final MenuItem item;
    private int qty;

    public CartItem(MenuItem item, int qty) {
        this.item = item;
        this.qty = qty;
    }

    public MenuItem getItem() { return item; }
    public int getQty() { return qty; }
    public void addQty(int n) { this.qty += n; }
    public double getTotal() { return item.getPrice() * qty; }
}