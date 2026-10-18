/** คำนวณค่าส่งและส่วนลด (แก้กติการาคาที่ไฟล์นี้ที่เดียว) */
public class PricingService {
    public static final double BASE_FEE = 15.0;   // ค่าส่งเริ่มต้น
    public static final double FREE_KM = 2.0;     // ระยะทางที่รวมในค่าส่งเริ่มต้น
    public static final double FEE_PER_KM = 5.0;  // ค่าส่งต่อ กม. ที่เกิน

    public double calculateDeliveryFee(Restaurant r) {
        double extraKm = Math.max(0, r.getDistanceKm() - FREE_KM);
        return Math.ceil(BASE_FEE + extraKm * FEE_PER_KM);
    }

    /** คืนส่วนลด (บาท) ถ้าโค้ดใช้ไม่ได้คืน -1 */
    public double calculateDiscount(String code, double subtotal, double deliveryFee) {
        if (code == null || code.trim().isEmpty()) return 0;
        switch (code.trim().toUpperCase()) {
            case "LINEMAN50":
                return subtotal >= 200 ? 50 : -1;
            case "FREEDEL":
                return deliveryFee;
            case "WELCOME10":
                return Math.floor(subtotal * 0.10);
            default:
                return -1;
        }
    }
}