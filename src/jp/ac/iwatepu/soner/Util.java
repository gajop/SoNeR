package jp.ac.iwatepu.soner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Util {
	private static Util instance = new Util();
	String inputDirName;
	String crawlerStartURL;
	int crawlerMaxPages;
	public boolean DEBUG;
	String dbURL;
	String dbUser;
	String dbPassword;
	String dbDriver;
	
	//TODO: maybe extract tags automatically from the Ontology?
	String [] tags = { 
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
			//"knows",
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

	public boolean isDEBUG() {
		return DEBUG;
	}

	public String getInputDirName() {
		return inputDirName;
	}

	//TODO: change this with a proper logger
	public void logIfDebug(String line) {
		if (DEBUG) {
			System.out.println(line);
		}
	}	
	
	public String getCrawlerStartURL() {
		return crawlerStartURL;
	}	

	public int getCrawlerMaxPages() {
		return crawlerMaxPages;
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
	        //load a properties file
			prop.load(new FileInputStream("Parsing.properties"));
	
            //get the property value and print it out
			inputDirName = prop.getProperty("input_dir");
			dbDriver = prop.getProperty("db_driver");
			dbURL = prop.getProperty("db_url");
			dbUser = prop.getProperty("db_user");
			dbPassword = prop.getProperty("db_password");
			DEBUG = Boolean.valueOf(prop.getProperty("debug"));
			crawlerStartURL = prop.getProperty("crawler_start_url");
			crawlerMaxPages = Integer.valueOf(prop.getProperty("crawler_max_pages"));
		} catch (IOException ex) {
			ex.printStackTrace();
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
	/*
	 * TODO: needed?
	public int getDistance(String first, String second) {
		int m = first.length();
		int n = second.length();
		int d[][] = new int[m+1][n+1];
		for (int i = 0; i < m; i++) {
			d[i][0] = i;
		}
		for (int j = 0; j < n; j++) {
			d[0][j] = j;
		}

		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {				
				if (first.charAt(i-1) == second.charAt(j-1)) {
					d[i][j] = d[i-1][j-1];
				} else {
					d[i][j] = Math.min(d[i-1][j-1] + 1 , Math.min(d[i-1][j] + 1,
									   d[i][j-1] + 1));
				}
			}
		}
		for (int i = 0; i < m+1; i++) {
			for (int j = 0; j < n+1; j++) {	
				System.out.print(d[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println(d[m][n]);
		System.out.println(d.length + " " + d[0].length + " " + m + " " + n);
		return d[m-1][n-1];
	}*/
}

class Pair {
	public double value;
	public int index;
	public Pair(double value, int index) {
		this.value = value;
		this.index = index;
	}
}
