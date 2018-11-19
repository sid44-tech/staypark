package staypark.parking.handler;

import staypark.parking.beans.affiliate.AffiliateFinancialRecord;
import staypark.parking.beans.affiliate.AffiliateFinancials;
import staypark.parking.beans.affiliate.AffiliateRegister;
import staypark.parking.beans.affiliate.AffiliateRoom;
import staypark.parking.beans.reservation.AffiliateReservation;
import staypark.parking.beans.security.AffiliateProfile;
import staypark.parking.beans.security.AuthProfile;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.beans.user.UserRegister;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import staypark.parking.util.EmailUtil;
import staypark.parking.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

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
            "select gp_username as username, gp_password as password, 'ROLE_ADMN' as type, gp_username as fname " +
            "from dbstaypark.gp_admin_login " +
            "union " +
            "select gal_email as username, gal_password as password, 'ROLE_AFFL' as type, gal_con_name as fname " +
            "from dbstaypark.gp_affiliate_login " +
            "union " +
            "select username, password, 'ROLE_CALL' as type, name as fname " +
            "from dbstaypark.gp_callcenter_user " +
            "union " +
            "select username, password, 'ROLL_USER' as type, firstname as fname " +
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
    private String GET_AFFILIATE_PROFILE = "select * from dbstaypark.gp_affiliate_login where gal_email = ? ";
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
    
    private String UPDATE_AFFILIATE_PROFILE =
            "update dbstaypark.gp_affiliate_login set " +
            "gal_com_name = ?, " +
            "gal_con_name = ?, " +
            "gal_email = ?, " +
            "gal_phone = ?, " +
            "gal_desc = ? " +
            "where gal_email = ? ";
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
            "[RANGE_REPLACE] " +
            "order by res.res_date desc, res.id desc " +
            "limit 0, 50 ";

    private String GET_AFFILIATE_ID_FROM_USERNAME =
            "select gal_id " +
            "from dbstaypark.gp_affiliate_login " +
            "where gal_email = ? ";

    private String GET_AFF_USER_FOR_ORDER =
                    "select gal_email " +
                    "from dbstaypark.gp_reservation res " +
                    "left join dbstaypark.gp_affiliate_login aff on aff.gal_id = res.gr_aff_id " +
                    "where order_id = ? " +
                    "union " +
                    "select gal_email " +
                    "from dbstaypark.gp_hotel_parking_reservation res " +
                    "left join dbstaypark.gp_affiliate_login aff on aff.gal_id = res.bk_aff_id " +
                    "where bk_order_id = ? ";

    private static String GET_FINANCIALS_BY_AFFILIATE =
            "select * from (select gr_reservation_date as res_date, order_id as order_id, gr_fname as fname, gr_lname as lname, gr_b_fee as base_fee, gr_s_tax as sales_tax, 'P' as type " +
            "from dbstaypark.gp_reservation " +
            "where gr_aff_id = ? and gr_reservation_date [RANGE_REPLACE] " +
            "union " +
            "select bk_rsvn_date as res_date, bk_order_id as order_id, bk_first_name as fname, bk_last_name as lname, bk_base_rate as base_fee, bk_drc_sl_tax as sales_tax, bk_parking_type as type " +
            "from dbstaypark.gp_hotel_parking_reservation " +
            "where bk_aff_id = ? and bk_rsvn_date [RANGE_REPLACE] " +
            ") as temp " +
            "order by res_date desc ";

    private static String GET_FINANCIALS_FILTER_TOTAL =
            "select round(sum(gr_b_fee), 2) as base_fee_total, round(sum(gr_s_tax), 2) as sales_tax_total, 'P' as type " +
            "from dbstaypark.gp_reservation " +
            "where gr_aff_id = ? and gr_reservation_date [RANGE_REPLACE] " +
            "union " +
            "select round(sum(bk_base_rate), 2) as base_fee_total, round(sum(bk_drc_sl_tax), 2) as sales_tax_total, bk_parking_type as type " +
            "from dbstaypark.gp_hotel_parking_reservation " +
            "where bk_aff_id = ? and bk_rsvn_date [RANGE_REPLACE] " +
            "group by bk_parking_type ";

    private static String GET_FINANCIALS_ALL_TOTAL =
            "select round(sum(gr_b_fee), 2) as base_fee_total, round(sum(gr_s_tax), 2) as sales_tax_total, 'P' as type " +
            "from dbstaypark.gp_reservation " +
            "where gr_aff_id = ? " +
            "union " +
            "select round(sum(bk_base_rate), 2) as base_fee_total, round(sum(bk_drc_sl_tax), 2) as sales_tax_total, bk_parking_type as type " +
            "from dbstaypark.gp_hotel_parking_reservation " +
            "where bk_aff_id = ? " +
            "group by bk_parking_type ";

    private static String GET_AFFILIATE_ROOM_INFO =
            "select room.rm_type, room.rm_cost, room.rm_status " +
            "from dbstaypark.gp_affiliate_login as login " +
            "left join dbstaypark.gp_room as room on room.rm_aff_id = login.gal_id " +
            "where login.gal_email = ? ";

    private final static String PASSWORD_EMAIL =
            "You have submitted a request to reset your password.  Click the below link to reset your password. " +
                    "If you did not request this password reset, you can ignore this email. This link will expire in 24 hours.\n\n";


    private final static String UPDATE_PASSWORD_TOKEN =
            "replace into dbstaypark.gp_user_password_reset (EMAIL, TOKEN, EXPIRES) values (?, ?, NOW() + INTERVAL 24 HOUR)";
    private final static String GET_USER_PASSWORD_TOKEN =
            " select u.username from dbstaypark.gp_user_password_reset as upr " +
                    " left join dbstaypark.gp_user_register as u on upr.email = u.email " +
                    " where upr.token = ? ";
    private final static String DELETE_PASSWORD_TOKEN =
            "delete from dbstaypark.gp_user_password_reset where token = ? ";
    private final static String CHECK_PASSWORD_TOKEN =
            " select * from dbstaypark.gp_user_password_reset " +
                    " where token = ? and NOW() < expires ";
    private final static String PASSWORD_UPDATE = " update dbstaypark.gp_user_register "
            + " set password = ? where UPPER(username) = UPPER(?) ";


    public String resetPassword(final String email) {
        if (!checkEmailExists(email)) {
            return "This email address is not associated with a user.";
        }

        Connection conn = null;
        PreparedStatement ps = null;
        String token = UUID.randomUUID().toString();

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_PASSWORD_TOKEN);
            ps.setString(1, email);
            ps.setString(2, token);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "Error resetting password.";
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        String url = "https://www.staypark.com/setpassword?req=" + token;
        try {
            EmailUtil.sendEmail(email, "Password Reset", PASSWORD_EMAIL + url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "Success. Check email for further intstructions.";
    }

    public String changePasswordWithToken(final String password, final String token) {
        String username = getUsernameForToken(token);
        if (username == null) {
            return "Unable to change password. Please contact support.";
        }

        boolean success = changePasswordNoCheck(username, password);

        if (success) {
            clearPasswordToken(token);
            return "Password succesfully changed.";
        } else {
            return "Unable to change password.  Please contact support.";
        }
    }

    public boolean changePasswordNoCheck(final String username, final String password) {
        boolean success = false;
        String passwordHash = encryptPassword(password);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(PASSWORD_UPDATE);
            ps.setString(1, passwordHash);
            ps.setString(2, username);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                success = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return success;
    }

    private void clearPasswordToken(final String token) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(DELETE_PASSWORD_TOKEN);
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }
    }

    private String getUsernameForToken(final String token) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String username = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_USER_PASSWORD_TOKEN);
            ps.setString(1, token);
            rs = ps.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return username;
    }

    public String checkValidToken(final String token) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean validToken = false;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(CHECK_PASSWORD_TOKEN);
            ps.setString(1, token);
            rs = ps.executeQuery();
            if (rs.next()) {
                validToken = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        if (!validToken) {
            return "false";
        }

        return "true";
    }




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
    
    public AffiliateProfile getAffiliateProfile(final String username) {
        AffiliateProfile up = new AffiliateProfile();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AFFILIATE_PROFILE);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                up.setCompanyName(rs.getString("gal_com_name"));
                up.setContactName(rs.getString("gal_con_name"));
                up.setEmail(rs.getString("gal_email"));
                up.setPhone(rs.getString("gal_phone"));
                up.setCompanyInfo(rs.getString("gal_desc"));
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

            ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return up;
    }
    
    public AffiliateProfile updateAffiliateProfile(final String username, final AffiliateProfile up) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_AFFILIATE_PROFILE);
            ps.setString(1, up.getCompanyName());
            ps.setString(2, up.getContactName());
            ps.setString(3, up.getEmail());
            ps.setString(4, up.getPhone());
            ps.setString(5, up.getCompanyInfo());
            ps.setString(6, username);

            ps.executeUpdate();
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
        if (checkUsernameExists(user.getUsername()) || checkEmailExists(user.getEmail())) {
            return "false";
        }

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
        } else {
            profile.setAuthenticated(false);
            return profile;
        }
        //TODO: One day one-way bcrypt should be done.  Out of scope for now.

    }

    public  List<AffiliateReservation> getAffiliateReservations(final String username, final String range, final String startDate, final String endDate) {
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
            ps = conn.prepareStatement(GET_RESERVATIONS_FOR_AFFILIATE.replace("[RANGE_REPLACE]", getRangeWhereClause(range)));
            ps.setInt(1, affId);
            ps.setInt(2, affId);
            if (range != null && range.equals("SELECT")) {
                ps.setDate(3, new java.sql.Date(DateUtil.stringMDYToDate(startDate).getTime()));
                ps.setDate(4, new java.sql.Date(DateUtil.stringMDYToDate(endDate).getTime()));
            }
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
                aff.setRange(range);

                list.add(aff);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return list;
    }

    public boolean checkAffiliateCanAccessOrder(final String orderId, final String username) {
        String affUsername = getAffiliateIdFromOrder(orderId);
        if (affUsername == null || !username.equals(username)) {
            return false;
        }

        return true;
    }

    public AffiliateFinancials getAffiliateFinancial(final String username, final String range, final String type) {
        AffiliateFinancials financials = new AffiliateFinancials();

        PreparedStatement ps = null;
        ResultSet rs = null;

        int affId = getAffiliateIdFromUsername(username);
        if (affId == -1) {
            return financials;
        }

        String whereClauseRange = getRangeWhereClauseForFinancials(range);

        Connection conn = DBUtil.getConnection();
        try {
            ps = conn.prepareStatement(GET_FINANCIALS_BY_AFFILIATE.replace("[RANGE_REPLACE]", whereClauseRange));
            ps.setInt(1, affId);
            ps.setInt(2, affId);
            rs = ps.executeQuery();
            while (rs.next()) {
                AffiliateFinancialRecord f = new AffiliateFinancialRecord();
                f.setBaseFee(rs.getString("base_fee") + " USD");
                f.setSalesTax(rs.getString("sales_tax") + " USD");
                f.setCustomerName(rs.getString("fname") + " " + rs.getString("lname"));
                f.setOrderNumber(rs.getString("order_id"));
                f.setReservationDate(DateUtil.formatDate(rs.getDate("res_date")));
                f.setType(getFinancialRecordType(rs.getString("type")));
                if (checkIfValidFinancialType(type, f.getType())) {
                    financials.addDetailRecord(f);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(null, ps, rs);
        }

        financials.setAllTimeTotal(getAllTimeTotal(affId, conn));
        financials.setFilterTotal(getFilterTimeTotal(affId, range, type, conn));

        DBUtil.closeResources(conn);


        return financials;
    }

    public byte[] getFinancialReportSpreadsheet(final String username) {
        AffiliateFinancials financials = new AffiliateFinancials();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;

        int affId = getAffiliateIdFromUsername(username);
        if (affId == -1) {
            return null;
        }

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_FINANCIALS_BY_AFFILIATE.replace("[RANGE_REPLACE]", " IS NOT NULL "));
            ps.setInt(1, affId);
            ps.setInt(2, affId);
            rs = ps.executeQuery();
            while (rs.next()) {
                AffiliateFinancialRecord f = new AffiliateFinancialRecord();
                f.setBaseFee(rs.getString("base_fee") + " USD");
                f.setSalesTax(rs.getString("sales_tax") + " USD");
                f.setCustomerName(rs.getString("fname") + " " + rs.getString("lname"));
                f.setOrderNumber(rs.getString("order_id"));
                f.setReservationDate(DateUtil.formatDate(rs.getDate("res_date")));
                f.setType(getFinancialRecordType(rs.getString("type")));
                financials.addDetailRecord(f);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        StringBuffer sb = new StringBuffer("");
        sb.append("Reservation Day");
        sb.append(", ");
        sb.append("Year");
        sb.append(", ");
        sb.append("Order Number");
        sb.append(", ");
        sb.append("Customer Name");
        sb.append(", ");
        sb.append("Base Fee");
        sb.append(", ");
        sb.append("Sales Tax");
        sb.append(", ");
        sb.append("Type");
        sb.append("\n");
        for (AffiliateFinancialRecord fr : financials.getDetails()) {
            sb.append(fr.getReservationDate());
            sb.append(", ");
            sb.append(fr.getOrderNumber());
            sb.append(", ");
            sb.append(fr.getCustomerName());
            sb.append(", ");
            sb.append(fr.getBaseFee());
            sb.append(", ");
            sb.append(fr.getSalesTax());
            sb.append(", ");
            sb.append(fr.getType());
            sb.append("\n");
        }

        return sb.toString().getBytes();
    }

    public List<AffiliateRoom> getAffiliateRoomInfo(final String username) {
        List<AffiliateRoom> rooms = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AFFILIATE_ROOM_INFO);
            ps.setString(1, username);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("rm_type") != null) {
                    AffiliateRoom room = new AffiliateRoom();
                    room.setType(rs.getString("rm_type"));
                    room.setStatus(rs.getString("rm_status"));
                    room.setCost(rs.getBigDecimal("rm_cost").toString());
                    rooms.add(room);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return rooms;
    }

    private String getAllTimeTotal(final int affId, final Connection conn) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(GET_FINANCIALS_ALL_TOTAL);
            ps.setInt(1, affId);
            ps.setInt(2, affId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return String.format("%,.2f", rs.getBigDecimal("base_fee_total").setScale(2)) + " USD";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(null, ps, rs);
        }

        return "0.00 USD";
    }

    private String getFilterTimeTotal(final int affId, final String range, final String type, final Connection conn) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        BigDecimal runningTotal = new BigDecimal(0);

        try {
            String sql = GET_FINANCIALS_FILTER_TOTAL.replace("[RANGE_REPLACE]", getRangeWhereClauseForFinancials(range));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, affId);
            ps.setInt(2, affId);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getBigDecimal("base_fee_total") != null) {
                	if (checkIfValidFinancialType(type, getFinancialRecordType(rs.getString("type")))) {
                		runningTotal = runningTotal.add(rs.getBigDecimal("base_fee_total"));
                	}
                }
            }
            runningTotal = runningTotal.setScale(2);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(null, ps, rs);
        }

        return String.format("%,.2f", runningTotal.setScale(2)) + " USD";
    }

    private String getFinancialRecordType(final String dbType) {
        if (dbType == null) {
            return "Parking";
        } else if (dbType.equals("P")) {
            return "Parking Only";
        } else if (dbType.equals("start_trip") || dbType.equals("end_trip") || dbType.equals("start_and_end_trip"))  {
            return "Room and Park";
        } else if (dbType.equals("room_only")) {
            return "Room only";
        }

        return "Parking";
    }

    private boolean checkIfValidFinancialType(final String filterType, final String recordType) {
        if (filterType == null) {
            return false;
        } else if (filterType.equals("ALL")) {
            return true;
        } else if (filterType.equals("PARKING") && recordType.equals("Parking Only")) {
            return true;
        } else if (filterType.equals("ROOM") && recordType.equals("Room only")) {
            return true;
        } else if (filterType.equals("ROOMPARK") && recordType.equals("Room and Park")) {
            return true;
        }

        return false;
    }

    private String getRangeWhereClause(final String type) {
        if (type == null || type.isEmpty()) {
            return "where res_date > DATE_SUB(now(), INTERVAL 7 DAY)";
        } else if (type.equals("TODAY")) {
            return "where res_date = now()";
        } else if (type.equals("YESTERDAY")) {
            return "where res_date > DATE_SUB(now(), INTERVAL 1 DAY)";
        } else if (type.equals("PREVIOUS_WEEK")) {
            return "where res_date > DATE_SUB(now(), INTERVAL 7 DAY)";
        } else if (type.equals("PREVIOUS_MONTH")) {
            return "where res_date > DATE_SUB(now(), INTERVAL 1 MONTH)";
        } else if (type.equals("PREVIOUS_6_MONTHS")) {
            return "where res_date > DATE_SUB(now(), INTERVAL 6 MONTH)";
        } else if (type.equals("SELECT")) {
            return "where res_date >= ? and res_date <= ? ";
        }

        return "where res_date > DATE_SUB(now(), INTERVAL 7 DAY)";
    }

    private String getRangeWhereClauseForFinancials(final String type) {
        if (type == null || type.isEmpty()) {
            return " > DATE_SUB(now(), INTERVAL 7 DAY)";
        } else if (type.equals("TODAY")) {
            return " = now()";
        } else if (type.equals("YESTERDAY")) {
            return " > DATE_SUB(now(), INTERVAL 1 DAY)";
        } else if (type.equals("PREVIOUS_WEEK")) {
            return " > DATE_SUB(now(), INTERVAL 7 DAY)";
        } else if (type.equals("PREVIOUS_MONTH")) {
            return " > DATE_SUB(now(), INTERVAL 1 MONTH)";
        } else if (type.equals("PREVIOUS_CAL_MONTH")) {
            return " BETWEEN DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-01 00:00:00') " +
                    "AND DATE_FORMAT(LAST_DAY(NOW() - INTERVAL 1 MONTH), '%Y-%m-%d 23:59:59') ";
        } else if (type.equals("PREVIOUS_6_MONTHS")) {
            return " > DATE_SUB(now(), INTERVAL 6 MONTH)";
        } else if (type.equals("SELECT")) {
            return " >= ? and res_date <= ? ";
        }

        return " > DATE_SUB(now(), INTERVAL 7 DAY)";
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

    private String createCustomerDBRecord(UserRegister user) {
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

        return profile;
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

    private String getAffiliateIdFromOrder(final String order) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AFF_USER_FOR_ORDER);
            ps.setString(1, order);
            ps.setString(2, order);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("gal_email");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return null;
    }
}