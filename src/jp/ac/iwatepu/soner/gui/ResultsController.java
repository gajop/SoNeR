package jp.ac.iwatepu.soner.gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

public class ResultsController implements Initializable {

	@FXML
	ListView<String> lvPageRank;
	@FXML
	ListView<String> lvHITS;		
	
	Vector<List<String>> sortedRanksPR;
	Vector<List<String>> sortedHubs;
	Vector<List<String>> sortedAuths;
	
	Task<Integer> task;
	
	@FXML
	ComboBox<String> cmbSynonym;
	
	int DISPLAY_AMOUNT = 50;
	
	public void setResults(ResultsSorter resultsSorter) {
		sortedRanksPR = resultsSorter.sortedRanksPR;
		sortedHubs = resultsSorter.sortedHubs;
		sortedAuths = resultsSorter.sortedAuths;
		selectResults(0);
	}	
	
	public void selectResults(int selectedItemIndex) {
		lvPageRank.setItems(FXCollections.observableList(sortedRanksPR.get(selectedItemIndex)));
		lvHITS.setItems(FXCollections.observableList(sortedHubs.get(selectedItemIndex)));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		WizardApplication.getInstance().primaryStage.setHeight(540);
	}
	
	public void cmbSynonymSelect(ActionEvent event) {
		int selectedItemIndex = cmbSynonym.selectionModelProperty().getValue().getSelectedIndex();
		
		selectResults(selectedItemIndex);
	}
	
}
