package staypark.parking.handler;

import org.springframework.stereotype.Service;

import staypark.parking.beans.payment.Payment;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentHandler {
	
	public Payment processPayment(final String currency, final String ccNumber, final String cvNumber, final String ccType,
			final String ccName, final String ccExpMonth, final String ccExpYear) {
		Payment payment = new Payment();
		
		payment.setSuccess(true);
		payment.setCardLastDigits("XXXX4444");
		payment.setCardType("Visa");
		payment.setPayerName("Jim Jimmy");
		payment.setPaymentResponse("Successful Payment");
		payment.setPaymentType("Credit");
		payment.setTrackingNumber("89789798789");
		payment.setTransactionKey("1");
		
		return payment;
	}
}
