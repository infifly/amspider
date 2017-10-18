package infifly.amazonapi.bean;

import java.util.List;

import ansj.sun.fetch.amazonapi.api.core.Bean;

public class AmazonList extends Bean{
	
	public List results;
	public String next_url;
	
	public List getResults() {
		return results;
	}
	
	public void setResults(List results) {
		this.results = results;
	}
	
	public String getNext_url() {
		return next_url;
	}
	
	public void setNext_url(String next_url) {
		this.next_url = next_url;
	}
}
