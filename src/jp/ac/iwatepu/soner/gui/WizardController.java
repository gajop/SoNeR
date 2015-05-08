package jp.ac.iwatepu.soner.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.crawler.foaf.FOAFCrawler.SEARCH_MODE;

public class WizardController implements Initializable {
	
	private static final Logger logger = LogManager.getLogger("Wizard");
	
	@FXML
	private TextField tfURL;	
	@FXML
	private Button btnStart;
	@FXML
	private ToggleButton btnAdvanced;
	@FXML
	private ScrollPane spAdvanced;
	//advanced options
	@FXML
	private TextField tfAmount;
	@FXML
	private TextField tfOutputFolder;
	@FXML
	private TextField tfDatabaseURL;
	@FXML
	private TextField tfDatabaseDriver;
	@FXML
	private TextField tfDatabaseUsername;
	@FXML
	private TextField tfDatabasePassword;
	@FXML
	private ComboBox<String> cmbStartingStep;
	@FXML
	private CheckBox cbAutomaticNextStep;
	@FXML
	private ComboBox<String> cmbCrawlingSearchMode;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tfURL.setText(Util.getInstance().getCrawlerStartURL());
		tfAmount.setText(String.valueOf(Util.getInstance().getCrawlerMaxPages()));
		tfOutputFolder.setText(String.valueOf(Util.getInstance().getInputDirName()));
		tfDatabaseURL.setText(Util.getInstance().getDbURL());
		tfDatabaseUsername.setText(Util.getInstance().getDbUser());
		tfDatabasePassword.setText(Util.getInstance().getDbPassword());
		tfDatabaseDriver.setText(Util.getInstance().getDbDriver());
		
		cmbStartingStep.getSelectionModel().select(0);
		spAdvanced.visibleProperty().bind(btnAdvanced.selectedProperty());		
	}
	
	@FXML
	protected void btnStartClick(ActionEvent event) {
		Util.getInstance().setCrawlerStartURL(tfURL.getText());
		Util.getInstance().setCrawlerMaxPages(Integer.valueOf(tfAmount.getText()));
		Util.getInstance().setInputDirName(tfOutputFolder.getText());
		Util.getInstance().setDbURL(tfDatabaseURL.getText());
		Util.getInstance().setDbUser(tfDatabaseUsername.getText());
		Util.getInstance().setDbPassword(tfDatabasePassword.getText());
		Util.getInstance().setDbDriver(tfDatabaseDriver.getText());
		WizardApplication.getInstance().setAutomaticNextStep(cbAutomaticNextStep.isSelected());
		
		String crawlingSearchMode = cmbCrawlingSearchMode.getValue();
		if (crawlingSearchMode.equals("Breadth first")) {
			Util.getInstance().setCrawlerSearchMode(SEARCH_MODE.BREADTH_FIRST);
		} else if (crawlingSearchMode.equals("Depth first")) {
			Util.getInstance().setCrawlerSearchMode(SEARCH_MODE.DEPTH_FIRST);
		} else {
			return;
		}
		try {
			String startStep = cmbStartingStep.getValue();
			if (startStep.equals("Downloading")) {
				WizardApplication.getInstance().loadFXML("Crawling.fxml");
			} else if (startStep.equals("Processing")) {
				WizardApplication.getInstance().loadFXML("Processing.fxml");
			} else if (startStep.equals("Tagging")) {
				WizardApplication.getInstance().loadFXML("Tagging.fxml");
			} else if (startStep.equals("Ranking")) {
				WizardApplication.getInstance().loadFXML("Ranking.fxml");
			} else {
				return;
			}
		} catch (IOException e) {
			logger.error(e);
			System.exit(-1);
		}
	}

}
