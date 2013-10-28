package edu.kit.cm.ivu.smartmeetings.backend.rooms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import lsm.beans.Observation;
import lsm.beans.ObservedProperty;
import lsm.beans.Sensor;
import lsm.server.LSMServer;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.HexEncoder;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.SparqlAsk;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.SparqlQuery;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.engine.SparqlSelect;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.BuildingSolutionHandler;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.IQuerySolutionHandler;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.ReservationSolutionHandler;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.RoomPropertySolutionHandler;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.RoomSolutionHandler;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.SensorDataSolutionHandler;
import edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler.SensorSolutionHandler;
import edu.kit.cm.ivu.smartmeetings.lsm_dummy.LSMTripleStoreDummy;

/**
 * The implementation of the room management services as a Singleton class.
 * 
 * @author David Kulicke
 * @author Bjoern Juergens
 * @author Valentin Zickner
 * @author Andreas Eberle
 */
public final class RoomManagementLogic {

	/** The unique instance of {@link RoomManagementLogic} */
	private static RoomManagementLogic instance;

	/** The logger used by all SmartDiscussions classes */
	private static final Logger LOG = Logger.getLogger("SmartDiscussions");

	/** Bundle containing all lsm related Strings */
	private final ResourceBundle lsm;

	/** The lsm endpoint address */
	private final String endpointAddress;

	/** The SPARQL query prefixes */
	private final String prefixes;

	/** The LSM Server used to connect to LSM */
	private final LSMServer lsmServer = new LSMTripleStoreDummy();

	/**
	 * Returns the instance of {@link RoomManagementLogic}.
	 * 
	 * @return the instance of {@link RoomManagementLogic}
	 */
	public static RoomManagementLogic getInstance() {
		if (instance == null) {
			instance = new RoomManagementLogic();
		}
		return instance;
	}

	/**
	 * Private constructor which prevents the external instantiation of the
	 * class
	 */
	private RoomManagementLogic() {
		this.lsm = ResourceBundle
				.getBundle("edu.kit.cm.ivu.smartmeetings.strings.lsm");
		this.endpointAddress = lsm.getString("lsm_endpoint_addr");
		this.prefixes = lsm.getString("prefixes");
	}

	public List<Building> getAllBuildings() {
		return executeQueryAndCreateList("search_buildings",
				new BuildingSolutionHandler(), "");
	}

	/**
	 * Returns a list of all available rooms.
	 * 
	 * @return a list of all available rooms.
	 */
	public List<Room> getAllRooms() {
		// all rooms = search with no constraints
		return executeQueryAndCreateList("search_rooms",
				new RoomSolutionHandler(), "");
	}

	/**
	 * Returns a list of all rooms located in the building with the given ID.
	 * 
	 * @param buildingId
	 *            the ID of the corresponding building
	 * @return a list of all rooms located in the building with the given ID
	 */
	public List<Room> getAllRoomsOfBuilding(final String buildingId) {
		final String buildingFilter = String.format(
				lsm.getString("building_id_filter"), buildingId);
		return executeQueryAndCreateList("search_rooms",
				new RoomSolutionHandler(), buildingFilter);
	}

	/**
	 * Returns a list which contains all rooms which match the given criteria.
	 * 
	 * @param criteria
	 *            the criteria which are used for the selection of the rooms
	 * @return a list which contains all rooms which match the given criteria
	 */
	public List<Room> searchRoom(final List<RoomProperty> criteria) {
		final StringBuilder searchQuery = new StringBuilder();
		int i = 0;

		for (final RoomProperty currProperty : criteria) { // build query with
															// all
															// criteria
			LOG.info("Property: " + currProperty.toString());
			// if the criterion has no children, the exact property is searched
			final List<RoomProperty> children = currProperty.getChildren();
			if (children == null || children.isEmpty()) {
				searchQuery.append(String.format(
						lsm.getString("property_detail_filter"),
						currProperty.getId()));

			} else {
				// otherwise, all subclasses are accepted too
				searchQuery.append(String.format(
						lsm.getString("property_class_filter"), i, i,
						currProperty.getId()));
			}
			i++;
		}

		return executeQueryAndCreateList("search_rooms",
				new RoomSolutionHandler(), searchQuery);
	}

