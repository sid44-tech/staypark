package staypark.parking.handler;

import staypark.parking.beans.AirportBean;
import staypark.parking.beans.AirportDetailBean;
import staypark.parking.beans.BrowseAirports;
import staypark.parking.beans.StateAirportBean;
import staypark.parking.beans.user.AirportReservations;
import staypark.parking.beans.user.ReservtionDetails;
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
            "select order_id, gr_d_air, gr_d_loc, gr_payment, reservation_status, gr_reservation_date " +
            "from dbstaypark.gp_reservation as res " +
            "left join dbstaypark.gp_user_register as u on u.id = res.user_id " +
            "where gr_p_id != 0 and user_id != 0 and u.username = ?" +
            "order by gr_reservation_date asc ";

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

    public ReservtionDetails getCustomerReservationDetails(final String username, final String orderId) {
        ReservtionDetails rd = new ReservtionDetails();

        rd.setOrderId("87878798");
        rd.setDepartingAirport("Airport Force 1");
        rd.setDepartingAirport("DIA");
        rd.setDropLocation("Suites Inn");
        rd.setDropTime("8:00 AM");
        rd.setPickTime("10:00 PM");
        rd.setBaseRate("34.00");
        rd.setServiceFee("5.00");
        rd.setSalesTax("2.50");
        rd.setTotalCharge("50.33");
        rd.setPaymentType("Credit");
        rd.setPaymentStatus("Paid");
        rd.setBookingDate("2018-03-14");
        rd.setParkingTime("9:00AM");
        rd.setState("CO");
        rd.setCity("Denver");
        rd.setTrackingNumber("90890890808098");
        rd.setBillingAddress("100 A. DD Place");
        rd.setReservationStatus("Booked");

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
            rs = ps.executeQuery();
            while (rs.next()) {
                AirportReservations res = new AirportReservations();

                res.setDepartingAirport(parseDepartingLocation(rs.getString("gr_d_air")));
                res.setDetails("Details");
                res.setDropLocation(rs.getString("gr_d_loc"));
                res.setOrderNumber(rs.getString("order_id"));
                res.setReservationStatus(getReservationStatus(rs.getString("gr_payment"), rs.getString("reservation_status")));
                res.setOrderDate(DateUtil.formatDate(rs.getDate("gr_reservation_date")));
                res.setDetailLink("/user/reservations/" + rs.getString("order_id"));

                reservations.add(res);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return reservations;
    }
    
    private String getReservationStatus(final String paymentStatus, final String reservationStatus) {
        if ("Notpaid".equals(paymentStatus)) {
            return "Pending";
        } else if ("A".equals(reservationStatus)) {
            return "Booked";
        }
    	
    	return "";
    }

    private String parseDepartingLocation(final String location) {
        if (location == null) {
            return "";
        }
        String depAirport = location.replace("�", " ");

        if (depAirport.indexOf("-") != -1) {
            depAirport = depAirport.split("-")[1].trim();
        }
        return depAirport;
    }
    
    private String parseDropLocation(final String dropLocation) {
    	String location = dropLocation;
    	
        if (location != null && location.indexOf("-") != -1) {
        	location = location.split("-")[0].trim();
        }
        
        return location;
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
