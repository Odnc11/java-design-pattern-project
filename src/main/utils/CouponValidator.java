package main.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Validates coupon codes and returns their discount information.
 * 
 * Coupon types:
 *   - PERCENTAGE: percentage-based discount (e.g., WELCOME10 = 10%)
 *   - FIXED: fixed amount discount (e.g., INDIRIM20 = 20₺)
 *   - SHIPPING: free shipping coupon
 *   - FLASH: flash sale coupon (10%)
 */
public class CouponValidator {

    public enum CouponType { PERCENTAGE, FIXED, SHIPPING, FLASH }

    /**
     * Coupon information returned by validation.
     */
    public static class CouponInfo {
        private final String code;
        private final CouponType type;
        private final double value;
        private final String description;

        public CouponInfo(String code, CouponType type, double value, String description) {
            this.code = code;
            this.type = type;
            this.value = value;
            this.description = description;
        }

        public String getCode() { return code; }
        public CouponType getType() { return type; }
        public double getValue() { return value; }
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return description;
        }
    }

    private static final Map<String, CouponInfo> VALID_COUPONS = new HashMap<>();

    static {
        VALID_COUPONS.put("WELCOME10", new CouponInfo("WELCOME10", CouponType.PERCENTAGE, 10,
                "%10 hoşgeldin indirimi"));
        VALID_COUPONS.put("INDIRIM20", new CouponInfo("INDIRIM20", CouponType.FIXED, 20,
                "20₺ sabit indirim"));
        VALID_COUPONS.put("SUPER50", new CouponInfo("SUPER50", CouponType.FIXED, 50,
                "50₺ süper indirim"));
        VALID_COUPONS.put("CODE15", new CouponInfo("CODE15", CouponType.FIXED, 100,
                "100₺ özel indirim"));
        VALID_COUPONS.put("KARGO", new CouponInfo("KARGO", CouponType.SHIPPING, 0,
                "Ücretsiz kargo"));
        VALID_COUPONS.put("FLASH", new CouponInfo("FLASH", CouponType.FLASH, 10,
                "⚡ Flash Sale %10"));
    }

    private CouponValidator() {} // Prevent instantiation

    /**
     * Validate a coupon code.
     * @param code The coupon code to validate
     * @return CouponInfo if valid, null if invalid
     */
    public static CouponInfo validate(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        return VALID_COUPONS.get(code.trim().toUpperCase());
    }

    /**
     * Get all valid coupon codes (for tooltip display).
     */
    public static String getAllCouponsDisplay() {
        StringBuilder sb = new StringBuilder("Geçerli kuponlar: ");
        boolean first = true;
        for (Map.Entry<String, CouponInfo> entry : VALID_COUPONS.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append(" (").append(entry.getValue().getDescription()).append(")");
            first = false;
        }
        return sb.toString();
    }
}