	/**
	 * Returns the room with the given id.
	 * 
	 * @param id
	 *            the id of the room
	 * @return the room with the given id
	 */
	public Room getRoomById(final String id) {
		final String idFilter = String.format(lsm.getString("room_id_filter"),
				id);
		final List<Room> rooms = executeQueryAndCreateList("search_rooms",
				new RoomSolutionHandler(), idFilter);
		if (rooms.isEmpty()) {
			return null;
		} else {
			return rooms.get(0);
		}
	}

	/**
	 * Returns the building with the given id.
	 * 
	 * @param id
	 *            the id of the building
	 * @return the building with the given id
	 */
	public Building getBuildingById(final String id) {
		final String idFilter = String.format(
				lsm.getString("building_id_filter"), id);
		final List<Building> buildings = executeQueryAndCreateList(
				"search_buildings", new BuildingSolutionHandler(), idFilter);
		if (buildings.isEmpty()) {
			return null;
		} else {
			return buildings.get(0);
		}
	}

	/**
	 * Returns a list of all available room properties and their subclasses.
	 * 
	 * @return a list of all available room properties and their subclasses
	 * @author David Kulicke
	 */
	public List<RoomProperty> getAllRoomProperties() {
		// retrieve the property superclasses
		final List<RoomProperty> topLevelProperties = executeQueryAndCreateList(
				"get_all_properties", new RoomPropertySolutionHandler());

		// enrich them with their subclasses
		for (final RoomProperty property : topLevelProperties) {
			fetchSubProperties(property);
		}
		return topLevelProperties;
	}

	/**
	 * Returns a list of all room properties the given room features.
	 * 
	 * @param roomId
	 *            the room whose properties should be selected
	 * @return a list of all room properties the given room features
	 * @author David Kulicke
	 */
	public List<RoomProperty> getPropertiesOfRoom(final String roomId) {
		// retrieve the property superclasses
		final List<RoomProperty> topLevelProperties = executeQueryAndCreateList(
				"get_room_properties", new RoomPropertySolutionHandler(),
				roomId);

		// enrich them with their subclasses
		for (final RoomProperty property : topLevelProperties) {
			fetchSubProperties(roomId, property);
		}
		final List<RoomProperty> sensorData = fetchSensorData(roomId);
		topLevelProperties.addAll(sensorData);
		return topLevelProperties;
	}

	/**
	 * Returns the reservation which has the given id.
	 * 
	 * @param reservationId
	 *            the id of the desired reservation
	 * @return the reservation which has the given id
	 */
	public Reservation getReservationById(final String reservationId) {
		final ReservationSolutionHandler reservationSolutionHandler = new ReservationSolutionHandler();
		reservationSolutionHandler.setReservationId(reservationId);

		final List<Reservation> reservations = executeQueryAndCreateList(
				"get_reservation_by_id", reservationSolutionHandler,
				reservationId);

		if (reservations.isEmpty()) {
			return null;
		}
		return reservations.get(0);
	}

	/**
	 * Returns all reservations the given user has created.
	 * 
	 * @param userName
	 *            the name of the user
	 * @return all reservations the given user has created
	 */
	public List<Reservation> getReservationsOfUser(final String userName) {
		final ReservationSolutionHandler reservationSolutionHandler = new ReservationSolutionHandler();
		final String userId = getUserId(userName);
		reservationSolutionHandler.setUserId(userId);

		return executeQueryAndCreateList("get_all_reservations_from_user",
				reservationSolutionHandler, userId);
	}

