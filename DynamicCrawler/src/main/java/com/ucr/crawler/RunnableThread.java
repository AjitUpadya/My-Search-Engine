package com.ucr.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

public class RunnableThread implements Runnable{
	protected int id;
	protected final ConcurrentLinkedQueue<String> queue;
	protected final List<String> urlList;
	protected final String url;
	protected String rootPath;
	protected int counter = 19686;
	protected int localDepth;
	protected final int maxDepth;
	protected final String outputDir; 
	protected final int numOfPages;
	private Document doc;
	private boolean msgCounter = true; 
	private int countOfLinksFound;
	private int countOfFilesDownloaded;
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainCrawler.class);
	
	public RunnableThread(int numOfPages, String url, ConcurrentLinkedQueue<String> queue, List<String> list, String outputDir, int depth) {
		this.url = url;
		this.queue = queue;
		this.urlList = list;
		this.outputDir = outputDir;
		this.maxDepth = depth;
		this.localDepth = 0; 
		this.numOfPages = numOfPages;
	}
	
	public void run() {
		try {
			createCrawlRootDirectory(this.outputDir);
			writeToRecordTxt(url, outputDir);
			processPage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processPage() throws IOException {
		synchronized (queue) {
			while(!queue.isEmpty()) {
				String url = resolveUrl(queue.remove());
				if(url.length() > 0){
					if(!urlList.contains(url) && validateLink(url)) {
						String htmlContent = "";
						urlList.add(url);
						if(countOfFilesDownloaded <= numOfPages) {
							//download the file
							htmlContent = downloadFile(url);
						}
						if(localDepth <= maxDepth && countOfLinksFound <= numOfPages) {
							//extract links from its html
							extractLinksFromHtml(url, htmlContent);
						} else if(countOfLinksFound >= numOfPages && msgCounter) {
							msgCounter = false;
							System.out.println("Downloading pages now.. Please wait");
						}
						if(countOfLinksFound >= numOfPages && countOfFilesDownloaded >= numOfPages) {
							break;
						}
					}
				}
			}
			System.out.println("Total number of links found : "+countOfLinksFound);
			System.out.println("Count of valid files downloaded : "+countOfFilesDownloaded);
		}
	}
	
	public String resolveUrl(String link) {
		if(link.length() > 0) {
			if(link.indexOf("#") != -1) {
				link = link.split("#")[0];
			}
			if(link.charAt(link.length() - 1) == '/') {
				link = link.substring(0, link.length()-1);
			}
		}
		try{
			URL url = new URL(link);
			String protocol = url.getProtocol();	
			String host = url.getHost();
			if(host.indexOf("www") == -1) {
				if(urlList.contains(protocol+"://www."+host)) {
					return "";
				}
			}
			if(link.startsWith("/")) {
				link = url.getProtocol() + "//" + url.getHost() + (url.getPort() >= 0 ? "" + url.getPort(): "") + link;
			}
		} catch(Exception e) {
			return "";
		}
		return link.trim();
	}
	
	public void extractLinksFromHtml(String url, String htmlContent) {
		//Document doc;
		try {
			//doc = Jsoup.connect(url).ignoreHttpErrors(true).timeout(0).get();
			if(doc == null) doc = Jsoup.connect(url).ignoreHttpErrors(true).timeout(0).get();
			Elements elements = doc.select("a[href]");
			for(Element link : elements) {
				String anchor;
				if(link.attr("abs:href").length() > 0) anchor = link.attr("abs:href").trim();
				else anchor = link.attr("href").trim();
				//validate anchor tag before inserting into queue
				anchor = resolveUrl(anchor);
				
				synchronized (queue) {
					if(anchor.length() > 0 && !queue.contains(anchor) && countOfLinksFound <= numOfPages) {
						this.queue.add(anchor);
						countOfLinksFound++;
						writeToRecordTxt(anchor, outputDir);
						logger.info(anchor);
					}
				}
			
				if(countOfLinksFound >= numOfPages) {
					break;
				}
			}
			localDepth++;
		} catch (IOException e) {
			//TODO log msg here
			return;
		}
	}
	
	public boolean validateLink(String link) {
		if(!urlList.contains(link)) {
			URL url;
			//only crawl through html files
			if (link.contains("@") || link.contains(".js")
					|| link.contains(":80") || link.contains(".jpg") || link.contains(".css") 
						|| link.contains(".pdf") || link.contains(".png") || link.contains(".gif")) {
				return false;
			}
			//only allow http sites
			if(!link.toLowerCase().startsWith("http://")) {
				return false;
			}
			//verify format of url
			try{
				String[] schemes = {"http"};
				url = new URL(link);
				url.toURI();
				UrlValidator urlValidator = new UrlValidator(schemes);
				if(!urlValidator.isValid(link)) return false;
			} catch(Exception e) {
				return false;
			}
			
			// avoid spaces and malformed urls
			
			//check if robot is allowed to access the url
			return isRobotAllowed(link);
		}
		return false;
	}
	
	public boolean isRobotAllowed(String link) {
		try {
			doc = Jsoup.connect(link).ignoreHttpErrors(true).timeout(10*1000).get();
			if(checkMetaTagsSafe(doc)) {
				URL url = new URL(link);
				return robotSafe(url);
			} else return false;
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("Failed to download the page : '"+ link +"'");
		}
		return false;
	}
	
	private boolean checkMetaTagsSafe(Document doc) {
		Elements metalinks = doc.select("meta[name=robots]");
		if(metalinks != null) {
			for(Element link : metalinks) {
				String content = link.attr("content");
				if(content.length() > 0) {
					if(content.toLowerCase().indexOf("noindex") != -1) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean robotSafe(URL url) { 
	    String strHost = url.getHost();
	    String DISALLOW = "Disallow";
	    String strRobot = "http://" + strHost + "/robots.txt";
	    URL urlRobot;
	    try { urlRobot = new URL(strRobot);
	    } catch (MalformedURLException e) {
	        // something weird is happening, so don't trust it 
	        return false; 
	    } 
	 
	    String strCommands = "";
	    try  
	    { 
	        InputStream urlRobotStream = urlRobot.openStream();
	        byte b[] = new byte[10000];
	        int numRead = urlRobotStream.read(b);
	        while (numRead != -1) {
	        	strCommands = new String(b, 0, numRead);
	            numRead = urlRobotStream.read(b);
	            if (numRead != -1) 
	            { 
	                    String newCommands = new String(b, 0, numRead);
	                    strCommands += newCommands;
	            } 
	        } 
	       urlRobotStream.close();
	    }  
	    catch (IOException e) 
	    { 
	        return true; // if there is no robots.txt file, it is OK to search 
	    } 
	 
	    if (strCommands.contains(DISALLOW)) // if there are no "disallow" values, then they are not blocking anything.
	    { 
	        String[] split = strCommands.split("\n");
	        ArrayList<RobotRule> robotRules = new ArrayList<RobotRule>();
	        String mostRecentUserAgent = null;
	        for (int i = 0; i < split.length; i++) 
	        { 
	            String line = split[i].trim();
	            if (line.toLowerCase().startsWith("user-agent")) 
	            { 
	                int start = line.indexOf(":") + 1;
	                int end   = line.length();
	                mostRecentUserAgent = line.substring(start, end).trim();
	            } 
	            else if (line.startsWith(DISALLOW)) {
	                if (mostRecentUserAgent != null) {
	                    RobotRule r = new RobotRule();
	                    r.userAgent = mostRecentUserAgent;
	                    int start = line.indexOf(":") + 1;
	                    int end   = line.length();
	                    r.rule = line.substring(start, end).trim();
	                    robotRules.add(r);
	                } 
	            } 
	        } 
	 
	        for (RobotRule robotRule : robotRules)
	        { 
	            String path = url.getPath();
	            if (robotRule.rule.length() == 0) return true; // allows everything if BLANK
	            if (robotRule.rule == "/") return false;       // allows nothing if /
	 
	            if (robotRule.rule.length() <= path.length())
	            {  
	                String pathCompare = path.substring(0, robotRule.rule.length());
	                if (pathCompare.equals(robotRule.rule)) return false;
	            } 
	        } 
	    } 
	    return true; 
	} 
	
	public String downloadFile(String url) {
		String htmlContent = "";
		try {
			//doc = Jsoup.connect(url).ignoreHttpErrors(true).timeout(0).get();
			if(doc == null) doc = Jsoup.connect(url).ignoreHttpErrors(true).timeout(0).get();
			htmlContent = doc.outerHtml();
			File rootDir = createCrawlRootDirectory(this.outputDir);
			File dir = new File(".");
			String loc = rootDir.getCanonicalPath() + File.separator + "File "+ counter++ +".txt";
			File file = new File(loc);
			if(!file.exists()) {
				file.createNewFile();
			}
			//write the html content to the file
			writeFileToDir(file, htmlContent, url, doc.title());
		} catch (IOException e) {
			//TODO add error msg to log file
			e.printStackTrace();
		}
		return htmlContent;
	}
	
	public boolean writeToRecordTxt(String URL, String outputDir) throws IOException {
		String loc = outputDir + File.separator + "record.txt";
		File file = new File(loc);
		if(!file.exists()) file.createNewFile();
		
		if(!checkExist(URL, file)) {
			FileWriter fstream = new FileWriter(loc, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(URL);
			out.newLine();
			out.close();
			return true;
		}
		return false;
	}
	
	// given a String, and a File
	// return if the String is contained in the File
	public boolean checkExist(String s, File fin) throws IOException {
 
		FileInputStream fis = new FileInputStream(fin);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
 
		String aLine = null;
		while ((aLine = in.readLine()) != null) {
			// //Process each line
			if (aLine.trim().contains(s)) {
				in.close();
				fis.close();
				return true;
			}
		}
 
		// do not forget to close the buffer reader
		in.close();
		fis.close();
 
		return false;
	}

	public File createCrawlRootDirectory(String outputDir) throws IOException {
		//to create crawl root folder
		File dir = new File(".");
		//TODO change this location
		//rootPath = dir.getCanonicalPath() + File.separator + "Crawl "+ new SimpleDateFormat("EEE, MMM d, ''yy").format(new Date());
		rootPath = outputDir;
		File theDir = new File(rootPath);
		if(!theDir.exists()) {
		    try { 
		        theDir.mkdir();
		    } catch(SecurityException se){ }    
		}
		return theDir;
	}
	public boolean writeFileToDir(File file, String contents, String url, String title) throws IOException {
		PrintWriter out;
		boolean flag = false;
		try {
			file.getParentFile().mkdirs();
			
			out = new PrintWriter(file);
			out.println(contents);
			out.println(url);
			out.flush();
			out.close();
			flag = true;
			this.countOfFilesDownloaded++;
		} catch (FileNotFoundException e) {
			System.out.println("Failed to write file");
			e.printStackTrace();
		}
		return flag;
	}
	
	public String getUrl() {
		return url;
	}
	
	public long getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Queue<String> getQueue() {
		return queue;
	}
}
