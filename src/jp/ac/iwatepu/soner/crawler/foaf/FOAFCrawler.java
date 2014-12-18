package jp.ac.iwatepu.soner.crawler.foaf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.crawler.ThreadPool;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class FOAFCrawler {
	private Set<String> visited = new HashSet<String>();
	private List<String> toVisit = new LinkedList<String>();
	private File outputDir = new File(Util.getInstance().getInputDirName());
	private int processedNum = 0;
	private int erroredNum = 0;
	private int MAX_THREADS = 10;
	private int MAX_TASKS = 20;
	
	static final Logger logger = LogManager.getLogger("FOAFCrawler");

	public static void main(String[] args) {
		FOAFCrawler foafCrawler = new FOAFCrawler();
				
		foafCrawler.run();
	}	
	
	public void downloadedPage(int numDownloaded) {		
	}
	
	public void loadedPage(int numLoaded) {		
	}	
	
	public int getErroredNum() {
		return erroredNum;
	}
	
	public int getProcessedNum() {
		return processedNum;
	}


	public void run() {
		if (outputDir.exists() && !outputDir.isDirectory()) {
			logger.error("Output path is not a folder. Exiting");
			System.exit(-1);
		}
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		
		toVisit.add(Util.getInstance().getCrawlerStartURL());
				
		loadDownloadedPageList();
		logger.info("Crawling started...");
		
		ThreadPool threadPool = new ThreadPool(MAX_THREADS, MAX_TASKS);
		
		while (!toVisit.isEmpty() && visited.size() < Util.getInstance().getCrawlerMaxPages()) {
			String top = toVisit.get(0);
			toVisit.remove(0);
			visited.add(top);
			try {
				threadPool.execute(new ProcessingJob(this, new URL(top), true, false));
			} catch (MalformedURLException e1) {
				logger.warn("Error: invalid URL");
			}
			while (threadPool.isBusy() && toVisit.isEmpty()) {
				try {
					Thread.sleep(100);					
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		}
		threadPool.stop();			

		logger.info("Crawling Done.");
	}
	
	private void loadDownloadedPageList() {		
		if (outputDir.list().length > 0) {
			logger.info("Loading downloaded file list...");
		}
		for (String fileName : outputDir.list()) {
			try {
				String original = URLDecoder.decode(fileName, "UTF-8");
				visited.add(original);
				new ProcessingJob(this, new File(outputDir, fileName).toURI().toURL(), false, true).run();
			} catch (Exception e) {
				logger.warn("Error parsing: " + fileName);
				logger.warn(e);
			}			
		}
		if (outputDir.list().length > 0) {
			logger.info("Loading complete.");
		}
	}
	
	private synchronized void addFoafLink(String seeAlsoStr) {
		if (!visited.contains(seeAlsoStr) && !toVisit.contains(seeAlsoStr)) {
			toVisit.add(seeAlsoStr);
		}
	}

	class ProcessingJob implements Runnable {
		FOAFCrawler shared;
		URL foafPageURL;
		boolean writeToFile;
		boolean load;
		
		public ProcessingJob(FOAFCrawler shared, URL foafPageURL,
				boolean writeToFile, boolean load) {
			super();
			this.shared = shared;
			this.foafPageURL = foafPageURL;
			this.writeToFile = writeToFile;
			this.load = load;
		}
	
		private void processPage(URL foafPageURL, boolean writeToFile) throws IOException {
			OntModel model = loadModel(foafPageURL);
			if (writeToFile) {
				File outputFile = new File(shared.outputDir, 
						URLEncoder.encode(foafPageURL.toString(), "UTF-8"));			
				model.write(new FileOutputStream(outputFile), "RDF/XML");	
			}
		
			String queryRequest = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
					"SELECT ?seeAlso \n" +
					"WHERE\n" +
					" { ?x foaf:knows ?knowsPerson.\n" +
					"   ?knowsPerson rdfs:seeAlso ?seeAlso " +
					" }";			
	
			Query query = QueryFactory.create(queryRequest);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
				
			ResultSet response = qexec.execSelect();
	
			while (response.hasNext()) {
				QuerySolution soln = response.nextSolution();
				RDFNode seeAlsoLink = soln.get("?seeAlso");
				String seeAlsoStr = seeAlsoLink.toString();
	
				shared.addFoafLink(seeAlsoStr);
			}
		}
		
		private OntModel loadModel(URL url) throws IOException {
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
			InputStream inFoafInstance = url.openStream();
			model.read(inFoafInstance, "http://xmlns.com/foaf/0.1/");
			inFoafInstance.close();
			return model;
		}
	
		@Override
		public void run() {
			try {
				processPage(this.foafPageURL, this.writeToFile);			
				if (this.load) {
					shared.loadedPage(shared.visited.size());
				} else {
					shared.downloadedPage(shared.visited.size());
				}
				shared.processedNum++;
	
				if (shared.processedNum % 100 == 0) {
					FOAFCrawler.logger.info("Processed: " + shared.processedNum);
				}
			} catch (Exception e) {
				shared.erroredNum++;						
				FOAFCrawler.logger.warn("Error parsing: " + foafPageURL);
				FOAFCrawler.logger.warn(e);
			}
		}
	}
}