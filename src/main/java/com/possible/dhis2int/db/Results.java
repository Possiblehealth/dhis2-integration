package com.possible.dhis2int.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Results {
	
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
		return rows.get(row - 1).get(column - 1);
	}
	
	public List<List<String>> getRows() {
		return rows;
	}
}
