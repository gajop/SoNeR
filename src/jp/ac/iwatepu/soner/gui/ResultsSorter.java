package jp.ac.iwatepu.soner.gui;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.ranking.HITSMain;
import jp.ac.iwatepu.soner.ranking.HITSResult;
import jp.ac.iwatepu.soner.ranking.PageRankMain;
import jp.ac.iwatepu.soner.ranking.PageRankResult;

public class ResultsSorter {
	List<PageRankResult> pageRankResults;
	List<HITSResult> hitsResults;
	
	public Vector<List<String>> sortedRanksPR;
	public Vector<List<String>> sortedHubs;
	public Vector<List<String>> sortedAuths;
	int displayAmount;
	
	public PageRankMain pageRankMain;
	public HITSMain hitsMain;
	
	static final Logger logger = LogManager.getLogger("Wizard");
	
	public ResultsSorter(PageRankMain pageRankMain, HITSMain hitsMain, int displayAmount) {
		super();
		this.pageRankResults = pageRankMain.results;
		this.hitsResults = hitsMain.results;
		this.pageRankMain = pageRankMain;
		this.hitsMain = hitsMain;
		this.displayAmount = displayAmount;
	}

	public void sortResults() {
		sortedRanksPR = new Vector<List<String>>();				
		for (PageRankResult prResult : pageRankResults) {
			sortedRanksPR.add(sortPageRankResult(prResult, displayAmount));
		}
		
		sortedHubs = new Vector<List<String>>();
		sortedAuths = new Vector<List<String>>();
		for (HITSResult hitsResult : hitsResults) {
			sortedAuths.add(sortHITSResultAuths(hitsResult, displayAmount));
			sortedHubs.add(sortHITSResultHubs(hitsResult, displayAmount));
		}				
		return;
	}
	
	public List<String> sortPageRankResult(PageRankResult result, int topNum) {
		List<String> resultStr = new LinkedList<String>();
		int[] topRanks = Util.getInstance().getTopNIndexes(result.ranks, topNum); 
		for (int index : topRanks) {
			try {
				String personName = personToString(index);
				personName = personName + " (" + result.getRanks()[index] + ")";
				resultStr.add(personName);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}			
		}
		return resultStr;
	}
	
	
	public List<String> sortHITSResultHubs(HITSResult result, int topNum) {
		List<String> resultStr = new LinkedList<String>();
		int[] topRanks = Util.getInstance().getTopNIndexes(result.getHubs(), topNum); 
		for (int index : topRanks) {
			try {
				String personName = personToString(index);
				personName = personName + " (" + result.getHubs()[index] + ")";
				resultStr.add(personName);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}			
		}
		return resultStr;
	}
	
	public List<String> sortHITSResultAuths(HITSResult result, int topNum) {
		List<String> resultStr = new LinkedList<String>();
		int[] topRanks = Util.getInstance().getTopNIndexes(result.getAuths(), topNum); 
		for (int index : topRanks) {
			try {
				String personName = personToString(index);
				personName = personName + " (" + result.getAuths()[index] + ")";
				resultStr.add(personName);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}			
		}
		return resultStr;
	}
	
	public String personToString(int id) throws ClassNotFoundException, SQLException {
		String personName = DBConnector.getInstance().getPersonName(id);
		if (personName == null) {
			personName = DBConnector.getInstance().getPersonTag("nick", id);
		}
		if (personName == null) {
			personName = DBConnector.getInstance().getPersonURI(id);
		}
		return personName;
	}
}
