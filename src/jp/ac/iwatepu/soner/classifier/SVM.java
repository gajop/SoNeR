package jp.ac.iwatepu.soner.classifier;

import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;

public class SVM {
	String[] tags = Util.getInstance().getTags();

	static final Logger logger = LogManager.getLogger("SVM");
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		SVM svm = new SVM();
		svm.run();
	}
	
	public int[] run() throws Exception {
		SVMTrain svmTrain = new SVMTrain();
		svmTrain.run();
		//String[][] values = svmTrain.values;
		
		Vector<Integer> pairs = new Vector<Integer>();
		int [] potentialCandidates = DBConnector.getInstance().getPeopleWithSimilarAttributes();
		int potentialCandidatesSize = potentialCandidates.length / 2;
		
		logger.info("Total potential candidates: " + potentialCandidatesSize);
		int pairAmount = 0;
		int percent = potentialCandidates.length / 100;
		for (int i = 0; i < potentialCandidates.length; i += 2) {
			int id1 = potentialCandidates[i];
			int id2 = potentialCandidates[i+1];
			if (percent != 0 && i % percent == 0) {
				logger.info((i / percent) + "%");
			}
			if (svmTrain.arePairs(id1, id2)) {
				pairs.add(id1);
				pairs.add(id2);
				pairAmount++;
				/*
				 * TODO: needed?
				String uri1 = DBConnector.getInstance().getPersonURI(id1);
				String uri2 = DBConnector.getInstance().getPersonURI(id2);
				
		//		if ((!uri1.contains("identi") && !uri1.contains("advogato")) || (!uri2.contains("identi") && !uri2.contains("advogato"))) {
		//			continue;					
		//		}
				int count = 0;
				for (int j = 0; j < tags.length; j++) {
					String tag = tags[j];
					String val1 = values[j][id1];
					String val2 = values[j][id2];
					if (!val1.equals("") && !val2.equals("")) {
						count++;
					}
				}
				if (count < 4) { 
					continue;
				}
				
				int totalKnownId1[] = DBConnector.getInstance().getAllKnownRelationshipsOfPerson(id1);
				int totalKnownId2[] = DBConnector.getInstance().getAllKnownRelationshipsOfPerson(id2);
				if (totalKnownId1.length < 1 || totalKnownId2.length < 1) {				
					continue;
				}
				logger.info(totalKnownId1 + " " + totalKnownId2);
				
				logger.info(uri1 + " ||| " + uri2);
				for (int j = 0; j < tags.length; j++) {
					String tag = tags[j];
					String val1 = values[j][id1];
					String val2 = values[j][id2];
					if (!val1.equals("") || !val2.equals("")) {
						logger.info(tag + ":| " + val1 + " ||| " + val2);
					}					
				}				
				logger.info("Known people:");
				for (int[] totalKnown : new int [][]{totalKnownId1, totalKnownId2}) {
					for (int j = 0; j < totalKnown.length; j += 2) {
						int knownId1 = totalKnown[j];
						int knownId2 = totalKnown[j+1];
						
						String knownURI1 = DBConnector.getInstance().getPersonURI(knownId1);
						String knownURI2 = DBConnector.getInstance().getPersonURI(knownId2);
						logger.info(knownURI1 + " ");
						for (int k = 0; k < 50 - knownURI1.length(); k++) {
							logger.info(" ");
						}
						logger.info(knownURI2);
					}
				}
				logger.info();
				logger.info();*/
			}
		}
		logger.info("Pairs: " + pairAmount + "/" + potentialCandidates.length);
		int[] pairsArray = new int[pairs.size()];
		for (int i = 0; i < pairs.size(); i++) {
			pairsArray[i] = pairs.get(i);
		}
		return pairsArray;
	}
}
