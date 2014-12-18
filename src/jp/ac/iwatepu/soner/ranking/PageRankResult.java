package jp.ac.iwatepu.soner.ranking;

public class PageRankResult {
	public int[] indexes;
	public double[] ranks;
	
	public PageRankResult(int[] indexes, double[] ranks) {
		super();
		this.indexes = indexes;
		this.ranks = ranks;
	}
	
}