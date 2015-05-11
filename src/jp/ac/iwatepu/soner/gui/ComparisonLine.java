package jp.ac.iwatepu.soner.gui;

import javafx.beans.property.SimpleStringProperty;

/**
 * Utility class for representing hubs, auths and page ranks in the result controller
 * @author gajop
 *
 */
public class ComparisonLine {
	private final SimpleStringProperty comparisonType = new SimpleStringProperty("");
	private final SimpleStringProperty pageRankDifference = new SimpleStringProperty("");
	private final SimpleStringProperty hubsDifference = new SimpleStringProperty("");
	private final SimpleStringProperty authsDifference = new SimpleStringProperty("");
	
	public ComparisonLine() {
		this("", 0, 0, 0);
	}
	public ComparisonLine(String comparisonType, double pageRankDifference,
			double hitsDifference, double authsDifference) {
		super();
		setComparisonType(comparisonType);
		setPageRankDifference(pageRankDifference);
		setHubsDifference(hitsDifference);
		setAuthsDifference(authsDifference);
	}

	public String getComparisonType() {
		return comparisonType.get();
	}

	public String getPageRankDifference() {
		return pageRankDifference.get();
	}

	public String getHubsDifference() {
		return hubsDifference.get();
	}

	public String getAuthsDifference() {
		return authsDifference.get();
	}
	
	public void setComparisonType(String comparisonType) {
		this.comparisonType.set(comparisonType);
	}
	
	public String doubleToTwoDigitString(double value) { 
		return String.valueOf(Integer.valueOf((int) (value * 100)) / 100.0);
	}
	public void setPageRankDifference(double pageRankDifference) {
		this.pageRankDifference.set(doubleToTwoDigitString(pageRankDifference) + "%");
	}
	public void setHubsDifference(double hubsDifference) {
		this.hubsDifference.set(doubleToTwoDigitString(hubsDifference) + "%");
	}
	public void setAuthsDifference(double authsDifference) {
		this.authsDifference.set(doubleToTwoDigitString(authsDifference) + "%");
	}
	
}