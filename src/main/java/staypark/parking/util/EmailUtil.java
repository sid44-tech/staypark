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
    public static final String INFO_EMAIL = "info@staypark.com";
    public static final String AFFILIATE_EMAIL = "support@staypark.com";
    //public static final String AFFILIATE_EMAIL = "affiliate@staypark.com";
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
				"<td width='29' align='right' valign='middle'>&nbsp;</td>" +
                        "<td></td>" +
                        "<td width='369'>Contact Details</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td align='right' valign='middle'>&nbsp;</td>" +
                        "<td>Name </td>" +
                        "<td>[NAME]</td>" +
                        "</tr>" +
                        "<tr >" +
                        "<td>&nbsp;</td>" +
                        "<td width='130' align='left' valign='middle'>Email Id </td>" +
                        "<td align='left' valign='top'>[EMAIL]</td>" +
                        "</tr>" +
                        "<tr >" +
                        "<td>&nbsp;</td>" +
                        "<td align='left' valign='middle'>Phone :</td>" +
                        "<td align='left' valign='top'>[PHONE]</td>" +
                        "</tr>" +
                        "<tr >" +
                        "<td>&nbsp;</td>" +
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
            "<td align='right' valign='middle'>&nbsp;</td>" +
            "<td>Company Name </td>" +
            "<td>[COMPANY_NAME]</td>" +
            "</tr>" +
            "<tr>" +
            "<td align='right' valign='middle'>&nbsp;</td>" +
            "<td>Name </td>" +
            "<td>[CONTACT_NAME]</td>" +
            "</tr>" +
            "<tr >" + "<td>&nbsp;</td>" +
            "<td width='130' align='left' valign='middle'>Email Id </td>" +
            "<td align='left' valign='top'>[EMAIL]</td>" +
            "</tr>" +
            "<tr >" +
            "<td>&nbsp;</td>" +
            "<td align='left' valign='middle'>Phone :</td>" +
            "<td align='left' valign='top'>[PHONE]</td>" +
            "</tr>" +
            "<tr >" +
            "<td>&nbsp;</td>" + "<td align='left' valign='middle'>Comment:</td>" +
            "<td align='left' valign='top'>[INFO]</td>" +
            "</tr>" +
            "</table>";

    public static String CUSTOMER_CONFIRM_BODY =
            "Dear [NAME], thank you for registering with <a href='staypark.com'>staypark.com</a>. You can login here with your email address or username <a href='https://www.staypark.com/login'>https://www.staypark.com/login</a>.";
}
