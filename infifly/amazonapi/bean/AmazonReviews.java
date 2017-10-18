package infifly.amazonapi.bean;

import java.util.List;

import ansj.sun.fetch.amazonapi.api.core.Bean;

public class AmazonReviews  extends Bean{
	public String has_next;
	public List<AmazonReview> comment_list;
	public String getHas_next() {
		return has_next;
	}
	public void setHas_next(String has_next) {
		this.has_next = has_next;
	}
	public List<AmazonReview> getComment_list() {
		return comment_list;
	}
	public void setComment_list(List<AmazonReview> comment_list) {
		this.comment_list = comment_list;
	}
	
}
