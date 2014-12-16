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
	public int[] getAuthIndexes() {
		return authIndexes;
	}
	public void setAuthIndexes(int[] authIndexes) {
		this.authIndexes = authIndexes;
	}
	public int[] getHubIndexes() {
		return hubIndexes;
	}
	public void setHubIndexes(int[] hubIndexes) {
		this.hubIndexes = hubIndexes;
	}
	public double[] getHubs() {
		return hubs;
	}
	public void setHubs(double[] hubs) {
		this.hubs = hubs;
	}
	public double[] getAuths() {
		return auths;
	}
	public void setAuths(double[] auths) {
		this.auths = auths;
	}
	 
}
