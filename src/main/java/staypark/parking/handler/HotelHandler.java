package staypark.parking.handler;

import staypark.parking.beans.ImageLink;
import staypark.parking.beans.Room;
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
    private static String GET_ROOM_IMAGES =
            "select rm_timage, rm_limage " +
                    "from gp_room_image " +
                    "WHERE rm_id = ? ";

    //parm is drc_htl_id from above query'
    private static String GET_ROOMS_AVAILABLE =
            "select * " +
            "from dbstaypark.gp_room as room " +
            "left join dbstaypark.gp_room_image as image on image.rm_id = room.rm_id " +
            "where room.rm_htl_id = ? and room.rm_status = 'Active'  " +
            "order by room.rm_cost, room.rm_id ";

    private static String GET_ROOM_COUNT_TOTAL_USED = "select count(bk_dtls_id) as tot " +
            "from gp_hotel_parking_reservation_details " +
            "WHERE bk_dtls_room_id = ? and bk_dtls_start_date = ? ";
    private static String GET_ROOM_SPECIAL = "select sp_rm_htl_id, sp_rm_num_room " +
            "from gp_special_date_for_room " +
            "WHERE sp_rm_date = ? and sp_rm_room_id = ? ";

    public List<RoomParkSearchResults> getHotelslForSearch(final String airportCode) {
        List<RoomParkSearchResults> hotels = getHotelsForAirport(airportCode);

        for (RoomParkSearchResults hotel : hotels) {
            hotel.setImageLinks(getHotelImageLinks(hotel.getHotelId()));
            hotel.setRoomList(getRoomDetails(hotel.getHotelId()));
        }

        return hotels;
    }

    private List<RoomParkSearchResults> getHotelsForAirport(final String airportCode) {
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
                bean.setHotelId(rs.getInt("drc_htl_id"));

                results.add(bean);
            }
        } catch (Exception e) {
            DBUtil.closeResources(conn, ps, rs);
            log.error(e.getMessage(), e);
        }

        return results;
    }

    private List<ImageLink> getHotelImageLinks(final int hotelId) {
        List<ImageLink> links = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_HOTEL_IMAGES);
            ps.setInt(1, hotelId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ImageLink il = new ImageLink();
                il.setLargeImageLink("/images/hotel-images/" + rs.getString("gppi_limage"));
                il.setSmallImageLink("/images/hotel-images/" + rs.getString("gppi_timage"));
                links.add(il);
            }
        } catch (Exception e) {
            DBUtil.closeResources(conn, ps, rs);
            log.error(e.getMessage(), e);
        }

        return links;
    }

    private List<Room> getRoomDetails(final int hotelId) {
        List<Room> rooms = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_ROOMS_AVAILABLE);
            ps.setInt(1, hotelId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("rm_id"));
                room.setRoomDescription(rs.getString("rm_type"));
                room.setCost(rs.getString("rm_cost"));
                room.setOccupancy(rs.getString("rm_person"));
                ImageLink il = new ImageLink();
                il.setSmallImageLink("/images/room-images/" + rs.getString("rm_timage"));
                il.setLargeImageLink("/images/room-images/" + rs.getString("rm_limage"));
                room.setImage(il);

                rooms.add(room);
            }
        } catch (Exception e) {
            DBUtil.closeResources(conn, ps, rs);
            log.error(e.getMessage(), e);
        }

        return rooms;
    }

    public void getRoomsAvailableForSearch() {
        //Get Hotels which match search
            //Run GET_HOTEL_IMAGES to get images for hotel to display on screen
                //Run GET_ROOMS_AVAILABLE to get rooms available for each hotel
                //Run GET_ROOM_COUNT_TOTAL_USED to get how many rooms are used for that date
                //Run GET_ROOM_SPECIAL to see if there are any special exemptions for the day

                //For Start and End of trip hotels, run same search for end of trip
                //Get Room Images using GET_ROOM_IMAGES

                //Get extra cost of children: Line 534
    }
}
