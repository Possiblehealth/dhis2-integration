package com.possible.dhis2int;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
////import org.apache.commons.codec.EncoderException;
////import org.apache.commons.codec.binary.Base64;
////import org.apache.commons.codec.binary.StringUtils;
////import org.apache.http.client.methods.CloseableHttpResponse;
////import org.apache.http.client.methods.HttpPost;
////import org.apache.http.entity.StringEntity;
////import org.apache.http.impl.client.CloseableHttpClient;
////import org.apache.http.impl.client.HttpClients;
////import org.json.simple.JSONArray;
////import org.json.simple.JSONObject;
////import org.json.simple.parser.JSONParser;
////import org.json.simple.parser.ParseException;
//
public class Applicationn {
//
//	private static String api = "http://35.154.1.137:8080/api/dataValueSets";
//
//	private static String databaseUrl = "jdbc:mysql://192.168.33.10/openmrs?"
//			+ "user=openmrs-user&password=password";
//
//	private static Integer getInteger(Object jsonObject, Object key) {
//		return ((Long) ((JSONObject) jsonObject).get(key)).intValue();
//	}
////
//	public static void mainnnnn(String[] args) {
//		String configFile = "config.json";
//		JSONObject config = getProgramConfig(configFile);
//		String sqlFile = config.get("sql").toString();
//		try {
//			String reportSql = getContent(sqlFile);
//			ResultSet resultSet = getResult(reportSql);
//
//			JSONObject dataValueSets = (JSONObject) config.get("dataValueSets");
//			JSONArray dataValues = (JSONArray) dataValueSets.get("dataValues");
//
//
//			dataValues.forEach(dataValue -> {
//				try {
//					resultSet.absolute(getInteger(dataValue, "row"));
//					String value = resultSet.getString(getInteger(dataValue, "column"));
//					((JSONObject) dataValue).put("value", value);
//				}
//				catch (SQLException e) {
//					e.printStackTrace();
//				}
//			});
//			post(dataValueSets);
//		}
//		catch (IOException | SQLException | ClassNotFoundException | EncoderException e) {
//			e.printStackTrace();
//		}
//	}
////
//	private static JSONObject getProgramConfig(String configFile) {
//		try {
//			return (JSONObject) new JSONParser().parse(new FileReader(configFile));
//		}
//		catch (ParseException | IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
////
////
//	private static String getContent(String sqlFile) throws IOException {
//		return Files.readAllLines(Paths.get(sqlFile)).stream().reduce((x, y) -> x + "\n" + y).get();
//	}
//
//	private static void post(JSONObject jsonObject) throws IOException, EncoderException {
////
////		CloseableHttpClient client = HttpClients.createDefault();
////		HttpPost httpPost = new HttpPost(api);
////
////		StringEntity stringEntity = new StringEntity(jsonObject.toString());
////		httpPost.setEntity(stringEntity);
////		String encoding = new String(new Base64().encode(StringUtils.getBytesUtf8("admin:District123")));
////		httpPost.setHeader("Accept", "application/json");
////		httpPost.setHeader("Content-type", "application/json");
////		httpPost.setHeader("Authorization", "Basic " + encoding);
////		CloseableHttpResponse response = client.execute(httpPost);
////
////		if (response.getStatusLine().getStatusCode() != 200) {
////			System.out.println("Failed due to :" + response.getStatusLine().toString());
////		}
////		client.close();
//	}
}
