package jp.ac.iwatepu.soner.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public abstract class AbstractWizardStepController implements Initializable {
	protected Task<Integer> task;

	@FXML
	protected ProgressBar pbProgress;	
	@FXML
	protected Label lblProgress;
	@FXML
	protected Button btnContinue;
	@FXML
	protected TextArea taOutput;
	
	protected abstract Task<Integer> createTask();
	protected abstract String nextStepName();
	
	static final Logger logger = LogManager.getLogger("Wizard");
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnContinue.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				try {
					loadNextStep();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		});
		
		task = createTask();
		
		// bind the progress bar and label to the task
		pbProgress.progressProperty().bind(task.progressProperty());
		task.messageProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				lblProgress.setText(newValue);
			}

		});
		
		// bind the download completion to the next step
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {			
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					if (WizardApplication.getInstance().isAutomaticNextStep()) {
						loadNextStep();
					} else {
						btnContinue.setDisable(false);
					}
				} catch (IOException e) {
					logger.error(e);
				}
			}
		});
		
		new Thread(task).start();
	}
	
	protected FXMLLoader loadNextStep() throws IOException {
		FXMLLoader loader = WizardApplication.getInstance().loadFXML(nextStepName());
		return loader;
	}
	
	protected void addOutput(String line) {
		taOutput.appendText(line + "\n");
	}
}
