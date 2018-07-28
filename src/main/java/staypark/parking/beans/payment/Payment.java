package staypark.parking.beans.payment;

import lombok.Data;

@Data
public class Payment {
	private boolean success = false;
	private String trackingNumber = "";
	private String transactionKey = "";
	private String paymentResponse = "";
	private String cardLastDigits = "";
	private String paymentType = "";
	private String payerName = "";
	private String cardType = "";

}
