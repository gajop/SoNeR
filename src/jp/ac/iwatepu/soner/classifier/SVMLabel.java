package jp.ac.iwatepu.soner.classifier;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBConnector;

public class SVMLabel {
	protected int[] sameNames;
	protected SVMTrain svmTrain;
	protected boolean [] matches;
	
	Scanner sc;
	protected String[] tags;
	protected String[][] values;
	
	public enum LabelResult { SAME, DIFFERENT, SKIP, END };
	
	static final Logger logger = LogManager.getLogger("SVM");
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SVMLabel svmLabel = new SVMLabel();
		svmLabel.run();
	}
	
	public LabelResult isSame(int i) throws Exception {
		int id1 = sameNames[i];
		int id2 = sameNames[i+1];
		logger.info(id1 + " " + id2);
		
		String uri1 = DBConnector.getInstance().getPersonURI(id1);
		String uri2 = DBConnector.getInstance().getPersonURI(id2);
		logger.info(uri1 + " ||| " + uri2);
		for (int j = 0; j < tags.length; j++) {
			String tag = tags[j];
			String val1 = values[j][id1];
			String val2 = values[j][id2];
			if (!val1.equals("") || !val2.equals("")) {
				logger.info(tag + ":| " + val1 + " ||| " + val2);
			}
		}
		boolean parsed = false;
		while (!parsed) {
			String line = sc.next();				
			if (line.charAt(0) == 'y') {
				parsed = true;
				return LabelResult.SAME;
			} else if (line.charAt(0) == 'n') {
				parsed = true;
				return LabelResult.DIFFERENT;
			} else if (line.charAt(0) == 'e') {
				return LabelResult.END;
			} else if (line.charAt(0) == 's') {
				return LabelResult.SKIP;
			}
		}
		return LabelResult.SKIP;
	}

	public void run() throws Exception {
		sameNames = DBConnector.getInstance().getPeopleWithSimilarAttributes();
		logger.info(sameNames.length);	
		
		svmTrain = new SVMTrain();
		svmTrain.fetch();
		matches = new boolean[sameNames.length / 2];
		
		sc = new Scanner(System.in);
		tags = svmTrain.tags;
		values = svmTrain.values;
		
		int endLevel = 0;
		for (int i = 0; i < sameNames.length; i += 2) {
			LabelResult labelResult = isSame(i);
			if (labelResult.equals(LabelResult.SAME)) {
				matches[i/2] = true;
			} else if (labelResult.equals(LabelResult.DIFFERENT)) {
				matches[i/2] = false;
			} else if (labelResult.equals(LabelResult.SKIP)) {
				continue;
			} else if (labelResult.equals(LabelResult.END)) {
				break;
			}
		}
		PrintWriter pw = new PrintWriter(new File("labels.csv"));
		for (int i = 0; i <= endLevel; i++) {
			int id1 = sameNames[i * 2];
			int id2 = sameNames[i * 2 + 1];
			boolean match = matches[i];
			pw.println(id1 + "," + id2 + "," + match);
		}
		pw.close();
	}
}
