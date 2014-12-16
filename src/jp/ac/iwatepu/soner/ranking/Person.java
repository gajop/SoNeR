package jp.ac.iwatepu.soner.ranking;

public class Person {
	int id;
	String localURL;
	String context;
	boolean isValidURL;
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
