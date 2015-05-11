package jp.ac.iwatepu.soner.ranking;

/**
 * Utility class for the minimal representation of a person
 * @author gajop
 *
 */
public class Person {
	private int id;
	private String localURL;
	private String context;
	private boolean isValidURL;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLocalURL() {
		return localURL;
	}
	public void setLocalURL(String localURL) {
		this.localURL = localURL;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public boolean isValidURL() {
		return isValidURL;
	}
	public void setValidURL(boolean isValidURL) {
		this.isValidURL = isValidURL;
	}
	public Person(int id, String localURL, String context, boolean isValidURL) {
		super();
		this.id = id;
		this.localURL = localURL;
		this.context = context;
		this.isValidURL = isValidURL;
	}
	
	
}
