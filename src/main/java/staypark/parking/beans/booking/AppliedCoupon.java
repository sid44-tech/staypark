package staypark.parking.beans.booking;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppliedCoupon {
    private BigDecimal discountAmount = new BigDecimal(0.00);
    private String message = "";
    private boolean couponValid = false;
}
