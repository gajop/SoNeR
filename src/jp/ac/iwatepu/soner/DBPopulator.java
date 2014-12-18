package jp.ac.iwatepu.soner;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DBPopulator {
	private static final Logger logger = LogManager.getLogger("DBPopulator");
	
	int BASE_BUFFER_SIZE = 100;
	Map<String, Vector<String>> bufferMap = new HashMap<String, Vector<String>>();
	
	Connection conn;
	
	public DBPopulator() throws ClassNotFoundException, SQLException {
		conn = DBConnector.getInstance().getConnection();
		conn.setAutoCommit(false);
		
		Vector<String> personUriBuffer = new Vector<String>();
		personUriBuffer.ensureCapacity(BASE_BUFFER_SIZE * 2);
		bufferMap.put("uri", personUriBuffer);
		for (String tag : Util.getInstance().getTags()) {
			Vector<String> buffer = new Vector<String>();
			buffer.ensureCapacity(BASE_BUFFER_SIZE * 3);	    	
			bufferMap.put(tag, buffer);	    	
		}
		Vector<String> bufferSameAs = new Vector<String>();
		bufferMap.put("sameAs", bufferSameAs);
		Vector<String> bufferKnows = new Vector<String>();
		bufferMap.put("knows", bufferKnows);
		Vector<String> bufferSynonym = new Vector<String>();
		bufferMap.put("seeAlso", bufferSynonym);		
	}
	
	public void createTables() throws Exception {
		DBConnector.getInstance().createTablesUnpure();
		for (String tag : Util.getInstance().getTags()) {
    		DBConnector.getInstance().createTagTableUnpure(tag);
    	}
		logger.info("Created tables");
	}
	
	public void insertURI(String URI, String context) throws SQLException {
		Vector<String> buffer = bufferMap.get("uri");
		buffer.add(URI);
		buffer.add(context);
		if (buffer.size() + 2 >= BASE_BUFFER_SIZE * 2) {
			flushURIs();
		}
	}	
	
	private void flushURIs() throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
			      "insert into peopleURI (localURL, context, validURL) values (?, ?, ?);");
		
		Vector<String> buffer = bufferMap.get("uri");
	    for (int i = 0; i < buffer.size(); i += 2) {
	    	prep.setString(1, buffer.get(i));
	    	prep.setString(2, buffer.get(i + 1));
	    	boolean validURL = true;
	    	try {
				new URL(buffer.get(i));
			} catch (MalformedURLException e) {
				validURL = false;
			}
	    	prep.setBoolean(3, validURL);
		    prep.addBatch();
	    }

	    try {
	    	prep.executeBatch();
		    conn.commit();
	    } catch (Exception ex) {
	    	logger.error(ex);
	    	
	    	conn.rollback();
	    	for (int i = 0; i < buffer.size(); i += 2) {
	    		prep.setString(1, buffer.get(i));
		    	prep.setString(2, buffer.get(i + 1));
		    	boolean validURL = true;
		    	try {
					new URL(buffer.get(i));
				} catch (MalformedURLException e) {
					validURL = false;
				}
		    	prep.setBoolean(3, validURL);
			    try {
			    	prep.execute();
			    	conn.commit();
			    } catch (Exception e) {
			    	conn.rollback();
			    }
		    }		
	    }
		buffer.clear();
		buffer.ensureCapacity(BASE_BUFFER_SIZE * 2);
	}
	
	public void insertKnown(String URI, String context, String known) throws SQLException {
		Vector<String> buffer = bufferMap.get("knows");
		buffer.add(URI);
		buffer.add(context);
		buffer.add(known);		
		if (buffer.size() + 3 >= BASE_BUFFER_SIZE * 3) {
			flushKnowns();
		}
	}
	
	private void flushKnowns() throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
			      "insert into peopleKnown (localURL, context, knownPersonUrl) values (?, ?, ?);");
	
		Vector<String> buffer = bufferMap.get("knows");
	    for (int i = 0; i < buffer.size(); i += 3) {
    		prep.setString(1, buffer.get(i));
	    	prep.setString(2, buffer.get(i + 1));
	    	prep.setString(3, buffer.get(i + 2));
		    prep.addBatch();	
	    }
	    
	    try {
	    	prep.executeBatch();
	    	conn.commit();
	    } catch (Exception ex) {
	    	conn.rollback();
		    for (int i = 0; i < buffer.size(); i += 3) {
	    		prep.setString(1, buffer.get(i));
		    	prep.setString(2, buffer.get(i + 1));
		    	prep.setString(3, buffer.get(i + 2));
			    try {
			    	prep.execute();
			    	conn.commit();
			    } catch (Exception e) {
			    	conn.rollback();
			    }
		    }		
	    }
		buffer.clear();
		buffer.ensureCapacity(BASE_BUFFER_SIZE * 3);
	}
	
	public void insertSynonym(String URI, String context, String synonym) throws SQLException {
		Vector<String> buffer = bufferMap.get("seeAlso");
		buffer.add(URI);
		buffer.add(synonym);
		buffer.add(context);
		if (buffer.size() + 3 >= BASE_BUFFER_SIZE * 3) {
			flushSynonyms();			
		}
	}
	
	
	private void flushSynonyms() throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
			      "insert into peopleSynonym (localURL, context, synonym) values (?, ?, ?);");
	
		Vector<String> buffer = bufferMap.get("seeAlso");
	    for (int i = 0; i < buffer.size(); i += 3) {
    		prep.setString(1, buffer.get(i));
	    	prep.setString(2, buffer.get(i + 1));
	    	prep.setString(3, buffer.get(i + 2));
		    prep.addBatch();	
	    }
	    
	    try {
	    	prep.executeBatch();
	    	conn.commit();
	    } catch (Exception ex) {
	    	for (int i = 0; i < buffer.size(); i += 3) {
	    		prep.setString(1, buffer.get(i));
		    	prep.setString(2, buffer.get(i + 1));
		    	prep.setString(3, buffer.get(i + 2));
			    try {
			    	prep.execute();
			    	conn.commit();
			    } catch (Exception e) {
			    	conn.rollback();
			    }
		    }		
	    }
	    buffer.clear();
		buffer.ensureCapacity(BASE_BUFFER_SIZE * 3);
	}
	
	public void insertCustomTag(String URI, String context, String value, String tag) throws SQLException {
		Vector<String> buffer = bufferMap.get(tag);
		buffer.add(URI);
		buffer.add(context);
		buffer.add(value);
		if (buffer.size() + 3 >= BASE_BUFFER_SIZE * 3) {
			flushCustomTag(tag);			
		}
	}
	
	
	private void flushCustomTag(String tag) throws SQLException {
		PreparedStatement prep = conn.prepareStatement(
			      "insert into " + tag + " (localURL, context, value) values (?, ?, ?);");
	
		Vector<String> buffer = bufferMap.get(tag);
	
	    for (int i = 0; i < buffer.size(); i += 3) {
	    	try {
				new URL(buffer.get(i+1));
			} catch (MalformedURLException e) {
				logger.error("Error parsing: " + buffer.get(i) + " " + buffer.get(i+1) + " " + buffer.get(i+2));
				logger.error(i);
				logger.error("Error parsing: " + buffer.get(i-3) + " " + buffer.get(i-2) + " " + buffer.get(i-1));
				System.exit(-1);
			}
	    	
    		prep.setString(1, buffer.get(i));
	    	prep.setString(2, buffer.get(i + 1));
	    	prep.setString(3, buffer.get(i + 2));
		    prep.addBatch();	
	    }
	    
	    try {
	    	prep.executeBatch();
	    	conn.commit();
	    } catch (Exception ex) {
		    for (int i = 0; i < buffer.size(); i += 3) {
	    		prep.setString(1, buffer.get(i));
		    	prep.setString(2, buffer.get(i + 1));
		    	prep.setString(3, buffer.get(i + 2));
			    try {
			    	prep.execute();
			    	conn.commit();
			    } catch (Exception e) {
			    	conn.rollback();
			    }
		    }		
	    }
	    buffer.clear();
		buffer.ensureCapacity(BASE_BUFFER_SIZE * 3);
	}
	
	public void flushAll() throws SQLException {		
		flushURIs();
		//FIXME: replace all these lines with logs instead
		logger.info("flushed URIs");
		flushKnowns();
		logger.info("flushed knowns");
		flushSynonyms();
		logger.info("flushed synonyms");
		for (String tag : Util.getInstance().getTags()) {
			flushCustomTag(tag);
		}
		logger.info("flushed tags");
	}
}
