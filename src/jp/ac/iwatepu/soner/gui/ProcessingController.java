package jp.ac.iwatepu.soner.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;

import javafx.concurrent.Task;
import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.processing.JenaFoafParser;

public class ProcessingController extends AbstractWizardStepController {

	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		super.initialize(location, resources);
	}

	
	@Override
	protected Task<Integer> createTask() {
		Task<Integer> task = new Task<Integer>() {
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
			    	updateMessage("Executing SQL scripts (this can take a while): Purify URIs");
			    	executeSql("sql/purify_uri.sql");
			    	updateMessage("Executing SQL scripts (this can take a while): Cleanup Known IDs");
			    	executeSql("sql/known_ids.sql");			    	
			    	updateMessage("Executing SQL scripts (this can take a while): Synonym script");
			    	executeSql("sql/synonym_script.sql");
			    	updateMessage("Executing SQL scripts (this can take a while): Purify attributes");
			    	DBConnector.getInstance().createPurifiedAttributes();
			    	done();
		    	} catch (Throwable t) {  
		    		logger.error(t);
		    	}
		    			    	
		    	addOutput("People: " + DBConnector.getInstance().getPeopleSize());
		    	addOutput("Known relationships: " + DBConnector.getInstance().getKnownPeopleSize());
		    	addOutput("Synonym size: " + DBConnector.getInstance().getSynonymSize());		    	
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

	    SqlExecuter executer = new SqlExecuter();
	    executer.setSrc(new File(sqlFilePath));
	    executer.setDriver(Util.getInstance().getDbDriver());
	    executer.setPassword(Util.getInstance().getDbPassword());
	    executer.setUserid(Util.getInstance().getDbUser());
	    executer.setUrl(Util.getInstance().getDbURL() + "?user=" + Util.getInstance().getDbUser() + "&password=" + Util.getInstance().getDbPassword());
	    executer.execute();
	}


}
