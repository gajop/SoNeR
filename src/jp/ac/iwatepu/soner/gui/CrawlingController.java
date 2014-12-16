package jp.ac.iwatepu.soner.gui;

import javafx.concurrent.Task;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.crawler.foaf.FOAFCrawler;

public class CrawlingController extends AbstractWizardStepController {
		
	@Override	
	public Task<Integer> createTask() {
		// create the crawling task
		Task<Integer> task = new Task<Integer>() {
		    @Override protected Integer call() throws Exception {
		    	updateMessage("Loading existing files...");
				FOAFCrawler foafCrawler = new FOAFCrawler() {
					@Override
					public void downloadedPage(int numDownloaded) {
						updateMessage(numDownloaded + "/" + Util.getInstance().getCrawlerMaxPages());
						updateProgress(numDownloaded, Util.getInstance().getCrawlerMaxPages());
					}
				};				
				foafCrawler.run();
				updateMessage("Done");
				updateProgress(100, 100);
				return 0;
		    }
		};
		return task;
	}

	@Override
	protected String nextStepName() {
		return "Processing.fxml";
	}
}
