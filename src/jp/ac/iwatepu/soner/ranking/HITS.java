package jp.ac.iwatepu.soner.ranking;

import java.util.Date;
import java.util.Vector;

import jp.ac.iwatepu.soner.Util;

public class HITS {
	int NUM_ITERS = 200;
	

	public HITSResult run(int graphSize, Vector<Integer>[] connections, double initialValues) throws Exception {
		Util.getInstance().logIfDebug("Staritng HITS Main...");		
		Date startTime = new Date();
		double hubs [] = new double[graphSize];
		double auths [] = new double[graphSize];
		
		Util.getInstance().logIfDebug("Initializing...");
		for (int i = 0; i < graphSize; i++) {
			hubs[i] = initialValues;
			auths[i] = initialValues;
		}
		
		Util.getInstance().logIfDebug("Starting algorithm...");
		for (int ITER = 0; ITER < NUM_ITERS; ITER++) {			
		    for (int personId = 0; personId < graphSize; personId++) {
				auths[personId] = 0;//0.15;
			}
			for (int personId = 0; personId < graphSize; personId++) {
				for (int knownPersonId : connections[personId]) {
					auths[knownPersonId] += hubs[personId];//0.85 * hubs[personId];
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
				hubs[personId] = 0;//initialValues; FIXME: use initial values?
			}
			for (int personId = 0; personId < graphSize; personId++) {
				for (int knownPersonId : connections[personId]) {
					hubs[personId] += auths[knownPersonId];//0.85 * auths[knownPersonId];
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
			
	//		Util.getInstance().logIfDebug("Iteration " + (ITER + 1) + " complete.");
		}
		Date endTime = new Date();
		double seconds = (endTime.getTime() - startTime.getTime()) / 1000.0;
		System.out.println("Elapsed time is " + seconds + " seconds.");
		Util.getInstance().logIfDebug("Done");
		//printTop(auths, hubs);
		
		return new HITSResult(Util.getInstance().getTopNIndexes(auths, auths.length), Util.getInstance().getTopNIndexes(hubs, hubs.length), auths, hubs);
	}
	
}
