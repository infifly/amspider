package infifly.amazonapi.fetch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ansj.sun.fetch.amazonapi.SyncApiClient_Amazon;
import ansj.sun.fetch.amazonapi.api.bean.AmazonList;
import ansj.sun.fetch.amazonapi.api.bean.AmazonReview;
import ansj.sun.fetch.amazonapi.api.bean.AmazonReviews;
import ansj.sun.fetch.amazonapi.api.core.Request;
import ansj.sun.fetch.amazonapi.api.core.Spider;

import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.fastjson.JSON;

public class AmazonFetch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new AmazonFetch().run();
		// System.out.println(new Test().getRandKeyword());
	}
	
	public void run(){
		List list=new ArrayList();
		 
		try{
			String u="https://www.amazon.com/s/ref=sr_pg_3/136-5963965-2497865?rh=i%3Aaps%2Ck%3Aiphone+case&page=1&ie=UTF8&keywords=";
			List ls=FileUtils.readLines(new File("/root/keywords.csv"));
			Iterator itr=ls.iterator();
			while(itr.hasNext()){
				String k=(String)itr.next();
				k=k.trim();
				k=k.replaceAll(" +"," ");
				k=k.replace(" ", "+");
				list.add(new Request(u+k+"|0|0"));
			}
		 
		}catch(Exception e){
			e.printStackTrace();
		}
	 
		new Spider().setDatadir("amazon002").setThreads(100).setPageProcess(new MyPageProcess2()).setStartUrls(list).run();
	}
	
	
	 
}
