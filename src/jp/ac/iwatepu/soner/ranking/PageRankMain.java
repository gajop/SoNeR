package jp.ac.iwatepu.soner.ranking;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.synonym.SynonymMerge;

public class PageRankMain {
	protected boolean useSynonyms = false;
	protected boolean withAttributeMatching = false;
	protected boolean onlyWithAttributeMatching = false;
	public List<PageRankResult> results;
	
	private static final Logger logger = LogManager.getLogger("PageRank");
	
	public static void main(String[] args) throws Exception {
		PageRankMain prMain = new PageRankMain();
		prMain.run();
	}
	
	public void run() throws Exception {
		results = new LinkedList<PageRankResult>();
		
		useSynonyms = false;
		withAttributeMatching = false;
		onlyWithAttributeMatching = false;
		PageRankResult resultWithoutSynonyms = runPageRank();
		results.add(resultWithoutSynonyms);
		
		useSynonyms = true;
		withAttributeMatching = false;
		onlyWithAttributeMatching = false;
		PageRankResult resultWithSynonyms = runPageRank();
		results.add(resultWithSynonyms);
		
		useSynonyms = true;
		withAttributeMatching = true;
		onlyWithAttributeMatching = true;
		PageRankResult resultWithSynonymsOnlyAttrMatching = runPageRank();
		results.add(resultWithSynonymsOnlyAttrMatching);
		
		useSynonyms = true;
		withAttributeMatching = true;
		onlyWithAttributeMatching = false;
		PageRankResult resultWithSynonymsAttrMatching = runPageRank();
		results.add(resultWithSynonymsAttrMatching);		
		
		printDifference(resultWithoutSynonyms, resultWithSynonyms);		
		printDifference(resultWithoutSynonyms, resultWithSynonymsOnlyAttrMatching);
		printDifference(resultWithoutSynonyms, resultWithSynonymsAttrMatching);
	}
	
	public PageRankResult runPageRank() throws Exception {
		PageRank pr = new PageRank();
		
		logger.info("Loading from DB...");
		int peopleSize = DBConnector.getInstance().getPeopleSize();
		int knownPeople [] = DBConnector.getInstance().getAllKnownRelationships();
		if (useSynonyms) {
			logger.info("Merging synonyms...");
			SynonymMerge synMerge = new SynonymMerge(withAttributeMatching, onlyWithAttributeMatching);
			synMerge.applySynonymsToKnownRelationships(knownPeople);			
		}
		@SuppressWarnings("unchecked")
		Vector<Integer>[] knownPeopleIds = new Vector[peopleSize];
		for (int i = 0; i < peopleSize; i++) {
			knownPeopleIds[i] = new Vector<Integer>();
		}
		for (int i = 0; i < knownPeople.length; i+=2) {
			knownPeopleIds[knownPeople[i]].add(knownPeople[i + 1]);
		}
		
		//double initialPR = 0.25;// / peopleSize;					
		double initialPR = 0.15;//1.0 / peopleSize;
		if (initialPR == 0) {
			initialPR = Double.MIN_VALUE;
		}

		PageRankResult result = pr.run(peopleSize, knownPeopleIds, initialPR);
		
		return result;
	}	
	
	int COMPARISON_AMOUNT = 3;
	int calculatedAmount = 0;
	public double totalDifference[] = new double[COMPARISON_AMOUNT];
	public double averageDifference[] = new double[COMPARISON_AMOUNT];
	public int totalDifferentRank[] = new int[COMPARISON_AMOUNT];
	
	private void printDifference(PageRankResult result1, PageRankResult result2) {
		double ranksWithoutSynonyms [] = result1.ranks;
		double ranksWithSynonyms [] = result2.ranks;
		
		int checkTop = 100;
		if (checkTop <= 0) {
			checkTop = result1.ranks.length;
		} else {
			checkTop = Math.min(result1.ranks.length, checkTop);
		}
		
		double totalDifference = 0;
		for (int i = 0; i < checkTop; i++) {
			totalDifference += Math.abs(ranksWithSynonyms[i] - ranksWithoutSynonyms[i]);
		}
		double averageDifference = totalDifference / checkTop;
		double sumWithoutSynonyms = Util.getInstance().sum(ranksWithoutSynonyms, checkTop);
		//double sumWithSynonyms = Util.getInstance().sum(ranksWithoutSynonyms, checkTop);
		int totalDifferentRank = 0;
		
		for (int i = 0; i < checkTop; i++) {
			totalDifferentRank += (result1.indexes[i] != result2.indexes[i])?1:0;			
		}
		
		this.totalDifference[calculatedAmount] = totalDifference / sumWithoutSynonyms * 100;
		this.averageDifference[calculatedAmount] = averageDifference;
		this.totalDifferentRank[calculatedAmount] = totalDifferentRank;
		calculatedAmount++;
		logger.info("Total difference: " + totalDifference + " average difference: " + averageDifference + 
				" " + totalDifference / sumWithoutSynonyms * 100 + "%" + " different rank: " + totalDifferentRank  + " / " + result1.ranks.length);		
	}	
}
