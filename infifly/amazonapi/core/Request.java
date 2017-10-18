package infifly.amazonapi.core;

import java.util.ArrayList;
import java.util.List;

public class Request {
	public String url;
	public String type; //1:产品页 
	public int score; //分值
	public int page=0;
	
	public List<Request> requests=new ArrayList();
	
	public Request(String str){
		String strs[]=str.split("\\|"); 
		if(strs.length==4){
			this.url=(strs[0]);
			this.type=(strs[1]);
			this.score=Integer.parseInt(strs[2]);
			this.page=Integer.parseInt(strs[3]);
		}else if(strs.length==3){
			this.url=(strs[0]);
			this.type=(strs[1]);
			this.score=Integer.parseInt(strs[2]);
		}else{
			this.url=strs[0];
			this.type="";
			this.score=0;
		}
	}
	
	
	public Request(String url,String type,int score){
		this.url=url;
		this.type=type;
		this.score=score;
	}
	
	public Request(String url,String type,int score,int page){
		this.url=url;
		this.type=type;
		this.score=score;
		this.page=page;
	}
	
	
	
	public int getPage() {
		return page;
	}


	public void setPage(int page) {
		this.page = page;
	}


	public List<Request> getRequests() {
		return requests;
	}

	public void addRequests(List<Request> requests) {
		this.requests = requests;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	
}