	/**
	 * Returns all reservations belonging to the given room.
	 * 
	 * @param roomId
	 *            the room whose reservations should be returned
	 * @return all reservations belonging to the given room
	 */
	public List<Reservation> getReservationsOfRoom(final String roomId) {
		final ReservationSolutionHandler reservationSolutionHandler = new ReservationSolutionHandler();
		reservationSolutionHandler.setRoomId(roomId);
		return executeQueryAndCreateList("get_all_reservations_of_room",
				reservationSolutionHandler, roomId);
	}

	/**
	 * Adds a new reservation for the given room with the given start and end
	 * date.
	 * 
	 * @param userName
	 *            the name of the user performing this request
	 * @param roomId
	 *            the room which should be reserved
	 * @param startDate
	 *            the start date of the reservation
	 * @param endDate
	 *            the end date of the reservation
	 * @return true if the reservation was added successfully or false otherwise
	 */
	public boolean addReservation(final String userName, final String roomId,
			final Date startDate, final Date endDate) {
		if (startDate.getTime() > endDate.getTime()) {
			LOG.warning("Invalid start/end time");
			return false;
		}

		// check if the reservation sensor for this room already exists,
		// otherwise create new one
		final String sensorID = roomId + "Res";
		final boolean sensorExists = executeAsk("check_sensor_existence",
				sensorID);

		if (!sensorExists) {
			LOG.info("Sensor does not exist yet, create new one");
			final Sensor reservationSensor = new Sensor();
			reservationSensor.setId(sensorID);
			reservationSensor
					.setSensorType("http://lsm.deri.ie/OpenIoT/SmartMeetings/ont#ReservationSensor");
			lsmServer.sensorAdd(reservationSensor);
		}

		// the reservation is represented by an observation performed by the
		// reservation sensor observing the room
		final Observation reservation = new Observation();
		reservation.setSensor(sensorID);
		reservation.setFeatureOfInterest(roomId);
		reservation.setTimes(new Date());

		final String reservationID = reservation.getId();
		final String userId = getUserId(userName);

		// start date, end date and user are added as properties
		final ObservedProperty startDateProp = new ObservedProperty();
		startDateProp.setObservationId(reservationID);
		startDateProp.setPropertyName("startDate");
		startDateProp.setValue(String.valueOf(startDate.getTime()));

		final ObservedProperty endDateProp = new ObservedProperty();
		endDateProp.setObservationId(reservationID);
		endDateProp.setPropertyName("endDate");
		endDateProp.setValue(String.valueOf(endDate.getTime()));

		final ObservedProperty userProp = new ObservedProperty();
		userProp.setObservationId(reservationID);
		userProp.setPropertyName("user");
		userProp.setValue(userId);

		reservation.addReading(startDateProp);
		reservation.addReading(endDateProp);
		reservation.addReading(userProp);

		// submit the reservation to LSM
		return lsmServer.sensorDataUpdate(reservation);
	}

	/**
	 * Removes the given reservation.
	 * 
	 * @param userName
	 *            the name of the user performing this request
	 * @param reservationId
	 *            the id of the reservation which should be removed
	 * @return true if the reservation was removed successfully or false
	 *         otherwise
	 */
	public boolean removeReservation(final String userName,
			final String reservationId) {
		
		final Reservation reservation = getReservationById(reservationId);
		final Date timestamp = reservation.getTimestamp();
		final String sensorID = reservation.getRoomId() + "Res";
		return lsmServer.deleteAllReadings(sensorID, "=", timestamp, null);
	}

