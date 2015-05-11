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

/**
 * Defines sorting of results displayed in the result controller
 * @author gajop
 *
 */
public class ResultsSorter {
	private List<PageRankResult> pageRankResults;
	private List<HITSResult> hitsResults;
	
	private  Vector<List<String>> sortedRanksPR;
	private  Vector<List<String>> sortedHubs;
	private  Vector<List<String>> sortedAuths;
	private int displayAmount;	
	
	static final Logger logger = LogManager.getLogger("Wizard");
	
	public ResultsSorter(PageRankMain pageRankMain, HITSMain hitsMain, int displayAmount) {
		super();
		this.pageRankResults = pageRankMain.getResults();
		this.hitsResults = hitsMain.getResults();
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
	
	private List<String> sortPageRankResult(PageRankResult result, int topNum) {
		List<String> resultStr = new LinkedList<String>();
		int[] topRanks = Util.getInstance().getTopNIndexes(result.ranks, topNum);
		int i = 0;
		for (int index : topRanks) {
			i++;
			try {				
				String personName = personToString(index);
				personName = i + ". " + personName + " (" + result.ranks[index] + ")";
				resultStr.add(personName);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}			
		}
		return resultStr;
	}
	
	
	private List<String> sortHITSResultHubs(HITSResult result, int topNum) {
		List<String> resultStr = new LinkedList<String>();
		int[] topRanks = Util.getInstance().getTopNIndexes(result.hubs, topNum);
		int i = 0;
		for (int index : topRanks) {
			i++;
			try {				
				String personName = personToString(index);
				personName = i + ". " +personName + " (" + result.hubs[index] + ")";
				resultStr.add(personName);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}			
		}
		return resultStr;
	}
	
	private List<String> sortHITSResultAuths(HITSResult result, int topNum) {
		List<String> resultStr = new LinkedList<String>();
		int[] topRanks = Util.getInstance().getTopNIndexes(result.auths, topNum); 
		int i = 0;
		for (int index : topRanks) {
			i++;
			try {				
				String personName = personToString(index);
				personName = i + ". " +personName + " (" + result.auths[index] + ")";
				resultStr.add(personName);
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}			
		}
		return resultStr;
	}
	
	private String personToString(int id) throws ClassNotFoundException, SQLException {
		String personName = DBConnector.getInstance().getPersonName(id);
		if (personName == null) {
			personName = DBConnector.getInstance().getPersonTag("nick", id);
		}
		if (personName == null) {
			personName = DBConnector.getInstance().getPersonURI(id);
		}
		return personName;
	}

	public Vector<List<String>> getSortedRanksPR() {
		return sortedRanksPR;
	}

	public Vector<List<String>> getSortedHubs() {
		return sortedHubs;
	}

	public Vector<List<String>> getSortedAuths() {
		return sortedAuths;
	}	
	
}
