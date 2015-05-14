package jp.ac.iwatepu.soner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import jp.ac.iwatepu.soner.crawler.foaf.FOAFCrawler;
import jp.ac.iwatepu.soner.crawler.foaf.FOAFCrawler.SEARCH_MODE;

/**
 * Class which contains project-wide configurations that are read from the properties file.
 * @author gajop
 *
 */
public class Util {
	private static Util instance = new Util();
	private String inputDirName;
	private String crawlerStartURL;
	private int crawlerMaxPages;
	private FOAFCrawler.SEARCH_MODE crawlerSearchMode;
	private String dbURL;
	private String dbUser;
	private String dbPassword;
	private String dbDriver;	
	private String [] tags = { 
			"familyName",
			"name",
			"mbox_sha1sum",
			"phone",
			"birthday",		
			"nick",
			"firstName",
			"givenName",
			"surname",		
			"mbox",
			"jabberID",
			"msnChatID",
			"yahooChatID",
			"weblog",
			"aimChatID",
			"homepage",
			"icqChatID",
			"accountName",
			"img",
			"schoolHomepage",
			"publications",
	};
	
	
	public String[] getTags() {
		return tags;
	}
		
	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getInputDirName() {
		return inputDirName;
	}
	
	public String getCrawlerStartURL() {
		return crawlerStartURL;
	}	

	public int getCrawlerMaxPages() {
		return crawlerMaxPages;
	}
	
	public SEARCH_MODE getCrawlerSearchMode() {
		return crawlerSearchMode;
	}

	public void setInputDirName(String inputDirName) {
		this.inputDirName = inputDirName;
	}

	public void setCrawlerStartURL(String crawlerStartURL) {
		this.crawlerStartURL = crawlerStartURL;
	}

	public void setCrawlerMaxPages(int crawlerMaxPages) {
		this.crawlerMaxPages = crawlerMaxPages;
	}
	
	public void setCrawlerSearchMode(SEARCH_MODE crawlerSearchMode) {
		this.crawlerSearchMode = crawlerSearchMode;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getDbURL() {
		return dbURL;
	}

	public void setDbURL(String dbURL) {
		this.dbURL = dbURL;
	}

	private Util() {
		init();
	}
	
	public static Util getInstance() {
		return instance;
	}
	
	private void init() {
		Properties prop = new Properties();			
		try {
			InputStream in = this.getClass().getResourceAsStream("SoNeR.properties");
			prop.load(in);
			//prop.load(new FileInputStream("SoNeR.properties"));
	
			inputDirName = prop.getProperty("input_dir");
			dbDriver = prop.getProperty("db_driver");
			dbURL = prop.getProperty("db_url");
			dbUser = prop.getProperty("db_user");
			if (dbUser == null) {
				dbUser = "";
			}
			dbPassword = prop.getProperty("db_password");
			if (dbPassword == null) {
				dbPassword = "";
			}
			crawlerStartURL = prop.getProperty("crawler_start_url");
			crawlerMaxPages = Integer.valueOf(prop.getProperty("crawler_max_pages"));
			String crawlerSearchModeStr = prop.getProperty("crawler_search_mode");
			if (crawlerSearchModeStr.equals("bfs")) {
				crawlerSearchMode = SEARCH_MODE.BREADTH_FIRST;
			} else if (crawlerSearchModeStr.equals("dfs")) {
				crawlerSearchMode = SEARCH_MODE.DEPTH_FIRST;
			} else {
				throw new RuntimeException("Crawler search mode is invalid/lacking.");
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public int[] getTopNIndexes(double [] array, int num) {
		int indexes[] = new int[num];
		List<Pair> pairs = new LinkedList<Pair>();
		for (int i = 0; i < array.length; i++) {
			pairs.add(new Pair(array[i], i));
		}
		Collections.sort(pairs, new Comparator<Pair>() {
			@Override
			public int compare(Pair o1, Pair o2) {
				if (o1.value > o2.value) {
					return -1;
				} else if (o1.value < o2.value) {
					return 1;
				} else {
					return 0;
				}				
			}
			
		});
		for (int i = 0; i < num; i++) {
			indexes[i] = pairs.get(i).index;
		}
		return indexes;
	}
	
	public double sum(double[] values) {
		return sum(values, values.length);
	}
	
	public double sum(double[] values, int num) {
		double sum = 0;
		for (int i = 0; i < num; i++) {
			sum += values[i];
		}
		return sum;
	}

}

class Pair {
	public double value;
	public int index;
	public Pair(double value, int index) {
		this.value = value;
		this.index = index;
	}
}
