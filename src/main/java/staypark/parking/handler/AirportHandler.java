package staypark.parking.handler;

import staypark.parking.beans.general.StateAirportBean;
import staypark.parking.beans.reservation.AirportBean;
import staypark.parking.beans.reservation.AirportDetailBean;
import staypark.parking.beans.reservation.BrowseAirports;
import staypark.parking.beans.user.AirportReservations;
import staypark.parking.beans.user.ReservtionDetails;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Service
@Slf4j
public class AirportHandler {
    private static Map<String, String> AIRPORT_CODE_TO_STATE = new HashMap<>();
    private static Map<String, String> AIRPORT_CODE_TO_NAME = new HashMap<>();
    private static List<BrowseAirports> AIRPORT_INFO_BY_STATE = null;
    
    private static String GET_AIRPORTS = "select cc_desc as airports from dbstaypark.gp_common_content where cc_type = 'Airport SEO Link Templates'";
    private static String GET_AIRPORTS_TO_STATE = "select d.gdl_location, d.gdl_scode, s.gps_state " +
            "from dbstaypark.gp_deperture_location as d " +
            "left join dbstaypark.gp_state as s on d.gdl_state_id = s.gps_id " +
            "where d.gdl_status = 'Active' ";
    private static String GET_AIRPORT_DETAILS = "select d.gdl_location, s.gps_title, d.gdl_des, d.gdl_logo, s.gps_state " +
            "from dbstaypark.gp_deperture_location as d " +
            "left join dbstaypark.gp_state as s on d.gdl_state_id = s.gps_id " +
            "where d.gdl_scode = ? ";
    private static String GET_CUSTOMER_RESERVATIONS =
           "select * from " +
           "(select order_id, gr_d_air, gr_d_loc, gr_payment, reservation_status, gr_reservation_date, 'P' as type " +
           "from dbstaypark.gp_reservation as res " +
           "left join dbstaypark.gp_user_register as u on u.id = res.user_id " +
           "where gr_p_id != 0 and user_id != 0 and u.username = ?" +

           "union " +

           "select bk_order_id, bk_loc_city, bk_inn_desc, bk_payment_status, bk_reservation_status, bk_rsvn_date, 'RP' as type " +
           "from dbstaypark.gp_hotel_parking_reservation as res " +
           "left join dbstaypark.gp_user_register as u on u.email = res.bk_email " +
           "where u.username = ?) as res " +

           "order by gr_reservation_date desc ";
    private static String GET_CUSTOMER_RESERVATION_DETAIL =
    		"select order_id, gr_d_air, gr_d_loc, gr_payment, reservation_status, gr_reservation_date, gr_d_time, gr_p_time, gr_b_fee, gr_s_fee, gr_s_tax, gr_g_total, " +
    		"gr_payment_type, s.gps_t_zone, gr_t_time, gr_h_time, loc_state, loc_city, gp_tracking_no, gr_add, gr_city, gr_state, gr_zip, loc_state, loc_city, 'P' as type, " +
    		"null as primaryInDate, null as primaryOutDate, null as secondaryInDate, null as secondaryOutDate, -1 as hotelDays " +
    		"from dbstaypark.gp_reservation as res " +
    		"left join dbstaypark.gp_user_register as u on u.id = res.user_id " +
    		"left join dbstaypark.gp_state as s on s.gps_state = res.loc_state " +
    		"where gr_p_id != 0 and user_id != 0 and u.username = ? and res.order_id = ? " +

    		"union " +

