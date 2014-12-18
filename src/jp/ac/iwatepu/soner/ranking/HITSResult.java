package jp.ac.iwatepu.soner.ranking;

public class HITSResult {
	public int[] authIndexes;
	public int[] hubIndexes;
	public double[] hubs;
	public double[] auths;
	
	public HITSResult(int[] authIndexes, int[] hubIndexes, double[] hubs,
			double[] auths) {
		super();
		this.authIndexes = authIndexes;
		this.hubIndexes = hubIndexes;
		this.hubs = hubs;
		this.auths = auths;
	}
}
