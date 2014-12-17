package jp.ac.iwatepu.soner.gui;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import jp.ac.iwatepu.soner.graph.GexfGraphGenerator;
import jp.ac.iwatepu.soner.ranking.HITSMain;
import jp.ac.iwatepu.soner.ranking.PageRankMain;

public class ResultsController implements Initializable {

	@FXML
	ListView<String> lvPageRank;
	@FXML
	ListView<String> lvHITSHubs;
	@FXML
	ListView<String> lvHITSAuths;
	@FXML
	ComboBox<String> cmbSynonym;
	@FXML
	TableView<ComparisonLine> tblComparison;
	@FXML
	Button btnExportGraph;
	
	Vector<List<String>> sortedRanksPR;
	Vector<List<String>> sortedHubs;
	Vector<List<String>> sortedAuths;
	
	Task<Integer> task;	
	
	int DISPLAY_AMOUNT = 50;
	
	public void btnExportGraphClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export social graph");
		fileChooser.setInitialFileName("SoNeR.gexf");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("GEXF", "*.gexf"));
		File file = fileChooser.showSaveDialog(WizardApplication.getInstance().primaryStage);
	    if (file != null) {
	    	GexfGraphGenerator gexfGraphGenerator = new GexfGraphGenerator(file);
			gexfGraphGenerator.run();
	    }
	}
	
	public void setResults(ResultsSorter resultsSorter, PageRankMain prMain, HITSMain hitsMain) {
		sortedRanksPR = resultsSorter.sortedRanksPR;
		sortedHubs = resultsSorter.sortedHubs;
		sortedAuths = resultsSorter.sortedAuths;
		selectResults(0);
		
		String labels [] = { "seeAlso only", "attribute only", "seeAlso + attribute" };
		ObservableList<ComparisonLine> data = tblComparison.getItems();
		for (int i = 0; i < labels.length; i++) {
			data.add(new ComparisonLine(labels[i],
		            prMain.totalDifference[i],		            
		            hitsMain.totalDifferenceHubs[i],
		            hitsMain.totalDifferenceAuths[i]));
		}
		
		cmbSynonym.getSelectionModel().select(0);
	}	
	
	public void selectResults(int selectedItemIndex) {
		lvPageRank.setItems(FXCollections.observableList(sortedRanksPR.get(selectedItemIndex)));
		lvHITSHubs.setItems(FXCollections.observableList(sortedHubs.get(selectedItemIndex)));
		lvHITSAuths.setItems(FXCollections.observableList(sortedAuths.get(selectedItemIndex)));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		WizardApplication.getInstance().primaryStage.setHeight(580);		
	}
	
	public void cmbSynonymSelect(ActionEvent event) {
		int selectedItemIndex = cmbSynonym.selectionModelProperty().getValue().getSelectedIndex();
		
		selectResults(selectedItemIndex);
	}
	
}