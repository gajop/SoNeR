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

/**
 * Controller for the results preview 
 * @author gajop
 *
 */
public class ResultsController implements Initializable {

	@FXML
	protected ListView<String> lvPageRank;
	@FXML
	protected ListView<String> lvHITSHubs;
	@FXML
	protected ListView<String> lvHITSAuths;
	@FXML
	protected ComboBox<String> cmbSynonym;
	@FXML
	protected TableView<ComparisonLine> tblComparison;
	@FXML
	protected Button btnExportGraph;
	
	private Vector<List<String>> sortedRanksPR;
	private Vector<List<String>> sortedHubs;
	private Vector<List<String>> sortedAuths;
	
	protected Task<Integer> task;	
	
	public void btnExportGraphClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export social graph");
		fileChooser.setInitialFileName("SoNeR.gexf");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("GEXF", "*.gexf"));
		File file = fileChooser.showSaveDialog(WizardApplication.getInstance().getPrimaryStage());
	    if (file != null) {
	    	GexfGraphGenerator gexfGraphGenerator = new GexfGraphGenerator(file);
			gexfGraphGenerator.run();
	    }
	}
	
	public void setResults(ResultsSorter resultsSorter, PageRankMain prMain, HITSMain hitsMain) {
		sortedRanksPR = resultsSorter.getSortedRanksPR();
		sortedHubs = resultsSorter.getSortedHubs();
		sortedAuths = resultsSorter.getSortedAuths();
		selectResults(0);
		
		String labels [] = { "seeAlso", "attribute", "seeAlso + attribute" };
		ObservableList<ComparisonLine> data = tblComparison.getItems();
		for (int i = 0; i < labels.length; i++) {
			data.add(new ComparisonLine(labels[i],
		            prMain.totalDifferentRank[i] * 100 / prMain.getCheckTop(),
		            hitsMain.totalDifferentHubs[i] * 100 / hitsMain.getCheckTop(),
		            hitsMain.totalDifferentAuths[i] * 100 / hitsMain.getCheckTop()));
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
		WizardApplication.getInstance().getPrimaryStage().setHeight(580);		
	}
	
	public void cmbSynonymSelect(ActionEvent event) {
		int selectedItemIndex = cmbSynonym.selectionModelProperty().getValue().getSelectedIndex();
		
		selectResults(selectedItemIndex);
	}
	
}