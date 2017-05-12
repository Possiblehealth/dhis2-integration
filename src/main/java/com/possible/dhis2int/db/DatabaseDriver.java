package com.possible.dhis2int.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;

@Service
public class DatabaseDriver {
	
	private Properties properties;
	
	@Autowired
	public DatabaseDriver(Properties properties) {
		this.properties = properties;
	}
	
	public Results executeQuery(String formattedSql) throws DHISIntegratorException {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			ResultSet resultSet = connection.createStatement().executeQuery(formattedSql);
			return Results.create(resultSet);
		}
		catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION, formattedSql), e);
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (SQLException ignored) {
				}
			}
		}
	}
	
}