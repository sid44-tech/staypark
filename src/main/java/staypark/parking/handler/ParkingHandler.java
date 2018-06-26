package staypark.parking.handler;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import staypark.parking.beans.HotelInformation;
import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParkingHandler {
	private static String GET_PARKING_LOCATIONS = 
			"select p.*, d.*, c.gpc_id, c.gpc_state_id, c.gpc_city, c.gpc_status, s.gps_id, s.gps_state, s.gps_status " + 
			"from gp_parking_info as p " + 
			"left join gp_deperture_location as d on p.gpi_dep_id = d.gdl_id " + 
			"left join gp_city as c on d.gdl_city_id= c.gpc_id " + 
			"left join gp_state as s on d.gdl_state_id=s.gps_id " + 
			"where d.gdl_scode=? and p.gpi_status='Active' ";
	
	
	public void getParkingSearchResults(final String airportCode, final Date startDate, final Date endDate,
			final String startTime, final String endTime) {
		
	}
	
	
	private List<HotelInformation> getParkingLocations(final String airportCode) {
		List<HotelInformation> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_PARKING_LOCATIONS);
			ps.setString(1,  airportCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				HotelInformation hotel = new HotelInformation();
				hotel.setHotelTitle(rs.getString("gpi_inn_name"));
				hotel.setAirportDistance(rs.getDouble("gpi_distence"));
				hotel.setShuttleFrequency(rs.getString("gpi_shuttel"));
				hotel.setParkingDescription(rs.getString("gpi_desc"));
				hotel.setLatitude(rs.getBigDecimal("gpi_latitude"));
				hotel.setLongitude(rs.getBigDecimal("gpi_longitude"));
				hotel.setParkingCost(new BigDecimal(0.00));
				hotel.setParkingCostDescription("Based on some number of days");
				hotel.setSoldout(false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}
		
		return list;
	}
}
