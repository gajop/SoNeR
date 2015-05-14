package jp.ac.iwatepu.soner.gui;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.jena.atlas.logging.Log;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jp.ac.iwatepu.soner.Util;

/**
 * Wizard application (entry point of the program).
 * Supports both GUI and CLI (with -m option).
 * @author gajop
 *
 */
public class WizardApplication extends Application {
	private static WizardApplication instance;
	private Pane content;
	private boolean automaticNextStep;
	private Stage primaryStage;
	
	static final Logger logger = LogManager.getLogger("Wizard");
	
	public static WizardApplication getInstance() { 
		return instance;
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	@Override
	public void start(Stage primaryStage) {
		instance = this;
		content = new Pane();	
		try {
			loadFXML("Start.fxml");
		} catch (IOException e) {			
			logger.error(e);
			System.exit(-1);
		}
		Scene scene = new Scene(content);	
		this.primaryStage = primaryStage;
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent event) {
		    	Platform.exit();
		    	System.exit(0);
		    }
		});
	}

	public static void main(String[] args) throws ParseException {
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption("m", true, "module (suppresses GUI)");
		options.addOption("p", true, "properties file override");
		Option helpOption = new Option("h", "print this message");
		helpOption.setLongOpt("help");
		options.addOption(helpOption);
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (UnrecognizedOptionException ex) {
			logger.error("No such option: " + ex.getOption());
			System.exit(-1);
			return;
		} catch (MissingArgumentException ex) {
			logger.error("Missing argument for option: " + ex.getOption());
			System.exit(-1);
			return;
		}
		
		
		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ant", options );
			System.exit(0);
		}
		
		if (cmd.hasOption("p")) {
			Util.externalProperties = cmd.getOptionValue("p");
		}
		
		// no "m" option, launch the GUI
		if (!cmd.hasOption("m")) {
			logger.info("module not specified (no -m argument given), launching GUI");
			launch(args);
		}
		else {
			String module = cmd.getOptionValue("m");
			
			AbstractWizardStepController awsc;
			if (module.equals("crawler")) {
				awsc = new CrawlingController();
			} else if (module.equals("parser")) {
				awsc = new ProcessingController();
			} else if (module.equals("ranker")) {
				awsc = new RankingController();
			} else {
				logger.fatal("No valid module specified. Possible options are: crawler, parser, ranker");
				System.exit(-1);
				return;
			}
			logger.info("Starting " + module);
			Task<Integer> task = awsc.createTask();
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					logger.info("Finished.");
					System.exit(0);					
				}
			});
			task.run();
		}		
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
