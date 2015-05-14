package jp.ac.iwatepu.soner;
import java.sql.*;
import java.util.HashSet;

import jp.ac.iwatepu.soner.ranking.Person;
import jp.ac.iwatepu.soner.synonym.SamePair;

/**
 * Singleton class used to connect to a database.
 * @author gajop
 *
 */
public class DBConnector {
	private static DBConnector instance;
	private String connectionPath;
	private boolean cacheResults = true;
	private int knownPeopleSize = -1;
	private int peopleSize = -1;
	private int synonymSize = -1;
	private int peopleSame = -1;
	
	public static DBConnector getInstance() {
		if (instance == null) {
			instance = new DBConnector();
			try {
				instance.init();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}	
	
	private DBConnector() {		
	}
	
	private void init() throws SQLException, ClassNotFoundException {
		Class.forName(Util.getInstance().getDbDriver());
		String user = Util.getInstance().getDbUser();
		String password = "";
		if (!user.equals("")) {
			user = "?user=" + user;
			password = Util.getInstance().getDbPassword();
			if (!password.equals("")) {
				password = "&password=" + password;
			}
		}
		connectionPath = Util.getInstance().getDbURL() + user + password;
	}
	
	public void createTables() throws SQLException {
		Connection conn = DriverManager.getConnection(connectionPath);
		Statement stat = conn.createStatement();
		stat.executeUpdate("DROP TABLE if exists peopleURI;");
		stat.executeUpdate("DROP TABLE if exists peopleKnown;");
		stat.executeUpdate("DROP TABLE if exists peopleSynonym;");
		
		stat.executeUpdate(
			"create table peopleURI (" +
				"localURL VARCHAR(200) NOT NULL," +
				"context VARCHAR(200) NOT NULL," +
				"validURL BOOLEAN NOT NULL," + 
				"id SERIAL PRIMARY KEY," +
				"UNIQUE (localUrl, context)" +
			");"
		);
		stat.executeUpdate(
			"create table peopleKnown (" +
				"localURL VARCHAR(200), " +
				"knownPersonUrl VARCHAR(200)," +
				"context VARCHAR(200)," +
				"PRIMARY KEY (localUrl, knownPersonUrl, context)" +
			");"
		);		
		stat.executeUpdate(
			"create table peopleSynonym (" +
				"localURL VARCHAR(200), " +
				"synonym VARCHAR(200)," +
				"context VARCHAR(200)," +
				"PRIMARY KEY (localUrl, synonym, context)" +
			");"
		);
		conn.close();
	}
	
	public void createTablesUnpure() throws SQLException {
		Connection conn = DriverManager.getConnection(connectionPath);
		Statement stat = conn.createStatement();
		stat.executeUpdate("DROP TABLE if exists peopleURI;");
		stat.executeUpdate("DROP TABLE if exists peopleKnown;");
		stat.executeUpdate("DROP TABLE if exists peopleSynonym;");
		
		stat.executeUpdate(
			"create table peopleURI (" +
				"localURL VARCHAR(200)," +
				"context VARCHAR(200)," +
				"validURL BOOLEAN" +
			");"
		);		
		stat.executeUpdate(
			"create table peopleKnown (" +
				"localURL VARCHAR(200), " +
				"context VARCHAR(200)," +
				"knownPersonUrl VARCHAR(200)" +				
			");"
		);		
		stat.executeUpdate(
			"create table peopleSynonym (" +
				"localURL VARCHAR(200), " +				
				"context VARCHAR(200)," +
				"synonym VARCHAR(200)" +
			");"
		);
		conn.close();
	}
	
	public void createPurifiedAttributes() throws SQLException {
		for (String tag : Util.getInstance().getTags()) {
			String sqlString = "drop table if exists " + tag + "_modified; " +
							   "create table " + tag + "_modified (personId INTEGER, value VARCHAR(200)); " +
							   "INSERT INTO " + tag + "_modified " +
							   "SELECT DISTINCT person_uri.personid, MIN(" + tag + ".value) " +
							   "FROM peopleuri_modified person_uri, " + tag +  " " +
							   "WHERE person_uri.localurl =  " + tag +  ".localurl AND person_uri.context =  " + tag +  ".context GROUP BY person_uri.personid;";
			Connection conn = DriverManager.getConnection(connectionPath);
			Statement stat = conn.createStatement();
			stat.executeUpdate(sqlString);
		}
	}
	
	public void createTagTableUnpure(String tag) throws SQLException {
		Connection conn = DriverManager.getConnection(connectionPath);
		Statement stat = conn.createStatement();
		stat.executeUpdate("DROP TABLE if exists " + tag + ";");
		
		stat.executeUpdate(
			"create table " + tag + " (" +
				"localURL VARCHAR(200)," +
				"context VARCHAR(200)," +
				"value VARCHAR(200)" +
			");"
		);
		conn.close();
	}
	
	public Connection getConnection()  throws SQLException, ClassNotFoundException  {
		Class.forName(Util.getInstance().getDbDriver());
		Connection conn = DriverManager.getConnection(connectionPath);
		
		return conn;
	}

	public int getPeopleSize() throws SQLException, ClassNotFoundException {
		if (cacheResults && peopleSize != -1) {
			return peopleSize;
		}
		Connection conn = getConnection();
		PreparedStatement prep = conn.prepareStatement(
	      "select count(*) from peopleuri_modified");	
		ResultSet rs = prep.executeQuery();
		rs.next();
		peopleSize = rs.getInt(1);
	    conn.close();	    
	    return peopleSize;
	}
	
	public int getKnownPeopleSize() throws SQLException, ClassNotFoundException {
		if (cacheResults && knownPeopleSize != -1) {
			return knownPeopleSize;
		}
		Connection conn = getConnection();
		PreparedStatement prep = conn.prepareStatement(
	      "select count(*) from peopleknown_modified");	
		ResultSet rs = prep.executeQuery();
		rs.next();
		knownPeopleSize = rs.getInt(1);
	    conn.close();	    
	    return knownPeopleSize;
	}
	
	public int[] getAllKnownRelationships() throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		int [] knownPeople = new int[getKnownPeopleSize() * 2];
		PreparedStatement prep = conn.prepareStatement(
	      "select personid, knownpersonid from peopleknown_modified");
		
		ResultSet rs = prep.executeQuery();
		int i = 0;
		while (rs.next()) {
			knownPeople[i++] = rs.getInt(1);
			knownPeople[i++] = rs.getInt(2);			
		}
		
	    conn.close();
		return knownPeople;
	}
	
	public int[] getAllKnownRelationshipsOfPerson(int personId) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		int [] knownPeople = new int[getKnownPeopleSize() * 2];
		PreparedStatement prep = conn.prepareStatement(
	      "select personid, knownpersonid from peopleknown_modified where personid = " + personId + " or knownpersonid = " + personId);
		
		ResultSet rs = prep.executeQuery();
		int i = 0;
		while (rs.next()) {
			knownPeople[i++] = rs.getInt(1);
			knownPeople[i++] = rs.getInt(2);			
		}
		
		int [] newKnownPeople = new int[i];
		for (i = 0; i < newKnownPeople.length; i++) {
			newKnownPeople[i] = knownPeople[i];
		}
		
	    conn.close();
		return newKnownPeople;
	}
	
