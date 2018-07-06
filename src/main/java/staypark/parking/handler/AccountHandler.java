package staypark.parking.handler;

import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
}
