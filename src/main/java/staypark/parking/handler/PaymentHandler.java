package staypark.parking.handler;

import staypark.parking.beans.payment.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import staypark.parking.beans.payment.Payment;

import java.math.BigDecimal;
import java.util.UUID;

import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.base.ApiOperationBase;
import net.authorize.api.controller.CreateTransactionController;

@Service
@Slf4j
public class PaymentHandler {

	//Live Authorize.net API Information
	private static String API_LOGIN_KEY = "";
	private static String API_TRANSACTION_KEY = "";
	//Test Authorize.net API Information
	//private static String key = "";

	//Blocked card names and email prefixes (with domain). Empty entries are ignored.
	private static final String[] BLOCKED_CC_NAMES = { "", "" };
	private static final String BLOCKED_EMAIL_PREFIX = "";
	private static final String BLOCKED_EMAIL_DOMAIN = "@gmail.com";

	private static boolean isBlocked(final String ccName, final String email) {
		if (ccName != null) {
			for (String name : BLOCKED_CC_NAMES) {
				if (!name.isEmpty() && ccName.toLowerCase().contains(name)) {
					return true;
				}
			}
		}
		return email != null && !BLOCKED_EMAIL_PREFIX.isEmpty()
				&& email.toLowerCase().startsWith(BLOCKED_EMAIL_PREFIX)
				&& email.toLowerCase().endsWith(BLOCKED_EMAIL_DOMAIN);
	}

	public Payment processPayment(final String currency, final String ccNumber, final String cvNumber, final String ccType,
			final String ccName, final String ccExpMonth, final String ccExpYear, final BigDecimal amount,
								  final String firstName, final String lastName, final String address, final String state, final String zip, final String email) {

		if (isBlocked(ccName, email)) {
			Payment payment = new Payment();
			payment.setSuccess(false);
			payment.setPaymentResponse("Unusual credit card activity detected, please contact customer support.");
			log.info("Unusual credit card activity detected, please contact customer support.");
			return payment;
		}

		PaymentResponse pr = doPayment(ccNumber, cvNumber, ccExpMonth, ccExpYear, amount, firstName, lastName, address, state, zip);
		Payment payment = new Payment();

		if (!pr.isSuccess()) {
			payment.setSuccess(false);
			payment.setPaymentResponse(pr.getResponseText());
			return payment;
		}

		payment.setSuccess(true);
		payment.setCardLastDigits(getLastFourCardNumber(ccNumber));
		payment.setCardType(ccType);
		payment.setPayerName(ccName);
		payment.setPaymentResponse(pr.getResponseText());
		payment.setPaymentType("Credit");
		payment.setTrackingNumber(pr.getTrackingNumber());
		payment.setTransactionKey(pr.getTransactionKey());
		
		return payment;
	}

	public PaymentResponse doPayment(final String cardNuber, final String cvv, final String expirationMonth, final String expirationYear, final BigDecimal amount,
									 final String firstName, final String lastName, final String address, final String state, final String zip) {
		PaymentResponse pr = new PaymentResponse();
		String tempId = UUID.randomUUID().toString();
		String refLog = "payRef=\"" + tempId + "\", ";
		String endpointLog = "endpoint=\"PaymentHandler.doPayment(..)\", ";
		String nameLog = "name=\"" + firstName + " " + lastName + "\", ";
		String amountLog = "amount=\"" + amount.toString() + "\"";
		log.info(refLog + endpointLog + nameLog + amountLog);

		//Common code to set for all requests
		ApiOperationBase.setEnvironment(Environment.PRODUCTION);

		MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType();
		merchantAuthenticationType.setName(API_LOGIN_KEY);
		merchantAuthenticationType.setTransactionKey(API_TRANSACTION_KEY);
		ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

		// Populate the payment data
		PaymentType paymentType = new PaymentType();
		CreditCardType creditCard = new CreditCardType();
		creditCard.setCardNumber(cardNuber);
		creditCard.setCardCode(cvv);
		String expiration = getTwoCharNumber(expirationMonth) + getTwoCharNumber(expirationYear);
		creditCard.setExpirationDate(expiration);
		paymentType.setCreditCard(creditCard);

		// Create the payment transaction request
		TransactionRequestType txnRequest = new TransactionRequestType();
		txnRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
		txnRequest.setPayment(paymentType);
		txnRequest.setAmount(amount);

		CustomerAddressType customerType = new CustomerAddressType();
		customerType.setFirstName(firstName);
		customerType.setLastName(lastName);
		customerType.setAddress(address);
		customerType.setState(state);
		customerType.setZip(zip);
		txnRequest.setBillTo(customerType);

		try {
			// Make the API Request
			CreateTransactionRequest apiRequest = new CreateTransactionRequest();
			apiRequest.setTransactionRequest(txnRequest);
			CreateTransactionController controller = new CreateTransactionController(apiRequest);
			controller.execute();

			CreateTransactionResponse response = controller.getApiResponse();

			if (response != null) {
				String resultLog = "resultCode=\"" + response.getMessages().getResultCode() + "\", ";
				String responseLog = "responseCode=\"" + response.getTransactionResponse().getResponseCode() + "\", ";

				if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
					TransactionResponse result = response.getTransactionResponse();
					if (result.getResponseCode().equals("1")) {
						pr.setSuccess(true);
						pr.setResponseText("Payment Successful");
						pr.setTrackingNumber(result.getTransId());
						pr.setTransactionKey(result.getResponseCode());
						String responseTextLog = "responseText=\"Payment Successful\"";
						log.info(refLog + resultLog + responseLog + responseTextLog);
					} else {
						pr.setSuccess(false);
						pr.setResponseText(getErrorFromResponse(result));
						String responseTextLog = "responseText=\"" + getErrorFromResponse(result) + "\"";
						log.info(refLog + resultLog + responseLog + responseTextLog);
					}
				} else {
					pr.setSuccess(false);
					pr.setResponseText(getErrorFromResponse(response.getTransactionResponse()));
					String responseTextLog = "responseText=\"" + getErrorFromResponse(response.getTransactionResponse()) + "\"";
					log.info(refLog + resultLog + responseLog + responseTextLog);
				}
			} else {
				log.info(refLog + "error=\"Payment is null\"");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return pr;
	}

	private String getErrorFromResponse(final TransactionResponse response) {
		if (response != null && response.getErrors() != null && response.getErrors().getError() != null && response.getErrors().getError().size() > 0) {
			return "Payment Error: " + response.getErrors().getError().get(0).getErrorText();
		}

		return "Payment Error: An error occurred processing the payment";
	}

	private String getTwoCharNumber(final String numberStr) {
		if (numberStr.length() == 1) {
			return "0" + numberStr;
		} else if (numberStr.length() == 4) {
			return numberStr.substring(2);
		} else {
			return numberStr;
		}
	}

	private String getLastFourCardNumber(final String cardNumber) {
		return "XXXX" + cardNumber.substring(cardNumber.length()-4);
	}
}
