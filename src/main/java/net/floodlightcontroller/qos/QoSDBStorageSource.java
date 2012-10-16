package net.floodlightcontroller.qos;


/**
* Copyright 2012 Marist College, New York
* Author Ryan Wallner (ryan.wallner1@marist.edu)
* 
*  Licensed under the Apache License, Version 2.0 (the "License"); you may
*  not use this file except in compliance with the License. You may obtain
*  a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*  License for the specific language governing permissions and limitations
*  under the License.
*    
*  Persistent QoS Policy storage using MySQL backend
**/


import java.sql.*;
import javax.sql.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class QoSDBStorageSource {
	
	protected String databaseUrl;
	protected String uname;
	protected String pwd;
	protected String dbtime;
	protected String dbClass = "com.mysql.jdbc.Driver";
	protected String query = "Select * FROM controller_qos";
	protected static Properties prop = new Properties();
	
	public void connectToDB(){
	
		try {
			//load a properties file
			prop.load(new FileInputStream("src/main/resources/db.mysql.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		databaseUrl = prop.getProperty("dburl");
		uname = prop.getProperty("username");
		pwd = prop.getProperty("password");
		System.out.println("Connected to: " + databaseUrl);
		//System.out.println(uname);
		//System.out.println(pwd);
		
		try {

			Class.forName(dbClass);
			Connection con = DriverManager.getConnection (databaseUrl,uname,pwd);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
			dbtime = rs.getString(1);
			System.out.println(dbtime);
			} //end while

			con.close();
			} //end try

			catch(ClassNotFoundException e) {
			e.printStackTrace();
			}

			catch(SQLException e) {
			e.printStackTrace();
			}
	}
	
	public void addRecord(){
		//TODO
	}
	public void deleteRecord(){
		//TODO
	}
	public void modifyRecord(){
		//TODO
	}

}
