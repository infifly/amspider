package infifly.amazonapi.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import infifly.amazonapi.queue.FileCacheQueue;

public class Spider {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	 
	}
	private Logger logger = Logger.getLogger(getClass());
	private String datadir="amazon001";
	private int threads=10;
	private Threadpool threadPool;
	public FileCacheQueue fqueue;
	private final AtomicLong pageCount = new AtomicLong(0);
	private List<Request> urls=new ArrayList();
	
	private PageProcess pageProcess;
	
	public PageProcess getPageProcess() {
		return pageProcess;
	}
	
	public Spider setStartUrls(List<Request> urls){
		this.urls=urls;
		return this;
	}
	/**
	 * 开始url
	 * @param url
	 * @return
	 */
	public Spider setStartUrl(String url){
		Request r=new Request(url);
		List list=new ArrayList();
		list.add(r);
		this.urls=list;
		return this;
	}

	public Spider setPageProcess(PageProcess pageProcess) {
		this.pageProcess = pageProcess;
		return this;
	}
	
	public String getDatadir() {
		return datadir;
	}

	public Spider setDatadir(String datadir) {
		this.datadir = datadir;
		return this;
	}
	

	public int getThreads() {
		return threads;
	}

	public Spider setThreads(int threads) {
		this.threads = threads;
		return this;
	}

	public void init(){
		threadPool=new Threadpool(threads);
		fqueue=new FileCacheQueue(datadir);
	}
	
	public void run(){
		init();
		if(this.urls==null){
			logger.error("please init start urls..");
			return;
		}
		 
		for(Request r : this.urls){
			fqueue.push(r);
		}
		 
		while(true){
			List<Request> urls=null;
			try{
				Thread.sleep(1000);
			}catch(Exception e2){}
			
			urls=fqueue.getUrls();
			for(Request request:urls){
				threadPool.execute(new Runnable() {
	                @Override
	                public void run() {
	                    try {
	                        processRequest(request);
	                    } catch (Exception e) {
	                        logger.error("process request " + request + " error", e);
	                    } finally {
	                        pageCount.incrementAndGet();
	                    }
	                }
				});
			}
			if(urls.size()==0 && threadPool.getCurrentThread()==0){
				break;
			}
		}
		threadPool.shutdown();
	}
	
	/**
	 * 下载数据//
	 * @param request
	 */
	public void processRequest(Request request){
		 pageProcess.process(request);
		 List<Request> ls=request.getRequests();
		 for(Request r : ls){
			 fqueue.push(r);
		 }
	}

}
