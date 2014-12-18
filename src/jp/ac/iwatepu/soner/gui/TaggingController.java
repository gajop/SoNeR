package jp.ac.iwatepu.soner.gui;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.classifier.SVMTrain;

public class TaggingController implements Initializable {
	private static final Logger logger = LogManager.getLogger("TaggingController");
	
	protected int[] sameNames;
	protected SVMTrain svmTrain;
	protected boolean [] matches;
	
	protected String[] tags;
	protected String[][] values;
	public enum LabelResult { SAME, DIFFERENT, SKIP, END };
	
	int currentIndex;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {		
		btnContinue.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				PrintWriter pw;
				try {
					pw = new PrintWriter(new File("labels.csv"));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					return;
				}
				for (int i = 0; i <= currentIndex; i += 2) {
					int id1 = sameNames[i];
					int id2 = sameNames[i + 1];
					boolean match = matches[i/2];
					pw.println(id1 + "," + id2 + "," + match);
				}
				pw.close();
				try {
					loadNextStep();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		try {
			sameNames = DBConnector.getInstance().getPeopleWithSimilarAttributes();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		logger.debug("Candidates: " + sameNames.length);	
		
		svmTrain = new SVMTrain();
		try {
			svmTrain.fetch();
		} catch (Exception e) {
			e.printStackTrace();
		}
		matches = new boolean[sameNames.length / 2];
		
		tags = svmTrain.tags;
		values = svmTrain.values;
		
		int row = 0;
		for (String tag : tags) {
			TextField tf1 = new TextField("");
			tf1.setEditable(false);
			person1Tags.put(tag, tf1);
			TextField tf2 = new TextField("");
			tf2.setEditable(false);
			person2Tags.put(tag, tf2);
			gpContent.addRow(row++, new Label(tag + ": "), tf1, tf2);
		}
		//spSplitPane
		
		displayPerson();
	}
	
	HashMap<String, TextField> person1Tags = new HashMap<String, TextField>();
	HashMap<String, TextField> person2Tags = new HashMap<String, TextField>();
	
	@FXML
	protected GridPane gpContent;	
	@FXML
	protected Button btnContinue;
	@FXML
	protected ScrollPane spAttributes;
	@FXML
	protected Button btnYes;
	@FXML
	protected Button btnNo;
	@FXML
	protected Label lblSynonyms;
	
	public void displayPerson() {
		spAttributes.setVvalue(0);
		if (currentIndex+1 >= sameNames.length) {
			btnYes.setDisable(true);
			btnNo.setDisable(true);
			currentIndex -= 2;
			lblSynonyms.setText("Tagging complete.");
			return;
		}
		
		int id1 = sameNames[currentIndex];
		int id2 = sameNames[currentIndex+1];
		for (int j = 0; j < tags.length; j++) {
			String tag = tags[j];
			String val1 = values[j][id1];
			String val2 = values[j][id2];
			person1Tags.get(tag).setText(val1);
			person2Tags.get(tag).setText(val2);
		}		
		lblSynonyms.setText("Are these people synonyms? (" + (currentIndex/2+1) + "/" + (sameNames.length/2) + ")");		
	}

	protected String nextStepName() {				 
		return "Ranking.fxml";
	}
	
	protected void loadNextStep() throws IOException {	
		WizardApplication.getInstance().loadFXML(nextStepName());
	}
	
	public void addLabel(LabelResult label, int i) {
		if (label.equals(LabelResult.SAME)) {
			matches[i/2] = true;
		} else if (label.equals(LabelResult.DIFFERENT)) {
			matches[i/2] = false;
		}
	}
	
	@FXML
	protected void btnYesClick(ActionEvent event) {
		addLabel(LabelResult.SAME, currentIndex);
		currentIndex += 2;
		displayPerson();
	}
	
	@FXML
	protected void btnNoClick(ActionEvent event) {
		addLabel(LabelResult.DIFFERENT, currentIndex);
		currentIndex += 2;
		displayPerson();
	}
}
