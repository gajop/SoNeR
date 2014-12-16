package jp.ac.iwatepu.soner;
import java.sql.*;
import java.util.HashSet;

import jp.ac.iwatepu.soner.ranking.Person;
import jp.ac.iwatepu.soner.synonym.SamePair;

public class DBConnector {
	private static DBConnector instance;
	private String connectionPath;
	boolean cacheResults = true; //TODO: should it cache results by default?
	int knownPeopleSize = -1;
	int peopleSize = -1;
	int synonymSize = -1;
	int peopleSame = -1;
	
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
	
	public void init() throws SQLException, ClassNotFoundException {
		Class.forName(Util.getInstance().getDbDriver());
		connectionPath = Util.getInstance().getDbURL() + "?user=" + Util.getInstance().getDbUser() + "&password=" + Util.getInstance().getDbPassword();
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
		//FIXME: this is probably wrong; see code that is affected by it
		peopleSize = rs.getInt(1) + 1; //helps with the assumption that IDs start with 1
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
	      "select name from name inner join peopleuri_modified on name.localUrl = peopleuri_modified.localUrl and " +
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
		allPeople[i] = new Person(i, "abcd", "abcd", true); //FIXME: why is the last person being changed?
		
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
				values[id]= value;
			}			
		}
		
	    conn.close();
		return values;
	}
	
	public int[] getSameNames() throws SQLException, ClassNotFoundException {
		Connection conn = getConnection();

		HashSet<SamePair> values = new HashSet<SamePair>();
		
		for (String tag : Util.getInstance().getTags()) {
			if (tag.equals("knows") || tag.equals("homepage")) { 
				continue;
			}
			/*String query = "select distinct  pm1.personid, pm2.personid from peopleuri_modified as pm1, peopleuri_modified as pm2, " +
					tag + "_modified as " + tag  + "1, " + tag + "_modified as " + tag  + "2" + " WHERE " +
					"pm1.validUrl and pm2.validUrl and " +
					"pm1.localURL = " + tag + "1.localURL and " + tag + "1.context = pm1.context and " +
					"pm2.localURL = " + tag + "2.localURL and " + tag + "2.context = pm2.context and " +
					"pm1.personid != pm2.personid and " + tag + "1.value is not null and " + tag + "2.value is not null and " +
					"(" + tag + "1.value = " + tag + "2.value)";*/
			String query = "select distinct  pm1.personid, pm2.personid from peopleuri_modified as pm1, peopleuri_modified as pm2, " +
					tag + "_modified as " + tag  + "1, " + tag + "_modified as " + tag  + "2" + " WHERE " +
					"pm1.validUrl and pm2.validUrl and " +
					"pm1.personid = " + tag + "1.personid and " +
					"pm2.personid = " + tag + "2.personid and " +
					"pm1.personid != pm2.personid and " + tag + "1.value is not null and " + tag + "2.value is not null and " +
					"(" + tag + "1.value = " + tag + "2.value)";
			/*
			 * FIXME: any way to automatically apply this filter?
			if (tag.equals("mbox_sha1sum")) { 
				query = query + " and mbox_sha1sum1.value != '08445a31a78661b5c746feff39a9db6e4e2cc5cf'";
			} else if (tag.equals("nick")) {
				query = query + " and nick1.value != 'AutoKake' and nick1.value != 'Kake'";
			}
			
			*/
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
				sameNames[i] = pair.getId1();
				sameNames[i+1] = pair.getId2();
				i += 2;
			}
		}
		
		/*
		 * FIXME: needed?
		int[] newSameNames = new int[sameNames.length / 2];
		HashMap<Integer, Set<Integer>> pairs = new HashMap<Integer, Set<Integer>>();
		int addedSoFar = 0;
		for (int i = 0; i < sameNames.length; i += 2) {
			int id1 = sameNames[i];
			int id2 = sameNames[i+1];
		
			Set<Integer> alreadyAdded = pairs.get(id2);
			if (alreadyAdded != null && alreadyAdded.contains(id1)) {
				continue;
			}
			
			Set<Integer> checked = pairs.get(id1);
			if (checked == null) {
				checked = new HashSet<Integer>();
				pairs.put(id1, checked);
			}
			checked.add(id2);
			
			newSameNames[addedSoFar++] = id1;
			newSameNames[addedSoFar++] = id2;
		}
		sameNames = newSameNames;*/
		
		
		return sameNames;
	}
	
	public int[] getPotentialCandidates() throws SQLException, ClassNotFoundException {
		return getSameNames();
	}
	
	public static void main(String[] args) throws Exception {		
		DBConnector.getInstance().run();
	}
	
	private void run() throws Exception {
		this.getConnection();
	}
}
