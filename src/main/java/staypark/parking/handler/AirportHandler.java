package staypark.parking.handler;

import staypark.parking.beans.AirportBean;
import staypark.parking.beans.BrowseAirports;
import staypark.parking.beans.StateAirportBean;
import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    			ab.setName(list.get(i).getTitle());
    			//add airport to state list
    		} else {
    			//add a new item to master list
    			//new state found, create a new airport list and add airport to it
    			currentTitle = list.get(i).getTitle();
    		}
    	}
    	
    	return AIRPORT_INFO_BY_STATE;
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
                        bean.setLink("/" + getStateFromAirportCode(codeText[0]).replace(" ", "_") + "/" + codeText[0]);
                        bean.setTitle(getStateFromAirportCode(codeText[0]) + " Airport Parking");
                        beans.add(bean);
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
