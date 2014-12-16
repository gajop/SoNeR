package jp.ac.iwatepu.soner.ranking;

public class PageRankResult {
	public int[] indexes;
	public double[] ranks;
	public int[] getIndexes() {
		return indexes;
	}
	public void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}
	public double[] getRanks() {
		return ranks;
	}
	public void setRanks(double[] ranks) {
		this.ranks = ranks;
	}
	public PageRankResult(int[] indexes, double[] ranks) {
		super();
		this.indexes = indexes;
		this.ranks = ranks;
	}
	
}