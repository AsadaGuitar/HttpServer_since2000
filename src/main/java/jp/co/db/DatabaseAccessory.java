package main.java.jp.co.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.java.jp.co.domain.Account;

public class DatabaseAccessory {

	private String url = "jdbc:postgresql://localhost:5432/http_low";
	private String username = "dev";
	private String password = "";
	
	protected Connection conn;
	
	public DatabaseAccessory() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection(url, username, password);
	}
	
	public Account findByName(String name) throws SQLException {
		String sql  = "select id, password, age from account where name = ?;";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, name);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			int id = rs.getInt("id");
			String password = rs.getString("password");
			int age = rs.getInt("age");
			if (id == 0 || password == null || age == 0) {
				System.err.println("illegal columns. name = " + name);
				return null;
			}
			return new Account(id, name, password, age);
		}
		else {
			System.err.println("not found record. name = " + name);
			return null;
		}
	}
}