	public String getPersonName(int id) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();
		
		PreparedStatement prep = conn.prepareStatement(
	      "select value from name inner join peopleuri_modified on name.localUrl = peopleuri_modified.localUrl and " +
	      "name.context = peopleuri_modified.context where personid = (?)");
		prep.setInt(1, id);
		
		ResultSet rs = prep.executeQuery();
		String name = null;
		if (rs.next()) {
			name = rs.getString(1);
		}
		
	    conn.close();
		return name;
	}
	
	public String getPersonTag(String tag, int id) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();
		
		PreparedStatement prep = conn.prepareStatement(
	      "select value from " + tag + "_modified  where personid = (?)");
		prep.setInt(1, id);
				
		ResultSet rs = prep.executeQuery();
		String value = null;
		if (rs.next()) {
			value = rs.getString(1);
		}
		
	    conn.close();
		return value;
	}
	
	public String getPersonURI(int id) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();
		
		PreparedStatement prep = conn.prepareStatement(
	      "select localUrl from peopleuri_modified where personid = (?)");
		prep.setInt(1, id);
		
		ResultSet rs = prep.executeQuery();
		String uri = null;
		if (rs.next()) {
			uri = rs.getString(1);
		}
		
	    conn.close();
		return uri;
	}
	
	public String getPersonContext(int id) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();
		
		PreparedStatement prep = conn.prepareStatement(
	      "select context from peopleuri_modified where personid = (?)");
		prep.setInt(1, id);
		
		ResultSet rs = prep.executeQuery();
		String context = null;
		if (rs.next()) {
			context = rs.getString(1);
		}
		
	    conn.close();
		return context;
	}
	
	public int getSynonymSize() throws SQLException, ClassNotFoundException {
		if (cacheResults && synonymSize != -1) {
			return synonymSize;
		}
		Connection conn = getConnection();
		PreparedStatement prep = conn.prepareStatement(
	      "select count(*) from peoplesynonym_modified");	
		ResultSet rs = prep.executeQuery();
		rs.next();
		synonymSize = rs.getInt(1);
	    conn.close();	    
	    return synonymSize;
	}
	
	public int[] getAllSynonyms() throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		int [] synonymPeople = new int[getSynonymSize() * 2];
		PreparedStatement prep = conn.prepareStatement(
	      "select personid, synonymid from peoplesynonym_modified");
		
		ResultSet rs = prep.executeQuery();
		int i = 0;
		while (rs.next()) {
			synonymPeople[i++] = rs.getInt(1);
			synonymPeople[i++] = rs.getInt(2);			
		}
		
	    conn.close();
		return synonymPeople;
	}
	
	public Person[] getAllPeople() throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		Person [] allPeople = new Person[getPeopleSize()];
		PreparedStatement prep = conn.prepareStatement(
	      "select personid, localUrl, context, validUrl from peopleuri_modified order by personid asc");
		
		ResultSet rs = prep.executeQuery();
		int i = 0;
		while (rs.next()) {
			Person newPerson = new Person(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBoolean(4));
			allPeople[i++] = newPerson;
		}
		
	    conn.close();
		return allPeople;		
	}
	
	public int getSamePeopleSize() throws SQLException, ClassNotFoundException {
		if (cacheResults && peopleSame != -1) {
			return peopleSame;
		}
		Connection conn = getConnection();
		PreparedStatement prep = conn.prepareStatement(
	      "select count(*) from peopleuri_same");	
		ResultSet rs = prep.executeQuery();
		rs.next();
		peopleSame = rs.getInt(1);
	    conn.close();	    
	    return peopleSame;
	}
	
	public int[] getSamePeople() throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		int [] samePeople = new int[getSamePeopleSize() * 2];
	
	    int i = 0;
	    int bufferSize = 1000000;
	    
		PreparedStatement prep = conn.prepareStatement(
			      "select firstpersonid, secondpersonid from peopleuri_same limit (?) offset (?)");
		prep.setInt(1, bufferSize);
	    while (i < samePeople.length) {        	        
	        prep.setInt(2, i);
			ResultSet rs = prep.executeQuery();
			while (rs.next()) {
				samePeople[i++] = rs.getInt(1);
				samePeople[i++] = rs.getInt(2);
			}
	    }	

		
	    conn.close();
		return samePeople;		
	}
	
	public String[] getField(String field) throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		String [] values = new String[getPeopleSize()];
		for (int i = 0; i < values.length; i++) {
			values[i] = "";
		}
		String query = "select personid, value from " + field + "_modified as p2 order by personid";
		PreparedStatement prep = conn.prepareStatement(query);
		
		ResultSet rs = prep.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			String value = rs.getString(2);
			if (value != null) {
				values[id - 1]= value;
			}			
		}
		
	    conn.close();
		return values;
	}
	
	public int[] getPeopleWithSimilarAttributes() throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		HashSet<SamePair> values = new HashSet<SamePair>();
		
		for (String tag : Util.getInstance().getTags()) {				
			String query = "select distinct  pm1.personid, pm2.personid from peopleuri_modified as pm1, peopleuri_modified as pm2, " +
					tag + "_modified as tag1, " + tag + "_modified as tag2" + " WHERE " +
					"pm1.validUrl and pm2.validUrl and " +
					"pm1.personid = tag1.personid and " +
					"pm2.personid = tag2.personid and " +
					"pm1.personid != pm2.personid and tag1.value is not null and tag2.value is not null and " +
					"(tag1.value = tag2.value)";
			
			// "homepage" tag tends to be incorrect and points to the FOAF specification
			if (tag.equals("homepage")) {
				query = query + " and tag1.value != 'http://xmlns.com/foaf/0.1/'"; 
			}

			PreparedStatement prep = conn.prepareStatement(query);
			ResultSet rs = prep.executeQuery();
			while (rs.next()) {
				int id1 = rs.getInt(1);
				int id2 = rs.getInt(2);
				values.add(new SamePair(id1, id2));
			}			
		}			
		conn.close();
		
		int [] sameNames = new int[values.size() * 2];
		{
			int i = 0;
			for (SamePair pair : values) { 
				sameNames[i] = pair.id1;
				sameNames[i+1] = pair.id2;
				i += 2;
			}
		}		
		
		return sameNames;
	}
	
}
