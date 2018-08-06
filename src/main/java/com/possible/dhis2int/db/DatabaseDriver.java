package com.possible.dhis2int.db;

import static org.apache.log4j.Logger.getLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

	public Results executeQuery(String formattedSql, String type) throws DHISIntegratorException {
		Connection connection = null;
		try {

			connection = DriverManager.getConnection(properties.openmrsDBUrl);
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

	public void executeQuerylog(Recordlog record) throws DHISIntegratorException {
		logger.debug("Inside execute query log method");
		Connection connection = null;

		try {

			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"insert into dhis2_report_status (report_name, date, submitted_by, error_log ,status) values ( ?, ?, ?, ?,?)");

			Timestamp time = new Timestamp(record.getTime().getTime());

			ps.setString(1, record.getEvent());
			ps.setTimestamp(2, time);
			ps.setString(3, record.getUserId());
			ps.setString(4, record.getComment());
			ps.setString(5, record.getStatus().toString());

			logger.debug(ps.toString());

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

	public String getQuerylog(String programName) throws DHISIntegratorException {
		logger.debug("I am here");

		ResultSet resultSet = null;
		Connection connection = null;
		String log = null;
		try {

			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			PreparedStatement ps = connection.prepareStatement(
					"select * from  dhis2_report_status where report_name = ? order by date desc limit 1");
			ps.setString(1, programName);

			logger.debug(ps.toString());

			resultSet = ps.executeQuery();
			JSONObject jsonObject = new JSONObject();
			while (resultSet.next()) {
				jsonObject.put("status",  resultSet.getString(4));
				jsonObject.put("response", resultSet.getString(6));
			}
			log = jsonObject.toString(INDENT_FACTOR);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION), e);
		} catch (JSONException e) {
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

}