	/**
	 * Registers at the room of the given reservation.
	 * 
	 * @param userName
	 *            the name of the user performing this request
	 * @param reservationId
	 *            the id of the appropriate reservation
	 * @return true if the registration was performed successfully or false
	 *         otherwise
	 */
	public boolean registerAtRoom(final String userName,
			final String reservationId) {

		final Date currentDate = new Date();
		final String userID = getUserId(userName);
		final Reservation reservation = getReservationById(reservationId);

		if (reservation == null) {
			LOG.fine("Reservation does not exist.");
			return false;
		} else if (!reservation.getUserId().equals(userID)) {
			LOG.fine("Reservation does not belong to given user.");
			return false;
		} else if (reservation.getLoginState() == true) {
			LOG.fine("User is already logged in.");
			return false;
		}

		// check if the timing is valid
		final Date startDate = reservation.getStartDate();
		final Date endDate = reservation.getEndDate();
		if (currentDate.compareTo(startDate) < 0
				|| currentDate.compareTo(endDate) > 0) {
			LOG.fine("registerAtRoom: can only register at room during the appointment.");
			return false;
		}

		final String datagraph = lsm.getString("lsm_datagraph");

		// delete old login state and insert new one
		final String logoffTriple = String.format(
				lsm.getString("logoff_triple"), reservationId);
		final String loginTriple = String.format(lsm.getString("login_triple"),
				reservationId);
		lsmServer.deleteTriples(datagraph, logoffTriple);
		return lsmServer.pushRDF(datagraph, loginTriple);
	}

	/**
	 * Unregisters from the room of the given reservation.
	 * 
	 * @param userName
	 *            the name of the user performing this request
	 * @param reservationId
	 *            the id of the appropriate reservation
	 * @return true if the unregistration was performed successfully or false
	 *         otherwise
	 */
	public boolean unregisterFromRoom(final String userName,
			final String reservationId) {
		final String userID = getUserId(userName);
		final Reservation reservation = getReservationById(reservationId);

		if (reservation == null) {
			LOG.fine("Reservation does not exist.");
			return false;
		} else if (!reservation.getUserId().equals(userID)) {
			LOG.fine("Reservation does not belong to given user.");
			return false;
		} else if (reservation.getLoginState() == false) {
			LOG.fine("User has not logged in.");
			return false;
		}

		final String datagraph = lsm.getString("lsm_datagraph");

		// delete old login state and insert new one
		final String logoffTriple = String.format(
				lsm.getString("logoff_triple"), reservationId);
		final String loginTriple = String.format(lsm.getString("login_triple"),
				reservationId);
		lsmServer.deleteTriples(datagraph, loginTriple);
		return lsmServer.pushRDF(datagraph, logoffTriple);
	}

	/**
	 * Fetches the latest sensor data of all sensor observations of the given room.
	 * @param roomID the id of the room
	 * @return a {@link RoomProperty} for each distinct sensor which contains properties with the corresponding sensor data
	 */
	private List<RoomProperty> fetchSensorData(final String roomID) {
		final List<RoomProperty> sensors = executeQueryAndCreateList("get_sensors_of_room", 
				new SensorSolutionHandler(), roomID);
		
		for (RoomProperty sensor: sensors) {
			final List<RoomProperty> sensorData = executeQueryAndCreateList("get_sensor_data_of_room",
					new SensorDataSolutionHandler(), sensor.getId(), roomID);
			sensor.setChildren(sensorData);
		}
		return sensors;
	}
	
	/**
	 * Fetches all {@link RoomProperty}s which are subclasses of the given
	 * {@link RoomProperty} and stores them as child objects in the given
	 * property.
	 * 
	 * @param roomId
	 *            the id of the appropriate room
	 * @param parent
	 *            the parent property
	 * @author David Kulicke
	 */
	private void fetchSubProperties(final RoomProperty parent) {
		final RoomPropertySolutionHandler solutionHandler = new RoomPropertySolutionHandler();

		// receive and store all child properties
		final List<RoomProperty> children = executeQueryAndCreateList(
				"get_all_property_details", solutionHandler, parent.getId());
		parent.setChildren(children);

		// proceed recursively with each child
		for (final RoomProperty child : children) {
			fetchSubProperties(child);
		}
	}

