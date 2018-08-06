package staypark.parking.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Slf4j
public class EmailUtil {
    public static final String INFO_EMAIL = "";
    public static final String AFFILIATE_EMAIL = "";
    public static final String SUPPORT_EMAIL = "";
//    public static final String INFO_EMAIL = "info@staypark.com";
//    public static final String AFFILIATE_EMAIL = "support@staypark.com";
//    public static final String SUPPORT_EMAIL = "support@staypark.com";
    static final String FROM = "support@staypark.com";
    static final String FROMNAME = "StayPark Support";
    static final String SMTP_USERNAME = "";
    static final String SMTP_PASSWORD = "";
    static final String HOST = "";
    static final int PORT = 587;

    public static boolean sendEmail(final String to, final String subject, final String body) throws Exception {

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setContent(body, "text/html");

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try {
            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception ex) {
            log.error("The email was not sent.");
            log.error("Error message: " + ex.getMessage());
            return false;
        } finally {
            transport.close();
        }

        return true;
    }


    public static String CONTACT_US_BODY =
            "<table width='57%' cellpadding='0' cellspacing='3' style='border:1px solid #c3c3c3;'>" +
                    "<tr >" +
				"<td width='29' align='right' valign='middle'></td>" +
                        "<td></td>" +
                        "<td width='369'>Contact Details</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td align='right' valign='middle'></td>" +
                        "<td>Name </td>" +
                        "<td>[NAME]</td>" +
                        "</tr>" +
                        "<tr >" +
                        "<td></td>" +
                        "<td width='130' align='left' valign='middle'>Email Id </td>" +
                        "<td align='left' valign='top'>[EMAIL]</td>" +
                        "</tr>" +
                        "<tr >" +
                        "<td></td>" +
                        "<td align='left' valign='middle'>Phone :</td>" +
                        "<td align='left' valign='top'>[PHONE]</td>" +
                        "</tr>" +
                        "<tr >" +
                        "<td></td>" +
                        "<td align='left' valign='middle'>Comment:</td>" +
                        "<td align='left' valign='top'>[COMMENT]</td>" +
                        "</tr>" +
                "</table>";

    public static String AFFILIATE_CONFIRM_BODY =
            "<table width='57%' cellpadding='0' cellspacing='3' style='border:1px solid #c3c3c3;'>" +
            "<tr >" +
            "<td colspan=3>New Affiliate information</td>" +
            "</tr>" +
            "<tr>" +
            "<td align='right' valign='middle'></td>" +
            "<td>Company Name </td>" +
            "<td>[COMPANY_NAME]</td>" +
            "</tr>" +
            "<tr>" +
            "<td align='right' valign='middle'></td>" +
            "<td>Name </td>" +
            "<td>[CONTACT_NAME]</td>" +
            "</tr>" +
            "<tr >" + "<td></td>" +
            "<td width='130' align='left' valign='middle'>Email Id </td>" +
            "<td align='left' valign='top'>[EMAIL]</td>" +
            "</tr>" +
            "<tr >" +
            "<td></td>" +
            "<td align='left' valign='middle'>Phone :</td>" +
            "<td align='left' valign='top'>[PHONE]</td>" +
            "</tr>" +
            "<tr >" +
            "<td></td>" + "<td align='left' valign='middle'>Comment:</td>" +
            "<td align='left' valign='top'>[INFO]</td>" +
            "</tr>" +
            "</table>";

    public static String CUSTOMER_CONFIRM_BODY =
            "Dear [NAME], thank you for registering with <a href='staypark.com'>staypark.com</a>. You can login here with your email address or username <a href='https://www.staypark.com/login'>https://www.staypark.com/login</a>.";
    
