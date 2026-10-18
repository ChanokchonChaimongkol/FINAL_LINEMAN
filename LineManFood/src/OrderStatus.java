public enum OrderStatus {
    PENDING("Waiting for restaurant"),
    PREPARING("Preparing your food"),
    READY("Food ready, waiting for rider"),
    PICKED_UP("Picked up, on the way"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}