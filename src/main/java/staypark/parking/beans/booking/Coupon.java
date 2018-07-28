package staypark.parking.beans.booking;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Coupon {
    private String couponType;
    private String code;
    private String discountType;
    private String amount;

    public BigDecimal getCouponAmount() {
        if (amount != null && !amount.isEmpty()) {
            try {
                return new BigDecimal(amount).setScale(2);
            } catch (Exception e) {
                return new BigDecimal(0).setScale(2);
            }
        }

        return new BigDecimal(0).setScale(2);
    }
}
