package jp.ac.iwatepu.soner.gui;

import javafx.concurrent.Task;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.crawler.foaf.FOAFCrawler;

/**
 * Controller for the crawling task.
 * @author gajop
 *
 */
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
						super.downloadedPage(numDownloaded);
						int maxPages = Util.getInstance().getCrawlerMaxPages();
						updateMessage("Downloading: " + numDownloaded + "/" + maxPages);
						updateProgress(Math.min(numDownloaded, maxPages), maxPages);
					}
					@Override
					public void loadedPage(int numLoaded) {
						super.loadedPage(numLoaded);
						int maxPages = Util.getInstance().getCrawlerMaxPages();
						updateMessage("Loading existing file: " + numLoaded + "/" + maxPages);
						updateProgress(Math.min(numLoaded, maxPages), maxPages);
					}
				};				
				foafCrawler.run();
				updateMessage("Done");
				updateProgress(100, 100);
				addOutput("Total downloads: " + foafCrawler.getProcessedNum());
				addOutput("Total errors: " + foafCrawler.getErroredNum());
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
