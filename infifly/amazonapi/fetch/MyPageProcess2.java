package infifly.amazonapi.fetch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ansj.sun.fetch.amazonapi.SyncApiClient_Amazon;
import ansj.sun.fetch.amazonapi.api.bean.AmazonList;
import ansj.sun.fetch.amazonapi.api.bean.AmazonReview;
import ansj.sun.fetch.amazonapi.api.bean.AmazonReviews;
import ansj.sun.fetch.amazonapi.api.core.Page;
import ansj.sun.fetch.amazonapi.api.core.PageProcess;
import ansj.sun.fetch.amazonapi.api.core.Request;

import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.fastjson.JSON;

public class MyPageProcess2 implements PageProcess {
	private static SyncApiClient_Amazon asyncClient = null;
	private ReentrantLock lock = new ReentrantLock();
	private boolean hasinit=false;
	private boolean hasinit2=false;
	private Logger logger = Logger.getLogger(getClass());
	private final Lock lock3 = new ReentrantLock();
	BloomFilter<String> bf = new FilterBuilder(2_000_000_000, 0.01).buildBloomFilter();
	private Downloader d=new Downloader();
	private String site="us";
	@Override
	public void process(Request request) {
		// TODO Auto-generated method stub
		logger.info(">>>"+request.getUrl());
		this.init();
		if(request.getType().equals("1")){//内容页
			String asin=request.getUrl();//产品key
			int page=request.getPage();
			 
			for(int i=0;i<3;i++){
				try{
					Page pg=this.AmazonReivews(site, asin, page+"");
					AmazonReviews review=this.parsePage(pg);
					List<AmazonReview> lss=review.getComment_list();
					for(AmazonReview r:lss){
						String link=r.getReview_author_link();
						try {
							lock3.lock();
							if (!bf.contains(link)) {
								bf.add(link);
								List list=new ArrayList();
								list.add(link);
								FileUtils.writeLines(new File("allamazon.txt"), list,true);
							}	
						} finally {
							lock3.unlock();
						}
					}
					//下一页
					if(review.getHas_next().equals("true")){
						List<Request> pls =new ArrayList();
						pls.add(new Request(asin,"1",0,page+1));
						request.addRequests(pls);
					}
					break;
				}catch(Exception e){
					logger.info("download error :"+e.getMessage());
				}
			}
			
		}else{//列表页
			
			for(int i=0;i<3;i++){
				try{
					
					Page pg=this.AmazonList(request.getUrl());
					if(pg==null)break;
					AmazonList l=this.parseList(pg);
					List<String> list=l.getResults();
					List<Request> ls =new ArrayList();
					for(String p: list){
						ls.add(new Request(p,"1",0));
					}
					if(l.getNext_url()!=null){
						ls.add(new Request(l.getNext_url()));
					}else{
						logger.info("list end :"+request.getUrl());
					}
					request.addRequests(ls);
					break;
				}catch(Exception e){
					logger.info("download error :"+e.getMessage());
					if(i==2)return;
				}
			}
		}
		
	}
	
	private void init(){
		if(hasinit)return;
		lock.lock();
		if (hasinit2 == false) {
			try{
				BufferedReader urlBrTemp = new BufferedReader(
						new InputStreamReader(new FileInputStream(new File("allamazon.txt"))));
				String temp = null;
				while ((temp = urlBrTemp.readLine()) != null) {
					if(!bf.contains(String.valueOf(temp))){
						bf.add(String.valueOf(temp));
					}
				}
				urlBrTemp.close();
				hasinit=true;
				hasinit2=true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		lock.unlock();
	}
	
	
	public List getKey(String url){
		url=url+"&";
		List list=new ArrayList();
		Pattern p2=Pattern.compile("keywords=(.*?)&"); 
		Matcher m2=p2.matcher(url); 
		if(m2.find()){
			String a=m2.group(1);
			list.add(a);
		}
		Pattern p=Pattern.compile("page=(\\d+)"); 
		Matcher m=p.matcher(url); 
		if(m.find()){
			String a=m.group(1);
			//System.out.println(a);
			list.add(a);
		}
		return list;
	}
	
	public Page AmazonReivews(String site, String asin, String page){
		String url="http://xxx.xxx.xxx.xx:5000/tembin/amazon-data-api/1.0.0/comment-info"+"?site="+site+"&asin="+asin+"&page="+page;
		Request request=new Request(url);
		return d.download(request);
	}
	
	//生成自己的url
	public Page AmazonList(String url){
		String u="http://xxx.xxx.xxx.xx:5000/tembin/amazon-data-api/1.0.0/one-page-asins-by-keywords?site="+site;
		List ls=this.getKey(url);
		if(ls.size()==2){
			String keyword=(String)ls.get(0);
			String page=(String)ls.get(1);
			u=u+"&page="+page+"&keywords="+keyword;
			Request request=new Request(u);
			return d.download(request);
		}
		return null;
	}
	
	public   AmazonReviews parsePage(Page page) {
		AmazonReviews o=new AmazonReviews();
		try {
			logger.info("response code = " + page.getCode());
			int code=page.getCode();
			o.setCode(code);
			if(code==500|| code==400||code==503){
				return o;
			}
			if(code==200){
				AmazonReviews a=JSON.parseObject(new String(page.getContent(), "utf-8"),AmazonReviews.class);
				a.setCode(code);
	    		return a;
			}
		} catch (Exception e) {
			logger.info("parse error :"+e.getMessage());
		}
		return o;
	}
	
	public   AmazonList parseList(Page page) {
		AmazonList o=new AmazonList();
		try {
			logger.info("response code = " + page.getCode());
			int code=page.getCode();
			o.setCode(code);
			if(code==500|| code==400||code==503){
				return o;
			}
			if(code==200){
				AmazonList a=JSON.parseObject(new String(page.getContent(), "utf-8"),AmazonList.class);
				a.setCode(code);
	    		return a;
			}
		} catch (Exception e) {
			logger.info("parse error :"+e.getMessage());
		}
		return o;
	}
}
