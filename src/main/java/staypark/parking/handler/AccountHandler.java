package staypark.parking.handler;

import staypark.parking.beans.AffiliateRegister;
import staypark.parking.beans.UserRegister;
import staypark.parking.beans.reservation.AffiliateReservation;
import staypark.parking.beans.security.AuthProfile;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.util.DBUtil;
import staypark.parking.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
    private String GET_USERNAME_AND_PASSWORD =
            "select * from " +
            "( " +
            "select gp_username as username, gp_password as password, 'ADMN' as type, gp_username as fname " +
            "from dbstaypark.gp_admin_login " +
            "union " +
            "select gal_email as username, gal_password as password, 'AFFL' as type, gal_con_name as fname " +
            "from dbstaypark.gp_affiliate_login " +
            "union " +
            "select username, password, 'CALL' as type, name as fname " +
            "from dbstaypark.gp_callcenter_user " +
            "union " +
            "select username, password, 'USER' as type, firstname as fname " +
            "from dbstaypark.gp_user_register " +
            ") as all_users " +
            "where username = ? ";
    private String ADD_AFFILIATE =
            "insert into dbstaypark.gp_affiliate_login " +
            "(gal_com_name,gal_con_name,gal_phone,gal_hotel_name,gal_state_id,gal_city_id,gal_add,gal_logo,gal_no_room,gal_prk_sps,gal_email,gal_s_email,gal_password,gal_desc,gal_status,gal_area,gal_billing_email1,gal_billing_email2,gal_faclty_name,gal_bill_address,chkemail,gal_time_zone,gal_cdt_time,gal_netpark,gal_confirmation_type,gal_netpark_id,gal_country,gal_fax_number,gal_fax_type,gal_currency) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '', ?, ?, ?, " +
            "'', '', '', '', '', 0, '', '', 0, '', 0, ?, '', '', 0) ";
    private String ADD_CUSTOMER =
            "insert into dbstaypark.gp_user_register " +
            "(username, password, firstname, lastname, occupation, gender, email, ph_num, address, state_id, city_id, zip, status) " +
            "values (?, ?, ?, ?, '', '', ?, ?, ?, ?, ?, ?, 'A') ";
    private String GET_USER_PROFILE = "select * from dbstaypark.gp_user_register where username = ? ";
    private String UPDATE_USER_PROFILE =
            "update dbstaypark.gp_user_register set " +
            "address = ?, " +
            "city_id = ?, " +
            "email = ?, " +
            "firstname = ?, " +
            "lastname = ?, " +
            "ph_num = ?, " +
            "state_id = ?, " +
            "zip = ? " +
            "where username = ? ";
    private String UPDATE_PASSWORD_CUSTOMER = "update dbstaypark.gp_user_register set password = ? where username = ? ";
    private String UPDATE_PASSWORD_AFFILIATE = "update dbstaypark.gp_affiliate_login set gal_password = ? where gal_email = ? ";

    private String GET_RESERVATIONS_FOR_AFFILIATE =
            "select * from ( " +
            "select gr_id as id, order_id as order_id, gr_reservation_date as res_date, gr_payment as pay_status, gr_email as email, concat(gr_fname, ' ',gr_lname) as owner_name, 'P' as type " +
            "from dbstaypark.gp_reservation " +
            "where gr_aff_id = ? " +
            "union " +
            "select bk_id as id , bk_order_id as order_id, bk_rsvn_date as res_date, bk_payment_status as pay_status, bk_email as email, owner_name as owner_name, 'RP' as type " +
            "from dbstaypark.gp_hotel_parking_reservation " +
            "where bk_aff_id = ?) as res " +
            //[RANGE_REPLACE]
            "order by res.id desc " +
            "limit 0, 50 ";


    private String GET_AFFILIATE_ID_FROM_USERNAME =
            "select gal_id " +
            "from dbstaypark.gp_affiliate_login " +
            "where gal_email = ? ";

    public UserProfile getUserProfile(final String username) {
        UserProfile up = new UserProfile();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_USER_PROFILE);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                up.setAddress(rs.getString("address"));
                up.setCity(rs.getString("city_id"));
                up.setEmail(rs.getString("email"));
                up.setFirstName(rs.getString("firstname"));
                up.setLastName(rs.getString("lastname"));
                up.setPhone(rs.getString("ph_num"));
                up.setState(rs.getString("state_id"));
                up.setZip(rs.getString("zip"));
                up.setUserId(rs.getInt("id"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return up;
    }

    public UserProfile updateUserProfile(final String username, final UserProfile up) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_USER_PROFILE);
            ps.setString(1, up.getAddress());
            ps.setString(2, up.getCity());
            ps.setString(3, up.getEmail());
            ps.setString(4, up.getFirstName());
            ps.setString(5, up.getLastName());
            ps.setString(6, up.getPhone());
            ps.setString(7, up.getState());
            ps.setString(8, up.getZip());
            ps.setString(9, username);

            int rowsUpdated = ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return up;
    }

    public boolean changePassword(final String username, final String currentPassword, final String newPassword) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean updated = false;

        if (username == null || currentPassword == null || newPassword == null) {
            return updated;
        }

        AuthProfile profile = validateCredentials(username, currentPassword);
        if (profile == null || !profile.isAuthenticated()) {
            return updated;
        }

        String query = "";
        if ("USER".equals(profile.getRole())) {
            query = UPDATE_PASSWORD_CUSTOMER;
        } else if ("AFFL".equals(profile.getRole())) {
            query = UPDATE_PASSWORD_AFFILIATE;
        } else {
            log.error("Unsupported change password user type");
        }

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, encryptPassword(newPassword));
            ps.setString(2, username);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                updated = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return updated;
    }

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

    public AuthProfile validateCredentials(final String username, final String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        AuthProfile profile = getAuthenticationInformation(username);

        if (profile.getPassword() == null) {
            return null;
        }

        //Really really old school stored passwords.  And by that I mean plain text.  Wha?
        if (checkPasswordPlain(password, profile.getPassword())) {
            profile.setAuthenticated(true);
            return profile;
        }
        //Old school stored passwords.  And by that I mean MD5.  Wha?
        else if (checkPasswordMD5(password, profile.getPassword())) {
            profile.setAuthenticated(true);
            return profile;
        }
        //Stored passwords.  And by that I mean BlowFish.  Wha?
        else if (checkPasswordBlowfish(password, profile.getPassword())) {
            profile.setAuthenticated(true);
            return profile;
        }
        //TODO: One day one-way bcrypt should be done.  Out of scope for now.

        return null;
    }

    public  List<AffiliateReservation> getAffiliateReservations(final String username, final String range) {
        List<AffiliateReservation> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int affId = getAffiliateIdFromUsername(username);
        if (affId == -1) {
            return list;
        }

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_RESERVATIONS_FOR_AFFILIATE);
            ps.setInt(1, affId);
            ps.setInt(2, affId);
            rs = ps.executeQuery();
            while (rs.next()) {
                AffiliateReservation aff = new AffiliateReservation();

                aff.setDate(rs.getString("res_date"));
                aff.setDetail("X");
                aff.setEmail(rs.getString("email"));
                aff.setName(rs.getString("owner_name"));
                aff.setOrder(rs.getString("order_id"));
                aff.setPayment(rs.getString("pay_status"));
                aff.setType(getBookingTypeText(rs.getString("type")));

                list.add(aff);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return list;
    }

    private String getBookingTypeText(final String type) {
        if (type == null) {
            return "Parking";
        } else if (type.equals("RP")) {
            return "Room and Park";
        } else {
            return "Parking";
        }
    }

    private String createCustomerDBRecord(UserRegister user){
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

    private String createAffiliateDBRecord(AffiliateRegister affiliate){
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

    private AuthProfile getAuthenticationInformation(final String username) {
        AuthProfile profile = new AuthProfile();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_USERNAME_AND_PASSWORD);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                profile.setUsername(rs.getString("username"));
                profile.setPassword(rs.getString("password"));
                profile.setRole(rs.getString("type"));
                profile.setFirstName(rs.getString("fname"));
                return profile;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return null;
    }

    private boolean checkPasswordBlowfish(final String realPassword, final String hashedPassword) {
        try {
            if (BCrypt.checkpw(realPassword, hashedPassword)) {
                return true;
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
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

    private int getAffiliateIdFromUsername(final String username) {
        int id = -1;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AFFILIATE_ID_FROM_USERNAME);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("gal_id");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return id;
    }
}
