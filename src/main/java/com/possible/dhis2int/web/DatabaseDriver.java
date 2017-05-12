package com.possible.dhis2int.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;

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
	
	public static class Results {
		
		private final List<List<String>> rows = new ArrayList<>();
		
		public static Results create(ResultSet resultSet) throws SQLException {
			Results results = new Results();
			Integer numberOfCols = resultSet.getMetaData().getColumnCount();
			while (resultSet.next()) {
				List<String> row = new ArrayList<>();
				for (Integer colIndex = 1; colIndex <= numberOfCols; colIndex++) {
					row.add(resultSet.getString(colIndex));
				}
				results.rows.add(row);
			}
			return results;
		}
		
		public String get(Integer row, Integer column) {
			return rows.get(row-1).get(column-1);
		}
	}
}