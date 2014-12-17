package jp.ac.iwatepu.soner.ranking;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;
import jp.ac.iwatepu.soner.synonym.SynonymMerge;

public class HITSMain {
	private boolean useSynonyms = false;
	private boolean withAttributeMatching = false;
	private boolean onlyWithAttributeMatching = false;
	public List<HITSResult> results;
	
	public static void main(String[] args) throws Exception {
		HITSMain hitsMain = new HITSMain();
		hitsMain.run();
	}
	
	public void run() throws Exception {
		results = new LinkedList<HITSResult>();
		
		useSynonyms = false;
		withAttributeMatching = false;
		onlyWithAttributeMatching = false;
		HITSResult resultWithoutSynonyms = runHITS();		
		
		useSynonyms = true;
		withAttributeMatching = false;
		onlyWithAttributeMatching = false;
		HITSResult resultWithSynonyms = runHITS();
		
		useSynonyms = true;
		withAttributeMatching = true;
		onlyWithAttributeMatching = true;
		HITSResult resultWithSynonymsOnlyAttrMatching = runHITS();		
		
		useSynonyms = true;
		withAttributeMatching = true;
		onlyWithAttributeMatching = false;
		HITSResult resultWithSynonymsAttrMatching = runHITS();		
		
		printDifference(resultWithoutSynonyms, resultWithSynonyms);
		printDifference(resultWithoutSynonyms, resultWithSynonymsOnlyAttrMatching);
		printDifference(resultWithoutSynonyms, resultWithSynonymsAttrMatching);		
	}
	
	public HITSResult runHITS() throws Exception {
		Util.getInstance().logIfDebug("Loading from DB...");
		int peopleSize = DBConnector.getInstance().getPeopleSize();
		//int knownPeopleSize = DBConnector.getInstance().getKnownPeopleSize();
		int knownPeople [] = DBConnector.getInstance().getAllKnownRelationships();
		if (useSynonyms) {
			System.out.println("Merging synonyms...");
			SynonymMerge synMerge = new SynonymMerge(withAttributeMatching, onlyWithAttributeMatching);	
			synMerge.applySynonymsToKnownRelationships(knownPeople);
		}
		//initialize
		@SuppressWarnings("unchecked")
		Vector<Integer>[] knownPeopleIds = (Vector<Integer>[]) new Vector[peopleSize];
		for (int i = 0; i < peopleSize; i++) {
			knownPeopleIds[i] = new Vector<Integer>();
		}
		for (int i = 0; i < knownPeople.length; i+=2) {
			knownPeopleIds[knownPeople[i]].add(knownPeople[i + 1]);
		}		
		
		HITS hits = new HITS();
		HITSResult result = hits.run(peopleSize, knownPeopleIds, 0.15);
		results.add(result);
		
		return result;
	}

	int COMPARISON_AMOUNT = 3;
	int calculatedAmount = 0;
	
	public double totalDifferenceHubs[] = new double[COMPARISON_AMOUNT];
	public double averageDifferenceHubs[] = new double[COMPARISON_AMOUNT];
	public int totalDifferentHubs[] = new int[COMPARISON_AMOUNT];
	
	public double totalDifferenceAuths[] = new double[COMPARISON_AMOUNT];
	public double averageDifferenceAuths[] = new double[COMPARISON_AMOUNT];
	public int totalDifferentAuths[] = new int[COMPARISON_AMOUNT];
	private void printDifference(HITSResult result1, HITSResult result2) {
		double authsWithoutSynonyms[] = result1.auths;
		double hubsWithoutSynonyms [] = result1.hubs;
		
		double authsWithSynonyms [] = result2.auths;
		double hubsWithSynonyms [] = result2.hubs;
		
		int checkTop = 10000;
		if (checkTop <= 0) {
			checkTop = result1.hubs.length;
		} else {
			checkTop = Math.min(result1.hubs.length, checkTop);
		}
		
		System.out.println("AUTHS:");
		{
			double totalDifference = 0;
			for (int i = 0; i < checkTop; i++) {
				totalDifference += Math.abs(authsWithSynonyms[i] - authsWithoutSynonyms[i]);
			}
			double averageDifference = totalDifference / checkTop;
			double sumWithoutSynonyms = Util.getInstance().sum(authsWithoutSynonyms, checkTop);
			//double sumWithSynonyms = Util.getInstance().sum(authsWithoutSynonyms, checkTop);
			
			int totalDifferentRank = 0;
			for (int i = 0; i < checkTop; i++) {
				totalDifferentRank += (result1.authIndexes[i] != result2.authIndexes[i])?1:0;
			}
			this.totalDifferenceHubs[calculatedAmount] = totalDifference;
			this.totalDifferentHubs[calculatedAmount] = totalDifferentRank;
			this.averageDifferenceHubs[calculatedAmount] = averageDifference;
			System.out.println("Total difference: " + totalDifference + " average difference: " + averageDifference + 
					" " + totalDifference / sumWithoutSynonyms * 100 + "%" + " different rank: " + totalDifferentRank  + " / " + result1.authIndexes.length);
		}
		
		System.out.println("HUBS:");
		{			
			double totalDifference = 0;
			for (int i = 0; i < checkTop; i++) {
				totalDifference += Math.abs(hubsWithSynonyms[i] -hubsWithoutSynonyms[i]);
			}
			double averageDifference = totalDifference / checkTop;
			double sumWithoutSynonyms = Util.getInstance().sum(hubsWithoutSynonyms, checkTop);
			//double sumWithSynonyms = Util.getInstance().sum(hubsWithoutSynonyms, checkTop);
			
			int totalDifferentRank = 0;
			for (int i = 0; i < checkTop; i++) {
				totalDifferentRank += (result1.hubIndexes[i] != result2.hubIndexes[i])?1:0;
			}
			this.totalDifferenceAuths[calculatedAmount] = totalDifference;
			this.totalDifferentAuths[calculatedAmount] = totalDifferentRank;
			this.averageDifferenceAuths[calculatedAmount] = averageDifference;
			calculatedAmount++;
			System.out.println("Total difference: " + totalDifference + " average difference: " + averageDifference + 
					" " + totalDifference / sumWithoutSynonyms * 100 + "%" + " different rank: " + totalDifferentRank + " / " + result1.hubIndexes.length);
		}
	}

	public void printTop(double[] auths, double[] hubs) throws Exception {
		int topNum = 10;	
		//AUTHS
		int[] topAuths = Util.getInstance().getTopNIndexes(auths, topNum); 
		System.out.println("Authorities - top " + topNum + " : ");
		for (int i = 0; i < topNum; i++) {
			int id = topAuths[i];			
			System.out.println(id + " " + auths[id] + " " +  " " + DBConnector.getInstance().getPersonURI(id));
		}
		
		//HUBS
		int[] topHubs = Util.getInstance().getTopNIndexes(hubs, topNum); 		
		System.out.println("Hubs - top " + topNum + " : ");
		for (int i = 0; i < topNum; i++) {
			int id = topHubs[i];
			System.out.println(id + " " + hubs[id] + " " +  " " + DBConnector.getInstance().getPersonURI(id));
		}
	}
}