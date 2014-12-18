package jp.ac.iwatepu.soner.classifier;

import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;

public class SVM {
	String[] tags = Util.getInstance().getTags();

	static final Logger logger = LogManager.getLogger("SVM");
	
	public static void main(String[] args) throws Exception {
		SVM svm = new SVM();
		svm.run();
	}
	
	public int[] run() throws Exception {
		SVMTrain svmTrain = new SVMTrain();
		svmTrain.run();
		
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
