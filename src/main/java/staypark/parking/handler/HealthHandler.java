package staypark.parking.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.stereotype.Service;

import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HealthHandler {
	private String PROBE = "select 1";

	public boolean dbProbe() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(PROBE);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}
		
		return false;
	}
}
