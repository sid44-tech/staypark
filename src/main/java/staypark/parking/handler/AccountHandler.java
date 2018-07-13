package staypark.parking.handler;

import staypark.parking.beans.AffiliateRegister;
import staypark.parking.beans.UserRegister;
import staypark.parking.util.DBUtil;
import staypark.parking.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

@Service
@Slf4j
public class AccountHandler {
    private String CHECK_USERNAME_EXISTS =
            "select * from " +
            "( " +
            "select gp_username as username, null as email, 'ADMN' as type " +
            "from dbstaypark.gp_admin_login " +
            "union " +
            "select null as username, gal_email as email, 'AFFL' as type " +
            "from dbstaypark.gp_affiliate_login " +
            "union " +
            "select username, email, 'CALL' as type " +
            "from dbstaypark.gp_callcenter_user " +
            "union " +
            "select username, email, 'USER' as type " +
            "from dbstaypark.gp_user_register " +
            ") as all_users " +
            "where username = ? ";
    private String CHECK_EMAIL_EXISTS =
            "select * from " +
            "( " +
            "select gp_username as username, null as email, 'ADMN' as type " +
            "from dbstaypark.gp_admin_login " +
            "union " +
            "select null as username, gal_email as email, 'AFFL' as type " +
            "from dbstaypark.gp_affiliate_login " +
            "union " +
            "select username, email, 'CALL' as type " +
            "from dbstaypark.gp_callcenter_user " +
            "union " +
            "select username, email, 'USER' as type " +
            "from dbstaypark.gp_user_register " +
            ") as all_users " +
            "where email = ? ";
    private String ADD_AFFILIATE =
            "insert into dbstaypark.gp_affiliate_login " +
            "(gal_com_name,gal_con_name,gal_phone,gal_hotel_name,gal_state_id,gal_city_id,gal_add,gal_logo,gal_no_room,gal_prk_sps,gal_email,gal_s_email,gal_password,gal_desc,gal_status,gal_area,gal_billing_email1,gal_billing_email2,gal_faclty_name,gal_bill_address,chkemail,gal_time_zone,gal_cdt_time,gal_netpark,gal_confirmation_type,gal_netpark_id,gal_country,gal_fax_number,gal_fax_type,gal_currency) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '', ?, ?, ?, " +
            "'', '', '', '', '', 0, '', '', 0, '', 0, ?, '', '', 0) ";
    private String ADD_CUSTOMER =
            "insert into dbstaypark.gp_user_register " +
            "(username, password, firstname, lastname, occupation, gender, email, ph_num, address, state_id, city_id, zip, status) " +
            "values (?, ?, ?, ?, '', '', ?, ?, ?, ?, ?, ?, 'A') ";
    private String GET_PASSWORD = "select password from dbstaypark.gp_user_register where username= ? ";

