package staypark.parking.handler;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import staypark.parking.util.AirportUtil;
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

	private static String GET_RESERVATION_COUNT_FOR_DAY_HOTEL =
			"select count(*) as count " +
			"from gp_reservation_dtls as d " +
			"WHERE d.rd_htl_id = ? and d.rd_st_date = ? ";
	
	
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
				hotel.setCurrencyType(AirportUtil.getCurrencyFromId(rs.getInt("gdl_country")));
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

	private int getReservationForDayCountbyHotel(final int hotelId, final Date date) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_RESERVATION_COUNT_FOR_DAY_HOTEL);
			ps.setInt(1,  hotelId);
			ps.setDate(2, new java.sql.Date(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt("count");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return count;
	}

	private void calculateParkingCost(final int hotelId, final Date dropDate, final String dropTime,
									  final Date pickupDate, final String pickupTime) {
		BigDecimal totalCost = new BigDecimal(0);
		String currencyCode = "$";
		int diffHours = 0;
		int diffDays = 1;
		//TODO: This calculation is probably not correct.  Need to validate.
		int totalDays = diffDays;
		if (diffHours > 0) {
			totalDays++;
		}

		for (int i = 0; i < totalDays; i++) {
			//TODO: Something must be done here.
			Date currentDate = new Date();

//			$date    = date('Y-m-d', strtotime("$DropDate +$a day"));
//			$day_nme = date("l", strtotime($date));
//			$htl_id  = $sql[$i]['gpi_aff_id'];

			int bookedCount = getReservationForDayCountbyHotel(hotelId, currentDate);

			$sp_date_book = getRecord(TABLE_RESERVATION_SPECIAL, $htl_id, "sp_htl_id", "*", "and sp_date='$date'", "", "", "", "", "");
			$sp_date      = $sp_date_book[0]['sp_date'];
			//

			$sp_day_book = getRecord(TABLE_RESERVATION_SPECIAL_DAY, $htl_id, "dy_htl_id", "*", "and dy_day='$day_nme'", "", "", "", "", "");
			$dy_day      = $sp_day_book[0]['dy_day'];


			if ($sp_date != "" && count($sp_date_book) > 0) {

				$sp_prk_cost = $sp_date_book[0]['sp_cost'];
				$sp_prk_no   = $sp_date_book[0]['sp_prk_no'];
				if ($sp_prk_no > $booking_cnt) {
					$totalcost += $sp_cost = $sp_date_book[0]['sp_cost'];
					$today = date("j-M-Y", strtotime($sp_date));
					$str0 .= "$today : $sp_prk_cost <br>";
				} else {
					$str0      = '';
					$str1      = '';
					$str2      = '';
					$totalcost = 0;
					break;
				}
			} else if ($dy_day != "" && count($sp_day_book) > 0) {

				$sp_prk_cost = $sp_day_book[0]['dy_cost'];
				$sp_prk_no   = $sp_day_book[0]['dy_room'];
				if ($sp_prk_no > $booking_cnt) {
					$totalcost += $sp_prk_cost;
					$today = date("j-M-Y", strtotime($date));
					$str1 .= "$today : $sp_prk_cost <br>";

				} else {
					$str0      = '';
					$str1      = '';
					$str2      = '';
					$totalcost = 0;
					break;
				}
			} else if ($sql[$i][gpi_pno] >= 0) {

				if ($sql[$i][gpi_pno] > $booking_cnt) {

					////Determine cost based on pricing structure
					if ($sql[$i][payment_type] == "Normal") {
						$sp_prk_cost = $sql[$i][gpi_feepday];
						$totalcost += $sp_prk_cost;

					} else {

						if ($total_days > 29) {
							if ($a < 30) {
								$sp_prk_cost = $sql[$i][dailyfee30];
								$dailyCost = $sp_prk_cost / $total_days;
							} else {
								$sp_prk_cost_extra += $sql[$i][dailyfee31];

							}
						} else {
							$sp_prk_cost_extra = 0;
							$sp_prk_cost = $sql[$i][dailyfee . ($total_days + 1)];

						}
						$totalcost =  $sp_prk_cost + $sp_prk_cost_extra;
						$dailyCost = "REPLACE_ME";
					}


					$today = date("j-M-Y", strtotime($date));
					$str2 .= "$today : $dailyCost <br>";
				}

				else {
					$str0      = '';
					$str1      = '';
					$str2      = '';
					$totalcost = 0;
					break;
				}


			}
		}
		//echo $diff_days;
		$gpi_cst    = $sql[$i][gpi_feepday];
		$pno        = $sql[$i][gpi_pno];
		$total_cost = $totalcost;
		$str3 .= "---------------------- <br>";
		$str4 .= "TOTAL : " . number_format($totalcost, 2) . " <br>";
		$str2 = str_replace("REPLACE_ME", number_format($totalcost/($total_days+1), 2), $str2);
	}
}
