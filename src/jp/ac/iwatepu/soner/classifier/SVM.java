package jp.ac.iwatepu.soner.classifier;

import java.util.Vector;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;

public class SVM {
	String[] tags = Util.getInstance().getTags();

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
		int [] potentialCandidates = DBConnector.getInstance().getPotentialCandidates();
		int potentialCandidatesSize = potentialCandidates.length / 2;
		
		System.out.println("Total potential candidates: " + potentialCandidatesSize);
		int pairAmount = 0;
		int percent = potentialCandidates.length / 100;
		for (int i = 0; i < potentialCandidates.length; i += 2) {
			int id1 = potentialCandidates[i];
			int id2 = potentialCandidates[i+1];
			if (percent != 0 && i % percent == 0) {
				System.out.println((i / percent) + "%");
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
				System.out.println(totalKnownId1 + " " + totalKnownId2);
				
				System.out.println(uri1 + " ||| " + uri2);
				for (int j = 0; j < tags.length; j++) {
					String tag = tags[j];
					String val1 = values[j][id1];
					String val2 = values[j][id2];
					if (!val1.equals("") || !val2.equals("")) {
						System.out.println(tag + ":| " + val1 + " ||| " + val2);
					}					
				}				
				System.out.println("Known people:");
				for (int[] totalKnown : new int [][]{totalKnownId1, totalKnownId2}) {
					for (int j = 0; j < totalKnown.length; j += 2) {
						int knownId1 = totalKnown[j];
						int knownId2 = totalKnown[j+1];
						
						String knownURI1 = DBConnector.getInstance().getPersonURI(knownId1);
						String knownURI2 = DBConnector.getInstance().getPersonURI(knownId2);
						System.out.print(knownURI1 + " ");
						for (int k = 0; k < 50 - knownURI1.length(); k++) {
							System.out.print(" ");
						}
						System.out.println(knownURI2);
					}
				}
				System.out.println();
				System.out.println();*/
			}
		}
		System.out.println("Pairs: " + pairAmount + "/" + potentialCandidates.length);
		int[] pairsArray = new int[pairs.size()];
		for (int i = 0; i < pairs.size(); i++) {
			pairsArray[i] = pairs.get(i);
		}
		return pairsArray;
	}
}