    		"select bk_order_id, bk_loc_city, bk_inn_desc, bk_payment_status, bk_reservation_status, bk_rsvn_date, bk_car_drop_off_date, " +
    		"bk_car_chkin_time, bk_base_rate, bk_drc_srv_tax, bk_drc_sl_tax, bk_total_cost, " +
    		"'Credit Card' as gr_payment_type, s.gps_t_zone, bk_car_days, bk_car_hours, bk_loc_state, bk_loc_city, bk_tracking_no, bk_address, bk_city, bk_prsn_state, " +
    		"bk_zip, bk_loc_state, bk_loc_city, 'RP' as type, " +
    		"bk_chkin_date as primaryInDate, bk_chkout_date as primaryOutDate, bk_se_en_htl_chkin_date as secondaryInDate, bk_bk_se_en_htl_chkout_date as secondaryOutDate, bk_days as hotelDays " +
    		"from dbstaypark.gp_hotel_parking_reservation as res " +
    		"left join dbstaypark.gp_user_register as u on u.email = res.bk_email " +
    		"left join dbstaypark.gp_state as s on s.gps_state = res.bk_loc_state " +
    		"where u.username = ? and res.bk_order_id = ? ";
    private static String GET_CUSTOMER_RESERVATION_DETAIL_AFFILIATE =
    		"select order_id, gr_d_air, gr_d_loc, gr_payment, reservation_status, gr_reservation_date, gr_d_time, gr_p_time, gr_b_fee, gr_s_fee, gr_s_tax, gr_g_total, " +
    		"gr_payment_type, s.gps_t_zone, gr_t_time, gr_h_time, loc_state, loc_city, gp_tracking_no, gr_add, gr_city, gr_state, gr_zip, loc_state, loc_city, 'P' as type, " +
    		"null as primaryInDate, null as primaryOutDate, null as secondaryInDate, null as secondaryOutDate, -1 as hotelDays " +
    		"from dbstaypark.gp_reservation as res " +
    		"left join dbstaypark.gp_user_register as u on u.id = res.user_id " +
    		"left join dbstaypark.gp_state as s on s.gps_state = res.loc_state " +
    		"where res.order_id = ? " +

    		"union " +

    		"select bk_order_id, bk_loc_city, bk_inn_desc, bk_payment_status, bk_reservation_status, bk_rsvn_date, bk_car_drop_off_date, " +
    		"bk_car_chkin_time, bk_base_rate, bk_drc_srv_tax, bk_drc_sl_tax, bk_total_cost, " +
    		"'Credit Card' as gr_payment_type, s.gps_t_zone, bk_car_days, bk_car_hours, bk_loc_state, bk_loc_city, bk_tracking_no, bk_address, bk_city, bk_prsn_state, " +
    		"bk_zip, bk_loc_state, bk_loc_city, 'RP' as type, " +
    		"bk_chkin_date as primaryInDate, bk_chkout_date as primaryOutDate, bk_se_en_htl_chkin_date as secondaryInDate, bk_bk_se_en_htl_chkout_date as secondaryOutDate, bk_days as hotelDays " +
    		"from dbstaypark.gp_hotel_parking_reservation as res " +
    		"left join dbstaypark.gp_user_register as u on u.email = res.bk_email " +
    		"left join dbstaypark.gp_state as s on s.gps_state = res.bk_loc_state " +
    		"where res.bk_order_id = ? ";
    private static String GET_AFF_ID_BY_P_INFO_ID = 
    		"select gpi_aff_id " + 
    		"gpi_aff_id " + 
    		"from dbstaypark.gp_parking_info " + 
    		"where gpi_id = ? ";

    static {
    	initializeAirportToStateMaps();
    }
    
