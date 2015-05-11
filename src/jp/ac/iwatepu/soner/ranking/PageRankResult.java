package jp.ac.iwatepu.soner.ranking;

/**
 * Utility class for holding results of the PageRank algorithm (ranks of each person)
 * @author gajop
 *
 */
public class PageRankResult {
	public int[] indexes;
	public double[] ranks;
	
	public PageRankResult(int[] indexes, double[] ranks) {
		super();
		this.indexes = indexes;
		this.ranks = ranks;
	}
	
}