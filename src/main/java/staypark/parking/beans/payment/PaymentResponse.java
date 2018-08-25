package staypark.parking.beans.payment;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String responseText;
    private String trackingNumber;
    private String transactionKey;
}
