package staypark.parking.handler;

import staypark.parking.beans.*;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class HotelHandler {

    private static String GET_HOTELS_FOR_SEARCH =
            "select cost.*, p.*, deptloc.*, city.gpc_id, city.gpc_state_id, city.gpc_city, city.gpc_state_id, city.gpc_status, state.gps_id, state.gps_state, state.gps_status " +
            "from dbstaypark.gp_htl_day_range_cost as cost " +
            "left join  dbstaypark.gp_deperture_location as deptloc on cost.drc_gpi_dep_id = deptloc.gdl_id  " +
            "left join dbstaypark.gp_parking_info as p on p.gpi_aff_id = cost.drc_aff_id " +
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

    public RoomParkSearchResults getHotelslForSearch(final RoomParkSearch search) {
    	String airportCode = AirportUtil.getAirportCodeFromSearchCityString(search.getCity());
        RoomParkSearchResults results = new RoomParkSearchResults();

        List<HotelInformation> hotels = getHotelsForAirport(airportCode);

        for (HotelInformation hotel : hotels) {
            //Set hotel image links
            hotel.setImageLinks(getHotelImageLinks(hotel.getHotelId()));

            //Set room information for hotel
            hotel.setRoomList(getRoomDetails(hotel, search));
        }

        results.setHotels(hotels);
        results.setSearch(buildSearchOptionsBean(search));

        return results;
    }

    private RoomParkSearchOptions buildSearchOptionsBean(final RoomParkSearch search) {
        RoomParkSearchOptions searchOptions = new RoomParkSearchOptions();

        //Set fields related to hotel stay
        searchOptions.setHotelArrivalDate(DateUtil.formatDate(search.getPrimaryCheckInDate()));
        searchOptions.setHotelArrivalDateMDY(DateUtil.formatDateWithYearAtEnd(search.getPrimaryCheckInDate()));
        searchOptions.setHotelDepartureDate(DateUtil.formatDate(search.getPrimaryCheckOutDate()));
        searchOptions.setHotelDepartureDateMDY(DateUtil.formatDateWithYearAtEnd(search.getPrimaryCheckOutDate()));
        searchOptions.setEndHotelArrivalDate(DateUtil.formatDate(search.getSecondaryCheckInDate()));
        searchOptions.setEndHotelArrivalDateMDY(DateUtil.formatDateWithYearAtEnd(search.getSecondaryCheckInDate()));
        searchOptions.setEndHotelDepartureDate(DateUtil.formatDate(search.getSecondaryCheckOutDate()));
        searchOptions.setEndHotelDepartureDateMDY(DateUtil.formatDateWithYearAtEnd(search.getSecondaryCheckOutDate()));

        int numberDaysAtHotel = getNumberOfDaysForHotel(search.getHotelTimeOptions(), search.getPrimaryCheckInDate(), search.getPrimaryCheckOutDate(),
                search.getSecondaryCheckInDate(), search.getSecondaryCheckOutDate());
        searchOptions.setTotalHotelNights(numberDaysAtHotel);

        //Set some room related details for display on the results page
        searchOptions.setRooms(search.getRooms());
        searchOptions.setTotalAdults(search.getAdults());
        searchOptions.setChildren(search.getChildren());

        //Set parking related information
        searchOptions.setCarDropOffDate(DateUtil.formatDate(search.getDropOffDate()) + " " + search.getDropOffTime());
        searchOptions.setCarPickUpDate(DateUtil.formatDate(search.getPickUpDate()) + " " + search.getPickUpTime());
        searchOptions.setCarDropOffDateMDY(DateUtil.formatDateWithYearAtEnd(search.getDropOffDate()));
        searchOptions.setCarPickUpDateMDY(DateUtil.formatDateWithYearAtEnd(search.getPickUpDate()));
        searchOptions.setCarDropOffTime(search.getDropOffTime());
        searchOptions.setCarPickUpTime(search.getPickUpTime());

        if (search.getHotelOptions().equals("R")) {
            searchOptions.setSearchType("R");
        } else if (search.getHotelTimeOptions().equals("S")) {
            searchOptions.setSearchType("S");
        } else if (search.getHotelTimeOptions().equals("E")) {
            searchOptions.setSearchType("E");
        } else if (search.getHotelTimeOptions().equals("B")) {
            searchOptions.setSearchType("B");
        }

        Date carStartDate = getCarStartDate(search.getHotelTimeOptions(), search.getDropOffDate(),
                search.getPrimaryCheckInDate(), search.getSecondaryCheckInDate());
        Date carEndDate = getCarEndDate(search.getHotelTimeOptions(), search.getPrimaryCheckOutDate(),
                search.getPickUpDate());

        searchOptions.setTotalParkingTime(DateUtil.getDaysDifference(carStartDate, carEndDate));

        return searchOptions;
    }

    private List<HotelInformation> getHotelsForAirport(final String airportCode) {
        List<HotelInformation> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_HOTELS_FOR_SEARCH);
            ps.setString(1, airportCode );
            rs = ps.executeQuery();
            while (rs.next()) {
                HotelInformation bean = new HotelInformation();
                bean.setHotelTitle(rs.getString("drc_gpi_inn_name") + " - " + rs.getString("gdl_logo_title"));
                bean.setCityState(rs.getString("gpc_city") + ", " + rs.getString("gps_state"));
                bean.setHotelText(rs.getString("drc_gpi_desc"));
                bean.setHotelId(rs.getInt("drc_htl_id"));
                bean.setCarBaseRate(rs.getBigDecimal("drc_base_rate"));
                bean.setCarUpToSevenRate(rs.getBigDecimal("drc_1_7_cost"));
                bean.setCarEightToFourteenRate(rs.getBigDecimal("drc_8_14_cost"));
                bean.setCarOverFifteenRate(rs.getBigDecimal("drc_upto_15_cost"));
                bean.setLogoImage("/images/hotel-logo/" + rs.getString( "drc_gpi_logo"));
                bean.setAirportDistance(rs.getDouble("gpi_distence"));
                bean.setShuttleFrequency(rs.getString("gpi_shuttel"));
                bean.setLatitude(rs.getBigDecimal("gpi_latitude"));
                bean.setLongitude(rs.getBigDecimal("gpi_longitude"));

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

    private List<Room> getRoomDetails(final HotelInformation hotel, final RoomParkSearch search) {
        List<Room> rooms = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_ROOMS_AVAILABLE);
            ps.setInt(1, hotel.getHotelId());
            rs = ps.executeQuery();
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("rm_id"));
                room.setRoomDescription(rs.getString("rm_type"));
                room.setOccupancy(rs.getInt("rm_person"));
                room.setMaxOccupancy(rs.getInt("rm_max_guest"));
                room.setBaseCost(rs.getBigDecimal("rm_cost"));
                room.setExtraPersonCost(rs.getBigDecimal("rm_extra_cost"));
                room.setExtraChildCost(rs.getBigDecimal("rm_child_extra_cost"));
                ImageLink il = new ImageLink();
                il.setSmallImageLink("/images/room-images/" + rs.getString("rm_timage"));
                il.setLargeImageLink("/images/room-images/" + rs.getString("rm_limage"));
                room.setImage(il);

                if ((room.getMaxOccupancy()*search.getRooms()) < (search.getAdults() + search.getChildren())) {
                    room.setAvailable(false);
                }

                rooms.add(room);
            }
        } catch (Exception e) {
            DBUtil.closeResources(conn, ps, rs);
            log.error(e.getMessage(), e);
        }
        
        for (Room room : rooms) {
            Date carStartDate = getCarStartDate(search.getHotelTimeOptions(), search.getDropOffDate(),
                    search.getPrimaryCheckInDate(), search.getSecondaryCheckInDate());
            Date carEndDate = getCarEndDate(search.getHotelTimeOptions(), search.getPrimaryCheckOutDate(),
                    search.getPickUpDate());

            double hotelDays = DateUtil.getDaysDifference(search.getPrimaryCheckInDate(), search.getPrimaryCheckOutDate());
            if (search.getHotelTimeOptions().equals("B")) {
                hotelDays += DateUtil.getDaysDifference(search.getSecondaryCheckInDate(), search.getSecondaryCheckOutDate());
            }

            BigDecimal parkingCost = new BigDecimal(0);
            if (!"R".equals(search.getHotelOptions())) {
               parkingCost = getParkingCostForRoomAndPark(carStartDate, carEndDate, hotel.getCarBaseRate(),
                        hotel.getCarUpToSevenRate(), hotel.getCarEightToFourteenRate(), hotel.getCarOverFifteenRate(),
                        search.getHotelOptions());
            }

            BigDecimal roomCost = getRoomCost(search.getAdults(), search.getChildren(), search.getRooms(), hotelDays,
                    room.getBaseCost(), room.getOccupancy(),
                    room.getExtraPersonCost(), room.getExtraChildCost());

            BigDecimal totalCost = roomCost.add(parkingCost);
            if (totalCost != null) {
                totalCost = totalCost.setScale(2, RoundingMode.CEILING);
            }

            room.setTotalCost(totalCost);
        }

        return rooms;
    }

    private int getNumberOfDaysForHotel(final String type, final Date primaryStart, final Date primaryEnd,
    		final Date secondaryStart, final Date secondaryEnd) {
    	if (type == null) {
    		return 0;
    	}
    	if (type.equals("B")) {
            int startDaysDifference = DateUtil.getDaysDifference(primaryStart, primaryEnd);
            int endDaysDifference = DateUtil.getDaysDifference(secondaryStart, secondaryEnd);
            return startDaysDifference + endDaysDifference;
    	} else {
            return DateUtil.getDaysDifference(primaryStart, primaryEnd);
    	}
    }

    private BigDecimal getParkingCostForRoomAndPark(final Date startParking, final Date endParking, final BigDecimal baseRate,
                                final BigDecimal upto7Rate, final BigDecimal upto14Rate, final BigDecimal over15Rate,
                                final String stayType) {
        BigDecimal discount = new BigDecimal(0.00);
        double days = DateUtil.getDaysDifference(startParking, endParking);

        if ("RP".equals(stayType)) {
            int daysDifference = DateUtil.getDaysDifference(startParking, endParking);
            if (daysDifference > 0) {
                if (daysDifference <= 7 && upto7Rate.doubleValue() > 0) {
                    double d = (baseRate.doubleValue()*days)-(upto7Rate.doubleValue()*days);
                    discount = new BigDecimal(d);
                } else if (daysDifference <= 14 && upto14Rate.doubleValue() > 0) {
                    double d = (baseRate.doubleValue()*days)-(upto14Rate.doubleValue()*days);
                    discount = new BigDecimal(d);
                } else if (over15Rate.doubleValue() > 0) {
                    double d = (baseRate.doubleValue()*days)-(over15Rate.doubleValue()*days);
                    discount = new BigDecimal(d);
                }
            }
        }

        double totalCost = (baseRate.doubleValue()*days) - discount.doubleValue();

        return new BigDecimal(totalCost);
    }

    private Date getCarStartDate(final String stayType, final Date carDropOffDate, final Date hotelCheckInDate,
                                 final Date secondaryHotelCheckInDate) {
        if ("S".equals(stayType)) {
            return carDropOffDate;
        } else if ("E".equals(stayType)) {
            return hotelCheckInDate;
        } else {
            return secondaryHotelCheckInDate;
        }
    }

    private Date getCarEndDate(final String stayType, final Date hotelCheckOutDate, final Date pickUpDate) {
        if ("S".equals(stayType)) {
            return pickUpDate;
        } else if ("E".equals(stayType)) {
            return pickUpDate;
        } else {
            return DateUtil.addDaysToDate(hotelCheckOutDate, 1);
        }
    }

    private BigDecimal getRoomCost(final double adults, final double children, final double rooms, final double days,
                                             final BigDecimal roomCost, final double roomPersons,
                                             final BigDecimal extraPersonCost, final BigDecimal extraChildCost) {
        BigDecimal cost = new BigDecimal(0);

        double extraPeople = adults - (roomPersons*rooms);
        if (extraPeople > 0) {
            double costd = ((days * roomCost.doubleValue() * rooms)+(extraPeople * extraPersonCost.doubleValue() * days * rooms));
            cost = new BigDecimal(costd);
        } else {
            double costd = (days * roomCost.doubleValue() * rooms);
            cost = new BigDecimal(costd);
        }

        double extraChildren = ((adults + children - extraPeople)-(roomPersons * rooms));
        if(extraChildren > 0) {
            cost.add(new BigDecimal(extraChildren * extraChildCost.doubleValue() * days * rooms));
        }

        return cost;
    }

    private boolean isRoomAvailableForNumberOfPeople(final double adults, final double children,
                                                          final double rooms, final double roomMaxPersons) {
        if (((adults + children)/rooms) > roomMaxPersons) {
            return false;
        }

        return true;
    }
}