    public static String CONFIRM_BOOKING_EMAIL = 
            "<table width='100%' style='border:#000000 solid' cellspacing='0' cellpadding='0'>" +
            "<tr>" +
            "<td><img src='http://www.staypark.com/images/logo.png' width='165' height='61' /></td>" +
            "<td></td>" +
            "</tr>" +
            "<tr>" +
            "<td  align='center' colspan='2' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'> [CONFIRMATION]</td>" +
            "</tr>" +
            "<tr>" +
            "<td  align='center' colspan='2' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'> </td>" +
            "</tr>" +
            "<tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Customer Name:</td>" +
            "<td>[FIRST_NAME] [LAST_NAME]</td><td></td>" +
            "</tr>" +
            "" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Facility Name:</td>" +
            "<td>[INN_NAME]</td>" +
            "</tr>" +
            "<tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "<td>[STATE]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Facility Address:</td>" +
            "  <td>[INN_ADDRESS]</td>" +
            "  </tr>" +
            "  <tr>" +
            "    <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "  " +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Facility Phone:</td>" +
            "  <td>[INN_PHONE]</td>" +
            "  </tr>" +
            "" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "  " +
            "  <tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Shuttle Frequency:</td>" +
            "<td>[SHUTTLE_INFO]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Drop Off Date/Time:</td>" +
            "<td>[DROP_TIME]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Pick Up Date/Time:</td>" +
            "<td>[PICK_UP_TIME]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Reservation Number:</td>" +
            "<td>[ORDER_ID]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Passengers:</td>" +
            "<td>[NUMBER_PASSENGERS]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Base Rate:</td>" +
            "<td>[BASE_RATE]</td>" +
            "</tr>" +
            "" +
            "" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Service Fee:</td>" +
            "<td>[SERVICE_FEE]</td>" +
            "</tr>" +
            "" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "[CONFIRM_ACCESS_FEE]" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Sales Tax:</td>" +
            "<td>[SALES_TAX]</td>" +
            "</tr>" +
            "[CONFIRM_COUPON_DISCOUNT]" +
            "<tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "<td></td>" +
            "</tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Grand Total:</td>" +
            "<td>[GRAND_TOTAL]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "[SPLIT_PAYMENT]" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Make:</td>" +
            "<td>[CAR_MAKE]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Model:</td>" +
            "<td>[CAR_MODEL]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>License:</td>" +
            "<td>[CAR_LICENSE_PLATE]</td>" +
            "</tr>" +
            "<tr>" +
            "<td></td>" +
            "<td></td>" +
            "</tr>" +
            "<tr>" +
            "<td colspan='2' width='100%'>StayPark Customer Service # 1-877-337-4377</td>" +
            "</tr>" +
            "<tr>" +
            "<td colspan='2' width='100%'><br/>" +
            "[EMAIL_FOOTER]" +
            "</td>" +
            "</tr>" +
            "<tr>" +
            "<td colspan='2' width='100%'></td>" +
            "</tr>" +
            "</table>" +
            "<br />" +
            "<font color='red'>Warm Regards</font><br />" +
            "<a href='www.staypark.com'>www.staypark.com</a><br />" +
            "<br />";

        public static String CONFIRM_ACCESS_FEE =
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Access Fee:</td>" +
            "<td>[AC_FEE]</td>" +
            "</tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "<td></td>" +
            "</tr>";
        public static String CONFIRM_COUPON_DISCOUNT =
            "<tr>" +
            "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "<td></td>" +
            "</tr>" +
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Discount:</td>" +
            "<td>[DISCOUNT_AMOUNT]</td>" +
            "</tr>";

        public static String SPLIT_PAYMENT =
            "<tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Total Due Now:</td>" +
            "<td>[TOTAL_DUE_NOW]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>" +
            " <tr>" +
            "<td width='40%' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Total Due At Lot:</td>" +
            "<td>[TOTAL_DUE_AT_LOT]</td>" +
            "</tr>" +
            "<tr>" +
            "  <td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:normal'></td>" +
            "  <td></td>" +
            "  </tr>";
        public static String EMAIL_FOOTER_DEFAULT =
            "Please print and bring the confirmation certificate with you to the parking facility. " +
            "Proceed inside to the front desk or attendant to Drop Off your vehicle.<br/><br/>Upon arrival back into town, " +
            "please call the parking facility from the Airport to inform them you are ready to be picked up by their shuttle and " +
            "transported back to their parking lot. <br/><br/>StayPark.com or their affiliates or any third party related " +
            "to our services are not responsible for any damages, loss, theft for any vehicle parked at our affiliates parking lots. " +
            "The customer assumes all responsibility. <br /><br /><span style='color:red;'>***NOTE: IF YOU LEAVE YOUR VEHICLE AT THE " +
            "FACILITY FOR LONGER THAN YOUR BOOKED RESERVATION DATES & YOU DON'T EXTEND YOUR BOOKING OR BOOK A NEW ONE FOR THE EXTRA " +
            "DAYS NEEDED, YOUR VEHICLE WILL BE TOWED BY THE FACILITY***</span><br /><br /><strong>PAYMENT HAS BEEN MADE TO STAYPARK. " +
            "NO ADDITIONAL FEES WILL BE COLLECTED UNLESS SPECIFIED OTHERWISE.</strong>";

    public static String CANCEL_BOOKING_EMAIL =
            "<table width='100%' style='border:#000000 solid' cellspacing='0' cellpadding='0'>" +
                    "<tr>" +
                    "<td><img src='http://www.staypark.com/images/logo.png' width='165' height='61' /></td>" +
                    "<td></td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td  align='center' colspan='2' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'> Cancellation for Reservation Number [ORDER_ID]</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td  align='center' colspan='2' style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'> </td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Customer Name:</td>" +
                    "<td>[NAME]</td><td></td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Departing Airport:</td>" +
                    "<td>[AIRPORT]</td><td></td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Location:</td>" +
                    "<td>[LOCATION]</td><td></td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Drop Off Time:</td>" +
                    "<td>[DROP_OFF_TIME]</td><td></td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px; font-weight:bold'>Pick Up Time:</td>" +
                    "<td>[PICK_UP_TIME]</td><td></td>" +
                    "</tr>" +
                    "</table>" +
                    "<br />" +
                    "<font color='red'>Warm Regards</font><br />" +
                    "<a href='www.staypark.com'>www.staypark.com</a><br />" +
                    "<br />";
}