    public List<BrowseAirports> getBrowseAirportsPageInfo() {
    	if (AIRPORT_INFO_BY_STATE != null) {
    		return AIRPORT_INFO_BY_STATE;
    	}
    	
    	AIRPORT_INFO_BY_STATE = new ArrayList<>();
    	List<StateAirportBean> list = parseAirportsInStateString();
    	Collections.sort(list);
    	
    	String currentTitle = list.get(0).getTitle();
    	BrowseAirports ba = new BrowseAirports();
    	
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).getTitle().contentEquals(currentTitle)) {
    			AirportBean ab = new AirportBean();
    			ab.setCode(list.get(i).getCode());
    			ab.setSimpleText(list.get(i).getText());
    			ab.setName(AIRPORT_CODE_TO_NAME.get(list.get(i).getCode()));
    			ab.setLink(list.get(i).getLink());
                ba.addAirport(ab);
    		} else {
                ba.setState(currentTitle);
                AIRPORT_INFO_BY_STATE.add(ba);

                ba = new BrowseAirports();

                AirportBean ab = new AirportBean();
                ab.setCode(list.get(i).getCode());
                ab.setSimpleText(list.get(i).getText());
                ab.setName(AIRPORT_CODE_TO_NAME.get(list.get(i).getCode()));
                ab.setLink(list.get(i).getLink());
                ba.addAirport(ab);

    			currentTitle = list.get(i).getTitle();
    		}
    	}
    	
    	return AIRPORT_INFO_BY_STATE;
    }

    public AirportDetailBean getAirportDetails(final String airportCode) {
        AirportDetailBean bean = new AirportDetailBean();

        if (airportCode == null || airportCode.trim().equals("") || airportCode.trim().length() > 3) {
            return bean;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AIRPORT_DETAILS);
            ps.setString(1, airportCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                bean.setImageLink("/images/airport-logo/" + rs.getString("gdl_logo"));
                bean.setText(Jsoup.parse(rs.getString("gdl_des")).text());
                bean.setTitle(rs.getString("gps_title"));
                bean.setAirport(rs.getString("gdl_location"));
                bean.setState(rs.getString("gps_state"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return bean;
    }

    public ReservtionDetails getCustomerReservationDetails(final String username, final String orderId, final boolean affiliate) {
        ReservtionDetails rd = new ReservtionDetails();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (affiliate) {
                ps = conn.prepareStatement(GET_CUSTOMER_RESERVATION_DETAIL_AFFILIATE);
                ps.setString(1, orderId);
                ps.setString(2, orderId);
            } else {
                ps = conn.prepareStatement(GET_CUSTOMER_RESERVATION_DETAIL);
                ps.setString(1, username);
                ps.setString(2, orderId);
                ps.setString(3, username);
                ps.setString(4, orderId);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
            	String bookingType = rs.getString("type");
                rd.setDepartingAirport(AirportUtil.parseDepartingLocation(rs.getString("gr_d_air")));
                rd.setDropLocation(AirportUtil.parseDropLocation(rs.getString("gr_d_loc")));
                rd.setOrderNumber(rs.getString("order_id"));
                rd.setReservationStatus(getReservationStatus(rs.getString("gr_payment"), rs.getString("reservation_status")));
                rd.setBookingDate(DateUtil.formatDate(rs.getDate("gr_reservation_date")));
               
                if (bookingType.equals("RP")) {
                    rd.setPickTime("");
                    rd.setDropTime(rs.getString("gr_d_time"));
                } else {
                    rd.setPickTime(rs.getString("gr_p_time") + " " + rs.getString("gps_t_zone"));
                    rd.setDropTime(rs.getString("gr_d_time") + " " + rs.getString("gps_t_zone"));
                }
                rd.setBaseRate(rs.getString("gr_b_fee"));
                rd.setServiceFee(rs.getString("gr_s_fee"));
                rd.setSalesTax(rs.getString("gr_s_tax"));
                rd.setTotalCharge(rs.getString("gr_g_total"));
                rd.setPaymentType(rs.getString("gr_payment_type"));
                rd.setState(rs.getString("loc_state"));
                rd.setCity(rs.getString("loc_city"));
                rd.setZip(rs.getString("gr_zip"));
                rd.setTrackingNumber(rs.getString("gp_tracking_no"));
                rd.setPaymentStatus(rs.getString("gr_payment"));
                rd.setBillingAddress(builAddress(rs.getString("gr_add"), rs.getString("gr_city"), rs.getString("gr_state"), rs.getString("gr_zip")));
                rd.setParkingTime(rs.getString("gr_t_time") + " day(s) ");
                rd.setBookingType(bookingType);
                rd.setPrimaryCheckInDate(DateUtil.formatDate(rs.getDate("primaryInDate")));
                rd.setPrimaryCheckoutDate(DateUtil.formatDate(rs.getDate("primaryOutDate")));
                if (rs.getString("secondaryInDate") == null) {
                	rd.setSecondaryCheckInDate("");
                } else {
                	rd.setSecondaryCheckInDate(rs.getString("secondaryInDate"));
                }
                rd.setSecondaryCheckoutDate(DateUtil.formatDate(rs.getDate("secondaryOutDate")));
                rd.setHotelDays(rs.getInt("hotelDays"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return rd;
    }

    public List<AirportReservations> getCustomerReservations(final String username) {
        List<AirportReservations> reservations = new ArrayList<>();

        if (username == null) {
            return reservations;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_CUSTOMER_RESERVATIONS);
            ps.setString(1, username);
            ps.setString(2, username);
            rs = ps.executeQuery();
            while (rs.next()) {
                AirportReservations res = new AirportReservations();

                res.setDepartingAirport(AirportUtil.parseDepartingLocation(rs.getString("gr_d_air")));
                res.setDetails("Details");
                res.setDropLocation(rs.getString("gr_d_loc"));
                res.setOrderNumber(rs.getString("order_id"));
                res.setReservationStatus(getReservationStatus(rs.getString("gr_payment"), rs.getString("reservation_status")));
                res.setOrderDate(DateUtil.formatDate(rs.getDate("gr_reservation_date")));
                res.setDetailLink("/user/reservations/" + rs.getString("order_id"));
                if (rs.getString("type").equals("P")) {
                    res.setBookingType("Parking Only");
                } else {
                    res.setBookingType("Room & Parking");
                }

                reservations.add(res);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return reservations;
    }
    
    public int getAffiliateIdByParkingId(final int pid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AFF_ID_BY_P_INFO_ID);
            ps.setInt(1, pid);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("gpi_aff_id");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }
        
        return -1;
    }

    private String builAddress(final String address, final String city, final String state, final String zip) {
        return address + ", " + city + ", " + state + "  " + zip;
    }
    
    private String getReservationStatus(final String paymentStatus, final String reservationStatus) {
        if ("Notpaid".equals(paymentStatus)) {
            return "Pending";
        } else if ("A".equals(reservationStatus)) {
            return "Booked";
        }
    	
    	return "Booked";
    }

    private List<StateAirportBean> parseAirportsInStateString() {
        String str = getAirportsInStateInfoFromDB();

        List<StateAirportBean> beans = new ArrayList<StateAirportBean>();
        String[] lines = str.split("\\n");
        if (lines != null) {
            for (int i = lines.length-1; i >= 0; i--) {
                if (!lines[i].equals("<br/>\r")) {
                    String text = lines[i].replace(",<br/>", "");
                    text = Jsoup.parse(text).text();
                    String[] codeText = text.split(": ");

                    if (codeText.length > 1) {
                        StateAirportBean bean = new StateAirportBean();
                        bean.setCode(codeText[0]);
                        bean.setText(codeText[1].replace("##", codeText[0]));
                        bean.setLink("/airport/" + codeText[0]);
                        bean.setTitle(getStateFromAirportCode(codeText[0]) + " Airport Parking");
                        if (!getStateFromAirportCode(codeText[0]).trim().equals("")) {
                            beans.add(bean);
                        }
                    }
                }
            }
        }
        
        return beans;
    }

    private String getAirportsInStateInfoFromDB() {
        String dbList = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AIRPORTS);
            rs = ps.executeQuery();
            if (rs.next()) {
                dbList = rs.getString("airports");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return dbList;
    }

    private String getStateFromAirportCode(final String code) {
    	String airport = AIRPORT_CODE_TO_STATE.get(code);
        if (airport == null) {
        	return "";
        }
        
        return airport;
    }

    private static void initializeAirportToStateMaps() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_AIRPORTS_TO_STATE);
            rs = ps.executeQuery();
            while (rs.next()) {
            	AIRPORT_CODE_TO_STATE.put(rs.getString("gdl_scode"), rs.getString("gps_state"));
            	AIRPORT_CODE_TO_NAME.put(rs.getString("gdl_scode"), rs.getString("gdl_location"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }
    }
}
