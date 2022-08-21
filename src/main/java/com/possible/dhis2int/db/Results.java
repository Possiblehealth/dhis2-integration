package com.possible.dhis2int.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


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

	public static JSONArray convertToJSON(ResultSet resultSet) throws Exception {
		JSONArray jsonArray = new JSONArray();
 
		while (resultSet.next()) {
	
			int columns = resultSet.getMetaData().getColumnCount();
			JSONObject obj = new JSONObject();
	
			for (int i = 0; i < columns; i++)
				obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1));
	
			jsonArray.put(obj);
		}
		return jsonArray;
	}
	
	public String get(Integer row, Integer column) {
		return rows.get(row - 1).get(column - 1);
	}
	
	public List<List<String>> getRows() {
		return rows;
	}
}