	/**
	 * Fetches all {@link RoomProperty}s which belong to the room with the given
	 * id and are subclasses of the given {@link RoomProperty}. They will be
	 * stored as child objects in the given property.
	 * 
	 * @param roomId
	 *            the id of the appropriate room
	 * @param parent
	 *            the parent property
	 * @author David Kulicke
	 */
	private void fetchSubProperties(final String roomId,
			final RoomProperty parent) {
		final RoomPropertySolutionHandler solutionHandler = new RoomPropertySolutionHandler();
		// solutionHandler.setParent(parent);
		// retrieve and store all child properties
		final List<RoomProperty> children = executeQueryAndCreateList(
				"get_room_properties_detail", solutionHandler, roomId,
				parent.getId());
		parent.setChildren(children);

		// proceed recursively with each child
		for (final RoomProperty child : children) {
			fetchSubProperties(roomId, child);
		}

	}

	/**
	 * Executes the query with the given query parameters. The retrieved result
	 * is parsed with the given {@link IQuerySolutionHandler} to create a list
	 * of objects representing the query's result.
	 * 
	 * @param queryId
	 *            The id of the query.
	 * @param solutionHandler
	 *            A {@link IQuerySolutionHandler} that is able to convert the
	 *            retrieved {@link QuerySolution}s to the needed objects.<br>
	 *            If the solution handler returns null for a request, no element
	 *            will be added to the list.
	 * @param queryParameters
	 *            Additional parameters for the query that are injected with
	 *            String.format(query, queryParameters)
	 * @return A {@link List} containing the result of the query.
	 * 
	 * @author Andreas Eberle
	 */
	private <T> List<T> executeQueryAndCreateList(final String queryId,
			final IQuerySolutionHandler<T> solutionHandler,
			final Object... queryParameters) {
		final SparqlSelect sel = new SparqlSelect(endpointAddress);
		final ResultSet results = executeQuery(sel, queryId, queryParameters);

		// Parse room list and create objects.
		final ArrayList<T> list = new ArrayList<T>();

		if (results != null) {
			while (results.hasNext()) {
				final QuerySolution solution = results.nextSolution();

				// create the object from the query result
				final T solutionObject = solutionHandler.createResult(solution);

				if (solutionObject != null) {
					LOG.fine("Found new object: " + solutionObject);
					list.add(solutionObject);
				} else {
					LOG.fine("Failed to parse solution object.");
				}
			}
		} else {
			LOG.fine("Results are Null for this Query (probably malformed)");
		}

		return list;
	}

	/**
	 * Executes the query with the given Sparql Engine and the given query
	 * parameters.
	 * 
	 * @param engine
	 *            the Sparql Query Engine to use.
	 * @param queryId
	 *            The id of the query.
	 * @param parameters
	 *            Additional parameters for the query that are injected with
	 *            String.format(query, queryParameters)
	 * @return The retrived result of the query.
	 * 
	 * @author Andreas Eberle
	 * @author Valentin Zickner
	 * @author David Kulicke
	 */
	private <Result> Result executeQuery(final SparqlQuery<Result> engine,
			final String queryId, final Object... parameters) {
		final String queryString = prefixes.concat(lsm.getString(queryId));

		LOG.fine("Query to execute without parameters: " + queryString);

		// Execute SPARQL-Query
		final String query = String.format(queryString, parameters);
		LOG.info("executing query \"" + queryId + "\": " + query);

		return engine.execute(query);
	}

	/**
	 * Execute the ask query with the given parameters.
	 * 
	 * @param queryId
	 * @param parameters
	 * @author Valentin Zickner
	 * @author David Kulicke
	 */
	private Boolean executeAsk(final String queryId, final Object... parameters) {
		final SparqlAsk sparqlAsk = new SparqlAsk(endpointAddress);
		return executeQuery(sparqlAsk, queryId, parameters);
	}

	/**
	 * Converts the given user name to its corresponding user ID.
	 * 
	 * @param userName
	 *            the user's name
	 * @return the corresponding ID
	 */
	private String getUserId(final String userName) {
		return lsm.getString("resource_namespace")
				+ HexEncoder.encode(userName);
	}

}
