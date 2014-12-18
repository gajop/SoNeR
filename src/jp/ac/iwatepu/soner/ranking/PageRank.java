package jp.ac.iwatepu.soner.ranking;

import java.util.Date;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.DBConnector;
import jp.ac.iwatepu.soner.Util;

public class PageRank {
	int NUM_ITERS = 200;
	
	private static final Logger logger = LogManager.getLogger("PageRank");
	
	public PageRankResult run(int graphSize, Vector<Integer>[] connections, double initialPR) throws Exception {
		logger.info("Initializing...");
		double ranks [] = new double[graphSize];
		double tmpRanks [] = new double[graphSize];
		//initialize
		for (int i = 0; i < graphSize; i++) {
			ranks[i] = initialPR;
			tmpRanks[i] = initialPR;
		}
		logger.info("Initial sum: ");
		logger.info(Util.getInstance().sum(ranks));

		logger.info("Num ranks: " + tmpRanks.length);
		
		logger.info("Staritng Page Rank Main...");
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
			
			logger.debug("Iteration " + (ITER + 1) + " complete.");
		}
		Date endTime = new Date();
		double seconds = (endTime.getTime() - startTime.getTime()) / 1000.0;
		logger.info("Elapsed time is " + seconds + " seconds.");
		logger.info("Done");
		printTop(ranks);
		return new PageRankResult(Util.getInstance().getTopNIndexes(ranks, ranks.length), ranks);
	}
	
	
	public void printTop(double[] ranks) throws Exception {
		int topNum = 10;
		int[] topRanks = Util.getInstance().getTopNIndexes(ranks, topNum); 
		
		logger.info("Ranks - top " + topNum + " : ");
		for (int i = 0; i < topNum; i++) {
			int id = topRanks[i];
			logger.info((i + 1) + ". " + id + " " + ranks[id] + " " + DBConnector.getInstance().getPersonURI(id));
			try {
				String name = DBConnector.getInstance().getPersonName(id);
				if (name != null) {
					logger.info("Name: " + name);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
