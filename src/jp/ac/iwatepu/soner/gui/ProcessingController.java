package jp.ac.iwatepu.soner.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;

import javafx.concurrent.Task;
import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.processing.JenaFoafParser;

/**
 * Controller for the processing task.
 * @author gajop
 *
 */
public class ProcessingController extends AbstractWizardStepController {

	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		super.initialize(location, resources);
	}
	
	@Override
	protected Task<Integer> createTask() {
		Task<Integer> task = new Task<Integer>() {
			@Override
			protected void updateMessage(String message) {
				logger.info(message);
				super.updateMessage(message);
			}
			
		    @Override protected Integer call() throws Exception {
		    	updateMessage("Loading files into memory...");
		    	JenaFoafParser jenaFOAFParser = new JenaFoafParser() {
		    		@Override
		    		public void parsedDocument(int totalDocuments,
		    				int parsedDocuments) {
		    			super.parsedDocument(totalDocuments, parsedDocuments);
						updateMessage(parsedDocuments + "/" + totalDocuments);
						updateProgress(parsedDocuments, totalDocuments);
		    		}
		    	};
		    	jenaFOAFParser.run();
		    	updateProgress(100, 100);
		    	
		    	try {
			    	if (Util.getInstance().getDbDriver().equals("org.sqlite.JDBC")) {
			    		updateMessage("Executing SQL scripts (this can take a while): Purify URIs");
			    		executeSql("main/resources/sql/purify_uri_sqlite.sql");			    		
			    	} else {
			    		executeSql("main/resources/sql/purify_uri.sql");
			    	}
			    	updateMessage("Executing SQL scripts (this can take a while): Cleanup Known IDs");
			    	executeSql("main/resources/sql/known_ids.sql");			    	
			    	updateMessage("Executing SQL scripts (this can take a while): Synonym script");
			    	executeSql("main/resources/sql/synonym_script.sql");
			    	updateMessage("Executing SQL scripts (this can take a while): Purify attributes");
			    	DBConnector.getInstance().createPurifiedAttributes();
			    	updateMessage("Executing SQL scripts: Finished");
			    	done();
			    	addOutput("People: " + DBConnector.getInstance().getPeopleSize());
			    	addOutput("Known relationships: " + DBConnector.getInstance().getKnownPeopleSize());
			    	addOutput("Synonym size: " + DBConnector.getInstance().getSynonymSize());
		    	} catch (Throwable t) { 
		    		logger.error(t);
		    		t.printStackTrace();
		    		System.exit(-1);
		    	}

		    	updateMessage("Done");		    	
				return 0;
		    }
		};
		return task;
	}

	@Override
	protected String nextStepName() {
		return "Tagging.fxml";
	}
	
	private void executeSql(String sqlFilePath) {
	    final class SqlExecuter extends SQLExec {
	        public SqlExecuter() {
	            Project project = new Project();
	            project.init();
	            setProject(project);
	            setTaskType("sql");
	            setTaskName("sql");
	        }
	    }
	    ClassLoader classLoader = getClass().getClassLoader();
	    InputStream in = classLoader.getResourceAsStream(sqlFilePath); 
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    final File tempFile;
	    try {
	    	tempFile = File.createTempFile("__temp", ".sql");
	    	PrintWriter pw = new PrintWriter(tempFile);
	        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
	        	pw.write(line + "\n");
	        }
	        pw.close();
	    } catch (Exception ex) {
	    	return;
	    }
        
	    
	    SqlExecuter executer = new SqlExecuter();
	    
	    executer.setSrc(tempFile);
	    executer.setDriver(Util.getInstance().getDbDriver());

		String user = Util.getInstance().getDbUser();
		String password = "";
		if (!user.equals("")) {
			user = "?user=" + user;
			password = Util.getInstance().getDbPassword();
			if (!password.equals("")) {
				password = "&password=" + password;
			}
		}
		String connectionPath = Util.getInstance().getDbURL() + user + password;
		
		executer.setUserid(Util.getInstance().getDbUser());
		executer.setPassword(Util.getInstance().getDbPassword());
	    executer.setUrl(connectionPath);
	    executer.setEscapeProcessing(false);
	    executer.execute();
	    tempFile.delete();
	}


}
