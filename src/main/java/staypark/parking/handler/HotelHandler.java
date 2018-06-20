package staypark.parking.handler;

import staypark.parking.beans.RoomParkSearchResults;
import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HotelHandler {

    private static String GET_HOTELS_FOR_SEARCH =
            "select cost.*, deptloc.*, city.gpc_id, city.gpc_state_id, city.gpc_city, city.gpc_state_id, city.gpc_status, state.gps_id, state.gps_state, state.gps_status " +
            "from dbstaypark.gp_htl_day_range_cost as cost " +
            "left join  dbstaypark.gp_deperture_location as deptloc on cost.drc_gpi_dep_id = deptloc.gdl_id  " +
            "left join dbstaypark.gp_city as city on deptloc.gdl_city_id = city.gpc_id " +
            "left join dbstaypark.gp_state as state on deptloc.gdl_state_id = state.gps_id " +
            "where deptloc.gdl_scode = ? and cost.drc_status = 'Active'";

    //parm is drc_htl_id from above query
    private static String GET_HOTEL_IMAGES =
            "select * from dbstaypark.gp_parking_image " +
            "where gppi_p_id = ? " +
            "order by gppi_id desc ";

    //parm is drc_htl_id from above query'
    private static String GET_ROOMS_AVAILABLE =
            "select * from dbstaypark.gp_room " +
            "where rm_htl_id = ? and rm_status = 'Active' " +
            "order by rm_cost, rm_id ";

    public List<RoomParkSearchResults> getHotelslForSearch(final String airportCode) {
        List<RoomParkSearchResults> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_HOTELS_FOR_SEARCH);
            ps.setString(1, airportCode );
            rs = ps.executeQuery();
            while (rs.next()) {
                RoomParkSearchResults bean = new RoomParkSearchResults();
                bean.setHotelTitle(rs.getString("drc_gpi_inn_name") + " - " + rs.getString("gdl_logo_title"));
                bean.setCityState(rs.getString("gpc_city") + ", " + rs.getString("gps_state"));
                bean.setHotelText(rs.getString("drc_gpi_desc"));

                results.add(bean);
            }
        } catch (Exception e) {
            DBUtil.closeResources(conn, ps, rs);
            log.error(e.getMessage(), e);
        }

        return results;
    }
}
