package staypark.parking.handler;

import staypark.parking.beans.StateAirportBean;
import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AirportHandler {
    private String GET_AIRPORTS = "select cc_desc as airports from dbstaypark.gp_common_content where cc_type = 'Airport SEO Link Templates'";
    private String GET_AIRPORTS_TO_STATE = "select d.gdl_location, d.gdl_scode, s.gps_state " +
            "from dbstaypark.gp_deperture_location as d " +
            "left join dbstaypark.gp_state as s on d.gdl_state_id = s.gps_id " +
            "where d.gdl_status = 'Active' ";

    public void parseAirportsInStateString() {
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
                        bean.setLink("/" + getStateFromAirportCode(codeText[0]) + "/" + codeText[0]);
                        beans.add(bean);
                    }
                }
            }
        }

        for (StateAirportBean bean : beans) {
            System.out.println(bean);
        }
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
        return "";
    }

    private static void buildAirportToStateMap() {
        //TODO: Add this method and lookup from it in the above method
    }
}
