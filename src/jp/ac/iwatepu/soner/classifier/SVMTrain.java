package jp.ac.iwatepu.soner.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVMTrain {
	int pairsSize = 35;
	public String[][] values;
	svm_model model;
	int peopleSize = 0;
	public String[] tags = Util.getInstance().getTags();

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SVMTrain svmTrain = new SVMTrain();
		svmTrain.run();
	}
	
	public void fetch() throws Exception {
		peopleSize = DBConnector.getInstance().getPeopleSize();
		System.out.println("Fetching from DB...");
		HashMap<String, String[]> tagMapping = new HashMap<String, String[]>();
		values = new String[tags.length][];
		for (int i = 0; i < tags.length; i++) {
			String tag = tags[i];
			String[] fieldValue = DBConnector.getInstance().getField(tag);
			tagMapping.put(tag, fieldValue);
			values[i] = fieldValue;
		}
		System.out.println("Fetching from DB done.");
	}

	public void run() throws Exception {
		fetch();
		ArrayList<Integer> firstids = new ArrayList<Integer>();
		ArrayList<Integer> secondids = new ArrayList<Integer>();
		ArrayList<Boolean> values = new ArrayList<Boolean>();
		
		File labelFile = new File("labels.csv");
		if (labelFile.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(labelFile));		
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				String[] parts = line.split(",");
				int firstId = Integer.valueOf(parts[0]);
				int secondId = Integer.valueOf(parts[1]);
				boolean value = Boolean.valueOf(parts[2]);
				
				firstids.add(firstId);
				secondids.add(secondId);
				values.add(value);
			}
			br.close();
		}		
		
		trainSVM(firstids.toArray(new Integer[firstids.size()]), 
				secondids.toArray(new Integer[secondids.size()]), 
				values.toArray(new Boolean[values.size()]));
	}
	
	public void trainSVM(Integer[] firstids, Integer[] secondids, Boolean[] values) {

		svm_parameter param = new svm_parameter();

		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.degree = 3;
		param.gamma = 0;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 0.5;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		// build problem
		svm_problem prob = new svm_problem();	
		
		prob.l = firstids.length;		
		prob.y = new double[prob.l];
		
		prob.x = new svm_node[prob.l][tags.length];
		
		for (int i = 0; i < firstids.length; i++) {
			int firstId = firstids[i];
			int secondId = secondids[i];
			boolean value = values[i]; 
			svm_node[] nodes = addPair(firstId, secondId);
			for (int j = 0; j < nodes.length; j++) {
				prob.x[i][j] = nodes[j];
			}
			if (value) { 
				prob.y[i] = 1;
			} else {
				prob.y[i] = -1;
			}
		}
		
		System.out.println("Training SVM...");
		model = svm.svm_train(prob, param);
		System.out.println("Training complete");
	}
	
	public boolean arePairs(int id1, int id2) {		
		svm_node[] nodes = addPair(id1, id2);
		double result = svm.svm_predict(model, nodes);
		return result > 0;
	}

	private svm_node[] addPair(int firstPersonId, int secondPersonId) {
		svm_node[] nodes = new svm_node[tags.length];
		for (int i = 0; i < tags.length; i++) {
			String firstValue = values[i][firstPersonId];
			String secondValue = values[i][secondPersonId];
			int distance = LevenshteinDistance.computeLevenshteinDistance(
					firstValue, secondValue);
			svm_node newNode = new svm_node();
			newNode.index = i + 1;
			newNode.value = distance;
			nodes[i] = newNode;
		}
		return nodes;
	}
}
