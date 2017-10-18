package infifly.amazonapi.queue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
//import us.codecraft.webmagic.Request;

import org.apache.log4j.Logger;

import infifly.amazonapi.core.Queue;
import infifly.amazonapi.core.Request;
import ansj.sun.util.IOUtil;


public class FileCacheQueue implements Queue{
	private Logger logger = Logger.getLogger(getClass());
	private BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();
	private AtomicBoolean bk = new AtomicBoolean(false);
	private final Lock lock = new ReentrantLock();
	private final Lock lock2 = new ReentrantLock();
	private final Lock lock3 = new ReentrantLock();
	private final Lock lock4 = new ReentrantLock();
	private boolean isinit=false;
	private static final String LINE = "\n";
	private static String lineNumPath;
	public static String data = "data";
	private static LineNumberReader urlBr;
	private static File urlFile = null;
	private static FileChannel urlFc;
	BloomFilter<String> bf = new FilterBuilder(2_000_000, 0.01).buildBloomFilter();
	public void init(){	
		if(!isinit){
			initSystemData();
	
			String temp = null;
	
			// 保持上一次读取的行数
			lineNumPath = data + "/line.txt";
	
			// 加载网页定向规则
			// initConfigeureXml();
	
			// 网页网址
			urlFile = new File(data + "/url.txt");
			try {
				if (!urlFile.isFile()) {
					urlFile.createNewFile();
				}
				urlBr = new LineNumberReader(new InputStreamReader(
						new FileInputStream(urlFile)));
	
				// 将urlBr调整到合适的位置
				int lineNum = 0;
				File lineNumFile = new File(lineNumPath);
				if (lineNumFile.isFile()) {
					BufferedReader br = IOUtil.getBufferedReader(lineNumFile);
					temp = br.readLine();
					if (temp != null && temp.length() > 0) {
						lineNum = Integer.parseInt(temp);
					}
					for (int i = 0; i < lineNum; i++) {
						urlBr.readLine();
					}
				}
				 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println(data + "/url.txt 没有建立!");
				System.exit(-1);
			}
			isinit=true;
		}
	}
	/**
	 * 加载系统文件夹
	 * 
	 * @return
	 */
	public  String initSystemData() {
		/*
		 * String temp = rb.getString("data"); if (StringUtils.isNotBlank(temp)) {
		 * data = temp; }
		 */
		File f = new File(data);
		if (!f.isDirectory()) {
			System.err.println("系统配置文件是" + data
					+ " 这个目录没有建立.系统已经自动建立.目录下要求包含url.txt文件.存放种子文件");
			f.mkdirs();
			//System.exit(-1);
		}
		System.out.println("data dir is " + data);
		return data;
	}
	
	/**
	 * 初始化系统文件.
	 * 
	 * @author infifly
	 * @mail 50398712@qq.com
	 * @throws IOException
	 */
	public synchronized void initFileSystem() throws IOException {
		/*
		 * if (System.currentTimeMillis()<dateZero) { return ; }
		 */
		// 重新设置系统文件路径
		// resetSystemFile() ;
		// url写入通道
		if (urlFc == null) {
			urlFc = new FileOutputStream(urlFile, true).getChannel();
		}
		// 加载bloomfilter
		 
			BufferedReader urlBrTemp = new BufferedReader(
					new InputStreamReader(new FileInputStream(urlFile)));
			 
			
			String temp = null;
			while ((temp = urlBrTemp.readLine()) != null) {
				if(!bf.contains(String.valueOf(temp))){
					bf.add(String.valueOf(temp));
				}
			}
			 
			urlBrTemp.close();
		 
	}
	
	public FileCacheQueue(String path) {
		lock.lock();
		try{
			if(path!=null&&path.length()>0){
				data=data+"/"+path;
			}
			init();
			initFileSystem();
		}catch(Exception e){
			e.printStackTrace();
		}
		lock.unlock();
	}
	
	public synchronized void quit() {
		try {
			System.out.println(urlBr.getLineNumber() +">>"+getUrlsSize());
			urlFc.close();
			 
			// 更新列表行数
			IOUtil.write(lineNumPath,
					(urlBr.getLineNumber() - getUrlsSize()) + "");
			urlBr.close();
			
			System.out.println("文件流关闭完毕");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 返回当前在内存中的网址列表
	 */
	public int getUrlsSize(){
		return queue.size();
	}
	
	 
	public  void push(Request request) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("push to queue " + request.getUrl());
			}
			lock3.lock();
			StringBuilder sb = new StringBuilder();
			String url = request.getUrl();
			if (!bf.contains(url)) {
				bf.add(url);
				//queue.add(request);
				sb.append(url);
				sb.append("|");
				sb.append(request.getType());
				sb.append("|");
				sb.append(request.getScore());
				sb.append("|");
				sb.append(request.getPage());
				sb.append(LINE);
				ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes());
				try {
					urlFc.write(buffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		} finally {
			lock3.unlock();
		}
	}
	
	public synchronized Request poll() {
		lock2.lock();
		System.out.println("queue size: "+queue.size());
		try {
			while (queue.size() == 0) {
				System.out.println("内存中网址采集完毕!(finished all links of memery!)");
				List<Request> all = getUrls();
				if (all != null) {
					queue.addAll(all);
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println(".>>>>>"+bk.get());
				if(bk.get())return null;
			}
			
			while(queue.size()>0){
				Request re=queue.poll();
				if(re!=null&&re.getUrl().indexOf("#")>-1){
					continue;
				}
				return re;
			}
			return null;
			//return queue.poll();

		} finally {
			lock2.unlock();
		}
	}
	
	/**
	 * 从文件获取2000url
	 * @return
	 */
	public synchronized List<Request> getUrls() {

		List<Request> all = new ArrayList<Request>();
		String temp = null;
		try {
			lock4.lock();
			while (all.size() < 200 && (temp = urlBr.readLine()) != null) {
				all.add(new Request(temp));
			}

			// 保存当前读取到的文本行
			IOUtil.write(lineNumPath, urlBr.getLineNumber() + "");
			lock4.unlock();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {

			// urlLock.unlock();
		}
		return all;

	}
	
	public void shutdown() {
		// TODO Auto-generated method stub
		bk.set(true);
		this.quit(); 
	}
}