    public boolean checkEmailExists(final String email) {
        boolean found = false;

        if (email == null || email.length() < 3) {
            return false;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(CHECK_EMAIL_EXISTS);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                found = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return found;
    }

    public boolean checkUsernameExists(final String username) {
        boolean found = false;

        if (username == null || username.length() < 2) {
            return false;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(CHECK_USERNAME_EXISTS);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                found = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return found;
    }

    public String createAffiliateAccount(AffiliateRegister affiliate){
        String success = createAffiliateDBRecord(affiliate);
        if (success.equals("true")) {
            String body = EmailUtil.AFFILIATE_CONFIRM_BODY;
            body = body.replace("[COMPANY_NAME]", affiliate.getCompany());
            body = body.replace("[CONTACT_NAME]", affiliate.getContact());
            body = body.replace("[EMAIL]", affiliate.getEmail());
            body = body.replace("[PHONE]", affiliate.getPhone());
            body = body.replace("[INFO]", affiliate.getInfo());
            try {
                EmailUtil.sendEmail(affiliate.getEmail(), "Affiliate Account Registration", body);
                EmailUtil.sendEmail(EmailUtil.AFFILIATE_EMAIL, "Affiliate Account Registration", body);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return success;
    }

    public String createCustomerAccount(UserRegister user) {
        String success = createCustomerDBRecord(user);
        if (success.equals("true")) {
            String body = EmailUtil.CUSTOMER_CONFIRM_BODY;
            body = body.replace("[NAME]", user.getFirstName());
            try {
                EmailUtil.sendEmail(user.getEmail(), "Customer Account Registration", body);
                EmailUtil.sendEmail(EmailUtil.AFFILIATE_EMAIL, "Customer Account Registration", body);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return success;
    }

    public String createCustomerDBRecord(UserRegister user){
        String success = "false";

        if (user == null) {
            return "false";
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(ADD_CUSTOMER);
            ps.setString(1, user.getUsername());
            ps.setString(2, encryptPassword(user.getPassword()));
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getAddress());
            ps.setString(8, user.getState());
            ps.setString(9, user.getCity());
            ps.setString(10, user.getZip());

            int updated = ps.executeUpdate();
            if (updated > 0) {
                success = "true";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return success;
    }

    public String createAffiliateDBRecord(AffiliateRegister affiliate){
        String success = "false";

        if (affiliate == null) {
            return "false";
        }

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(ADD_AFFILIATE);
            ps.setString(1, affiliate.getCompany());
            ps.setString(2, affiliate.getContact());
            ps.setString(3, affiliate.getPhone());
            ps.setString(4, affiliate.getHotel());
            ps.setInt(5, affiliate.getState());
            ps.setInt(6, affiliate.getCity());
            ps.setString(7, affiliate.getAddress());
            ps.setString(8, affiliate.getHotelLogo().getOriginalFilename());
            ps.setInt(9, affiliate.getRooms());
            ps.setInt(10, affiliate.getParking());
            ps.setString(11, affiliate.getEmail());
            ps.setString(12, encryptPassword(affiliate.getPassword()));
            ps.setString(13, affiliate.getInfo());
            ps.setString(14, "Active");
            ps.setInt(15, affiliate.getCountry());

            int updated = ps.executeUpdate();
            if (updated > 0) {
                success = "true";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return success;
    }

    public boolean validateCredentials(final String username, final String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        String hashedPassword = getHashedPassword(username);

        if (hashedPassword == null) {
            return false;
        }

        //Really really old school stored passwords.  And by that I mean plain text.  Wha?
        if (checkPasswordPlain(password, hashedPassword)) {
            return true;
        }
        //Old school stored passwords.  And by that I mean MD5.  Wha?
        else if (checkPasswordMD5(password, hashedPassword)) {
            return true;
        }
        //Stored passwords.  And by that I mean BlowFish.  Wha?
        else if (checkPasswordBlowfish(password, hashedPassword)) {
            return true;
        }
        //TODO: One day one-way bcrypt should be done.  Out of scope for now.

        return false;
    }

    private String getHashedPassword(final String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_PASSWORD);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return null;
    }

    private boolean checkPasswordBlowfish(final String realPassword, final String hashedPassword) {
        if (BCrypt.checkpw(realPassword, hashedPassword)) {
            System.out.println("It matches");
            return true;
        } else {
            System.out.println("It does not match");
            return false;
        }
    }

    private boolean checkPasswordMD5(final String realPassword, final String hashedPassword) {
        if (realPassword == null || hashedPassword == null) {
            return false;
        }

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
        String decodedString = new String(decodedBytes);
        if (decodedString.equals(realPassword)) {
            return true;
        }

        return false;
    }

    private boolean checkPasswordPlain(final String realPassword, final String hashedPassword) {
        if (realPassword == null || hashedPassword == null) {
            return false;
        }

        if (realPassword.equals(hashedPassword)) {
            return true;
        }

        return false;
    }

    private String encryptPassword(final String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
