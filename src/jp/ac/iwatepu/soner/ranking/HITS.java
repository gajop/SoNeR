package jp.ac.iwatepu.soner.ranking;

import java.util.Date;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jp.ac.iwatepu.soner.Util;

/**
 * HITS algorithm implementation
 * @author gajop
 *
 */
public class HITS {
	private int NUM_ITERS = 200;
	private static final Logger logger = LogManager.getLogger("HITS");

	public HITSResult run(int graphSize, Vector<Integer>[] connections, double initialValues) throws Exception {
		logger.info("Staritng HITS Main...");		
		Date startTime = new Date();
		double hubs [] = new double[graphSize];
		double auths [] = new double[graphSize];
		
		logger.info("Initializing...");
		for (int i = 0; i < graphSize; i++) {
			hubs[i] = initialValues;
			auths[i] = initialValues;
		}
		
		logger.info("Starting algorithm...");
		for (int ITER = 0; ITER < NUM_ITERS; ITER++) {			
		    for (int personId = 0; personId < graphSize; personId++) {
				auths[personId] = 0;
			}
			for (int personId = 0; personId < graphSize; personId++) {
				for (int knownPersonId : connections[personId]) {
					auths[knownPersonId] += hubs[personId];
				}
			}			
			
			double norm = 0;
			for (int i = 0; i < auths.length; i++) {
				norm += auths[i] * auths[i]; 
			}
			norm = Math.sqrt(norm);			
			for (int i = 0; i < auths.length; i++) {
				auths[i] /= norm;
			}						
			
			for (int personId = 0; personId < graphSize; personId++) {
				hubs[personId] = 0;
			}
			for (int personId = 0; personId < graphSize; personId++) {
				for (int knownPersonId : connections[personId]) {
					hubs[personId] += auths[knownPersonId];
				}
			}
			
			norm = 0;
			for (int i = 0; i < hubs.length; i++) {
				norm += hubs[i] * hubs[i]; 
			}
			norm = Math.sqrt(norm);
			
			for (int i = 0; i < hubs.length; i++) {
				hubs[i] /= norm;
			}
			
			logger.debug("Iteration " + (ITER + 1) + " complete.");
		}
		Date endTime = new Date();
		double seconds = (endTime.getTime() - startTime.getTime()) / 1000.0;
		logger.info("Elapsed time is " + seconds + " seconds.");
		logger.info("Done");
		
		return new HITSResult(Util.getInstance().getTopNIndexes(auths, auths.length), Util.getInstance().getTopNIndexes(hubs, hubs.length), auths, hubs);
	}
	
}
