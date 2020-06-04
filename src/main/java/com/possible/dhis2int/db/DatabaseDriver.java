package com.possible.dhis2int.db;

import static org.apache.log4j.Logger.getLogger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.audit.Recordlog;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;

@Service
public class DatabaseDriver {

	private final Logger logger = getLogger(DatabaseDriver.class);

	private Properties properties;
	private final static Integer INDENT_FACTOR = 1;

	@Autowired
	public DatabaseDriver(Properties properties) {
		this.properties = properties;
	}

	public Results executeQuery(String formattedSql, String type) throws DHISIntegratorException, UnsupportedEncodingException {
		Connection connection = null;
		try {
			String decodedUrl = URLDecoder.decode(properties.openmrsDBUrl,"UTF-8"); 
			connection = DriverManager.getConnection(decodedUrl);
			if ("ElisGeneric".equalsIgnoreCase(type)) {
				connection = DriverManager.getConnection(properties.openelisDBUrl);
			}
			ResultSet resultSet = connection.createStatement().executeQuery(formattedSql);
			return Results.create(resultSet);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION, formattedSql), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void recordQueryLog(Recordlog record, Integer month, Integer year) throws DHISIntegratorException {
		logger.debug("Inside recordQueryLog method");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO dhis2_log (report_name, submitted_date, submitted_by, report_log ,status, comment, report_month, report_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			Timestamp time = new Timestamp(record.getTime().getTime());
			ps.setString(1, record.getEvent());
			ps.setTimestamp(2, time);
			ps.setString(3, record.getUserId());
			ps.setString(4, record.getLog());
			ps.setString(5, record.getStatus().toString());
			ps.setString(6, record.getComment());
			ps.setInt(7, month);
			ps.setInt(8, year);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.JSON_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
					
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public String getQuerylog(String programName, Integer month, Integer year, java.util.Date date) throws DHISIntegratorException {
		logger.info("Inside getQueryLog method");
		ResultSet resultSet = null;
		Connection connection = null;
		String log = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps;
			String retrieveQuery;
			if (date != null) { // search by date 
				retrieveQuery = "SELECT * FROM  dhis2_log WHERE report_name = ? AND submitted_date = ? ORDER BY submitted_date DESC LIMIT 1";
				ps = connection.prepareStatement(retrieveQuery);
	            Timestamp ts=new Timestamp(date.getTime());  
				ps.setString(1, programName);
				ps.setTimestamp(2, ts);
			} else { 
				retrieveQuery = "SELECT * FROM  dhis2_log WHERE report_name = ? AND report_month = ? AND report_year = ? ORDER BY submitted_date DESC LIMIT 1";
				ps = connection.prepareStatement(retrieveQuery);
				ps.setString(1, programName);
				ps.setInt(2, month);
				ps.setInt(3, year);

			}

			resultSet = ps.executeQuery();
			JSONObject jsonObject = new JSONObject();
			while (resultSet.next()) {
				jsonObject.put("status", resultSet.getString(5));
				jsonObject.put("comment", resultSet.getString(6));
				jsonObject.put("response", resultSet.getString(7));
			}
			log = jsonObject.toString(INDENT_FACTOR);
		} catch (SQLException e) {
			logger.error(e);
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION), e);
		} catch (JSONException e) {
			logger.error(e);
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}

		return log;
	}

	public void createTempTable(Integer numberOfMaleLessThanSix, Integer numberOfFemalesLessThanSix,
			Integer numberOfMalesMoreThanSix, Integer numberOfFemalesMoreThanSix) throws DHISIntegratorException {
		logger.info("Inside create temp table method.");

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS imam");
			statement.executeUpdate(
					"CREATE TABLE imam(male_less_than_six int, female_less_than_six int, male_more_than_six int, female_more_than_six int)");
			String insertImamData = new StringBuffer(
					"INSERT INTO imam(male_less_than_six , female_less_than_six , male_more_than_six , female_more_than_six) SELECT ")
							.append(numberOfMaleLessThanSix).append(", ").append(numberOfFemalesLessThanSix)
							.append(", ").append(numberOfMalesMoreThanSix).append(", ")
							.append(numberOfFemalesMoreThanSix).toString();
			statement.executeUpdate(insertImamData);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to create table imam"), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void dropImamTable() throws DHISIntegratorException {
		logger.info("Inside dropImamTable method.");
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS imam");
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to drop table imam"), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void createTempFamilyTable(Integer numberOfVasectomyUser, Integer numberOfPillsUser,
			Integer numberOfOtherUser, Integer numberOfMinilipUser, Integer numberOfIUCDUser,
			Integer numberOfImplantUser, Integer numberOfDepoUser, Integer numberOfCondomsUser)
			throws DHISIntegratorException {
		logger.info("create family planning temp table method.");

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS familyPlanning");
			statement.executeUpdate(
					"CREATE TABLE familyPlanning(vasectomy_user int, pills_user int, other_user int, minilap_user int,IUCD_user int, implant_user int, depo_user int, condoms_user int)");

			String insertFamilyPlanningData = new StringBuffer(
					"INSERT INTO familyPlanning(vasectomy_user, pills_user, other_user,  minilap_user, IUCD_user, implant_user, depo_user, condoms_user) SELECT ")
							.append(numberOfVasectomyUser).append(", ").append(numberOfPillsUser).append(", ")
							.append(numberOfOtherUser).append(", ").append(numberOfMinilipUser).append(", ")
							.append(numberOfIUCDUser).append(", ").append(numberOfImplantUser).append(", ")
							.append(numberOfDepoUser).append(", ").append(numberOfCondomsUser).toString();
			statement.executeUpdate(insertFamilyPlanningData);

		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to create table familyPlanning"), e);
		} finally {
			if (connection != null) {
				try {

					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

}