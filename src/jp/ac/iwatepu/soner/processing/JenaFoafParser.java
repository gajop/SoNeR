package jp.ac.iwatepu.soner.processing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBPopulator;
import jp.ac.iwatepu.soner.Util;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;

public class JenaFoafParser {
	
	private OntModel model;
	private String currentDocumentURL;	
	private DBPopulator dbPopulator;
	private int totalDocuments = 0;
	private int parsedDocuments = 0;
	
	private static final Logger logger = LogManager.getLogger("JenaFoafParser");
	
	public JenaFoafParser() {
		try {
			dbPopulator = new DBPopulator();
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);	
		}
	}
	
	private String trim(String uri) {
		String newUri = uri.replaceAll("\n", "").replaceAll("\r", "").trim();
		if (newUri.equals("http://xmlns.com/foaf/0.1/#me") || newUri.equals("#me")) {
			newUri = currentDocumentURL;
		}
		if (newUri.endsWith("#me")) {
			newUri = newUri.substring(0, newUri.length() - 3);
		}
		return newUri.trim();
	}
	
	private void initializeDatabase() throws Exception {
		dbPopulator.createTables();
	}	
	
	private void flushDatabase() throws SQLException {
		dbPopulator.flushAll(); 
	}
	
	private  void loadModel(File modelFile) throws IOException {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		InputStream inFoafInstance = FileManager.get().open(modelFile.getAbsolutePath());
		model.read(inFoafInstance, "http://xmlns.com/foaf/0.1/");
		inFoafInstance.close();
	}	
	
	protected void parsedDocument(int totalDocuments, int parsedDocuments) {		
	}
	
	private void parseCurrentModel() {
		String personClass = "Person";
		String query = "SELECT ?person " +
				"WHERE {" +
				"      ?person a foaf:" + personClass + " ." +
				"}";
		try {
			if (!runQuery(query, model)) {
				logger.info("no human found: ");
				printOWLModel();
			}
		} catch (SQLException e) {
			logger.error(e);
			System.exit(1);
		}		
		for (String tag : Util.getInstance().getTags()) {
			parseTag(query, personClass, tag);
		}	
		parseTag(query, personClass, "knows");
		parseTag(query, personClass, "seeAlso");
		return;
	}
	
	private void parseTag(String query, String personClass, String tag) {
		String value = "foaf:" + tag;
		if (tag.equals("seeAlso")) {
			value = "rdfs:" + tag;
		}
		query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"SELECT ?person ?" + tag +
				" WHERE {" +
				"      ?person a foaf:" +  personClass + "  ." +
				"      ?person " + value + " ?" + tag + " ." +
				"}";
		try {
			runQuery(query, model, tag);
		} catch (SQLException e) {				
			logger.error(e);
			System.exit(1);
		}
	}
	
	private boolean runQuery(String queryRequest, Model model) throws SQLException {
		boolean found = false;
		StringBuffer queryStr = new StringBuffer();	
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/> ");

		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet response = qexec.execSelect();

			while (response.hasNext()) {
				QuerySolution soln = response.nextSolution();
				RDFNode personId = soln.get("?person");			
		
				String personIdString = trim(personId.toString());  
				if (personIdString != null && !personIdString.isEmpty()) {
					found = true;
					dbPopulator.insertURI(personIdString, currentDocumentURL);
				}
				
				logger.debug(soln.toString());
			}
		} finally {
			qexec.close();
		}
		return found;
	}
	
	private void runQuery(String queryRequest, Model model, String tag) throws SQLException {
		StringBuffer queryStr = new StringBuffer();	
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
		queryStr.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/> ");

		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try {
			ResultSet response = qexec.execSelect();

			while (response.hasNext()) {
				QuerySolution soln = response.nextSolution();
				RDFNode personId = soln.get("?person");			
				RDFNode value = soln.get("?" + tag);				
				
				String personIdString = trim(personId.toString()); 
				String valueString = trim(value.toString()); 
				if (personIdString != null && !personIdString.isEmpty() && valueString != null && !valueString.isEmpty()) {					
					if (tag.equals("knows")) {
						dbPopulator.insertKnown(personIdString, currentDocumentURL, valueString);
					} else if (tag.equals("seeAlso")) {
						dbPopulator.insertSynonym(personIdString, currentDocumentURL, valueString);
					} else {
						dbPopulator.insertCustomTag(personIdString, currentDocumentURL, valueString, tag);
					}
				}
				
				logger.debug(soln.toString());
			}
		} finally {
			qexec.close();
		}
	}
	
	// print out the OWL model
	public void printOWLModel(){
		model.write(System.out, "RDF/XML");		
	}	
	
	//close the model
	public void closeModel() {
		try {
			model.removeAll();
		   	model.close();
		} catch (Exception e) {
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		JenaFoafParser parser = new JenaFoafParser();
		parser.run();
	}
	
	public void run() throws Exception {
		ArrayList<File> allFiles = new ArrayList<File>();
							
		allFiles.addAll(Arrays.asList(new File(Util.getInstance().getInputDirName()).listFiles()));
    	File[] files = new File[allFiles.size()];
    	files = allFiles.toArray(files);
    	
    	int totalErrors = 0;
    	List<File> errorFiles = new LinkedList<File>();
    	
    	initializeDatabase();
    	parsedDocuments = 0;
    	totalDocuments = files.length;
    	for (int i = 0; i < files.length; i++) {
    		File file = files[i];
    		try {
    			currentDocumentURL = URLDecoder.decode(file.getName(), "UTF-8");
    		} catch (Exception ex) {
    			currentDocumentURL = file.getName();
    		}
    		logger.info(currentDocumentURL);
    		logger.info("Loading: " + (i + 1) + "/"  + files.length + " " + file.getAbsolutePath() + "...");
    		try {
	    		loadModel(file);
	    		parseCurrentModel();
	    		closeModel();
    		} catch (Exception ex) {
    			logger.warn("Error parsing file " + file.getAbsolutePath());
    			totalErrors++;
    			errorFiles.add(file);
    		} finally { 
    			closeModel();
    		}
    		parsedDocuments = i+1;
    		parsedDocument(totalDocuments, parsedDocuments);    		
    	}
    	logger.info("Flushing!");
    	flushDatabase();
    	if (totalErrors > 0) {
    		logger.warn("Total errors: " + totalErrors + ":");
    		for (File file : errorFiles) {
    			logger.warn(file.getAbsolutePath());
    		}
    	} else {
    		logger.info("No errors.");
    	}
    	logger.info("Done!");
	}
	
}
