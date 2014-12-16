package jp.ac.iwatepu.soner.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

public class WizardController implements Initializable {
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		spAdvanced.visibleProperty().bind(btnAdvanced.selectedProperty());		
	}
	
	@FXML
	private TextField tfURL;
	
	@FXML
	private Button btnStart;
	
	@FXML
	protected void btnStartClick(ActionEvent event) {
		Util.getInstance().setCrawlerStartURL(tfURL.getText());
		Util.getInstance().setCrawlerMaxPages(Integer.valueOf(tfAmount.getText()));
		Util.getInstance().setInputDirName(tfOutputFolder.getText());
		Util.getInstance().setDbURL(tfDatabaseURL.getText());
		Util.getInstance().setDbUser(tfDatabaseUsername.getText());
		Util.getInstance().setDbPassword(tfDatabasePassword.getText());
		WizardApplication.getInstance().setAutomaticNextStep(cbAutomaticNextStep.isSelected());
		
		try {
			String startStep = cmbStartingStep.getValue();
			if (startStep == null || startStep.equals("Downloading")) {
				WizardApplication.getInstance().loadFXML("Crawling.fxml");
			} else if (startStep.equals("Processing")) {
				WizardApplication.getInstance().loadFXML("Processing.fxml");
			} else if (startStep.equals("Tagging")) {
				WizardApplication.getInstance().loadFXML("Tagging.fxml");
			} else if (startStep.equals("Ranking")) {
				WizardApplication.getInstance().loadFXML("Ranking.fxml");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

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
}
