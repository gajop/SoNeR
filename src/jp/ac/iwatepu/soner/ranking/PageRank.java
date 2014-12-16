package jp.ac.iwatepu.soner.ranking;

import java.util.Date;
import java.util.Vector;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;

public class PageRank {
	int NUM_ITERS = 200;
	
	public PageRankResult run(int graphSize, Vector<Integer>[] connections, double initialPR) throws Exception {
		Util.getInstance().logIfDebug("Initializing...");
		double ranks [] = new double[graphSize];
		double tmpRanks [] = new double[graphSize];
		//initialize
		for (int i = 0; i < graphSize; i++) {
			ranks[i] = initialPR;
			tmpRanks[i] = initialPR;
		}
		System.out.println("Initial sum: ");
		System.out.println(Util.getInstance().sum(ranks));

		System.out.println("Num ranks: " + tmpRanks.length);
		
		Util.getInstance().logIfDebug("Staritng Page Rank Main...");
		Date startTime = new Date();
		for (int ITER = 0; ITER < NUM_ITERS; ITER++) {
			for (int personId = 0; personId < graphSize; personId++) {
				for (int knownPersonId : connections[personId]) {
					tmpRanks[knownPersonId] += (1 - initialPR) * ranks[personId] / connections[personId].size();					
				}
			}

			//update pageRank vectors
			for (int i = 0; i < graphSize; i++) { 			
				ranks[i] = tmpRanks[i];
				tmpRanks[i] = initialPR;
			}
			
			//Util.getInstance().logIfDebug("Iteration " + (ITER + 1) + " complete.");
		}
		Date endTime = new Date();
		double seconds = (endTime.getTime() - startTime.getTime()) / 1000.0;
		System.out.println("Elapsed time is " + seconds + " seconds.");
		Util.getInstance().logIfDebug("Done");
		printTop(ranks);
		return new PageRankResult(Util.getInstance().getTopNIndexes(ranks, ranks.length), ranks);
	}
	
	
	public void printTop(double[] ranks) throws Exception {
		int topNum = 10;
		int[] topRanks = Util.getInstance().getTopNIndexes(ranks, topNum); 
		
		System.out.println("Ranks - top " + topNum + " : ");
		for (int i = 0; i < topNum; i++) {
			int id = topRanks[i];
			System.out.println((i + 1) + ". " + id + " " + ranks[id] + " " + DBConnector.getInstance().getPersonURI(id));
			try {
				String name = DBConnector.getInstance().getPersonName(id);
				if (name != null) {
					System.out.println("Name: " + name);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
