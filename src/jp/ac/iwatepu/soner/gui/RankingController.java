package jp.ac.iwatepu.soner.gui;


import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import jp.ac.iwatepu.soner.ranking.HITSMain;
import jp.ac.iwatepu.soner.ranking.HITSResult;
import jp.ac.iwatepu.soner.ranking.PageRankMain;
import jp.ac.iwatepu.soner.ranking.PageRankResult;

/**
 * Controller for the ranking task
 * @author gajop
 *
 */
public class RankingController extends AbstractWizardStepController {
	private PageRankMain pageRankMain;
	private HITSMain hitsMain;
	private ResultsSorter resultsSorter;

	@Override
	protected Task<Integer> createTask() {
		Task<Integer> task = new Task<Integer>() {
		    @Override protected Integer call() throws Exception {
		    	updateMessage("Initializing...");
		    	updateProgress(0, 100);
		    	final int totalRanking = 8;
		    	pageRankMain = new PageRankMain() {
		    		int totalDone = 0;
		    		@Override
		    		public PageRankResult runPageRank() throws Exception {
		    			PageRankResult result = super.runPageRank();		    			
		    			updateProgress(++totalDone, totalRanking);
		    			updateMessage((100 * totalDone / totalRanking) + "%");
		    			return result;
		    		}
		    	};
		    	pageRankMain.run();
		    	hitsMain = new HITSMain() {
		    		int totalDone = 4;
		    		@Override
		    		public HITSResult runHITS() throws Exception {
		    			HITSResult result = super.runHITS();		    			
		    			updateProgress(++totalDone, totalRanking);
		    			updateMessage((100 * totalDone / totalRanking) + "%");
		    			return result;
		    		}
		    	};
		    	hitsMain.run();
		    	
		    	updateMessage("Sorting...");
		    	resultsSorter = new ResultsSorter(pageRankMain, hitsMain, 50);
		    	resultsSorter.sortResults();
		    	updateMessage("Done");
		    	done();
		    	
		    	
				return 0;
		    }
		};
		return task;
	}



	@Override
	protected String nextStepName() {				 
		return "Results.fxml";
	}
	
	@Override
	protected FXMLLoader loadNextStep() throws IOException {	
		FXMLLoader loader = super.loadNextStep();
		ResultsController controller = loader.<ResultsController>getController();
		controller.setResults(resultsSorter, pageRankMain, hitsMain);
		return loader;
	}
}
