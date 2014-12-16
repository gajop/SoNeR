package jp.ac.iwatepu.soner.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class WizardApplication extends Application {
	private static WizardApplication instance;
	Pane content;
	boolean automaticNextStep;
	Stage primaryStage;
	
	public static WizardApplication getInstance() { 
		return instance;
	}
	
	@Override
	public void start(Stage primaryStage) {
		instance = this;
		content = new Pane();	
		try {
			loadFXML("Start.fxml");
		} catch (IOException e) {
			System.exit(-1);
			e.printStackTrace();
		}
		Scene scene = new Scene(content);	
		this.primaryStage = primaryStage;
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public FXMLLoader loadFXML(String fxml) throws IOException {
		content.getChildren().clear();
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
		Node node = (Node) loader.load();
		content.getChildren().add(node);
		return loader;
	}

	public boolean isAutomaticNextStep() {
		return automaticNextStep;
	}

	public void setAutomaticNextStep(boolean automaticNextStep) {
		this.automaticNextStep = automaticNextStep;
	}
}
