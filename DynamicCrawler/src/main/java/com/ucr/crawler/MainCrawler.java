package com.ucr.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.LoggerFactory;

/**
 * Handles requests for the application home page.
 */
public class MainCrawler {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainCrawler.class);
	private static List<String> inputArgs = new ArrayList<String>();
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	public MainCrawler(int numOfPages, String inputFilePath, String outputDir, int depth) {
		logger.info("Welcome to the demo of a web crawler!");
		startProcess(numOfPages, inputFilePath, outputDir, depth);
		//startProcess(Integer.parseInt(inputArgs.get(0)), inputArgs.get(1), inputArgs.get(2), Integer.parseInt((inputArgs.get(3))));
	}
	
	public MainCrawler() {
		logger.info("Welcome to the demo of a web crawler!");
		startProcess(Integer.parseInt(inputArgs.get(0)), inputArgs.get(1), inputArgs.get(2), Integer.parseInt((inputArgs.get(3))));
	}
	
	//invoke this method with params {num of pages to crawl, input dir, output dir}
	public static void startProcess(int numOfPages, String inputFilePath, String outputDir, int depth) {
		logger.info("Starting crawl now !!!");
		List<String> seedUrls = readMultipleSeedUrls(inputFilePath);
		if(!(seedUrls == null)) {
			try{
				ThreadController controller = new ThreadController();
				controller.startThreads(numOfPages, seedUrls, outputDir, depth);
			} catch (Exception e) {
				logger.error("Failed to initiate crawler. Please try again");
			}
		}
		//write to file and log
		System.out.println("Crawling completed successfully");
	}
	
	public static List<String> readMultipleSeedUrls(String inputFilePath) {
		List<String> list = new ArrayList<String>();
		try {
			File file = new File(inputFilePath);
			if(file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		 
				String aLine = null;
				while ((aLine = in.readLine()) != null) {
					if(aLine.trim().endsWith(".edu") || aLine.trim().endsWith(".edu/")) {
						list.add(aLine.trim());
					} else logger.error("Failed to read the url '"+ aLine.trim() +"' Please try again with a seed url that ends with .edu");
				}
				in.close();
				fis.close();
			}
		} catch(Exception e) {
			logger.error("Failed to read the seed file. Please try again with each seed url in a new line");
			return null;
		}
		return list;
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		if(args != null) {
			for(String arg : args) {
				inputArgs.add(arg);
			}
		}
		/*if(inputArgs.size() != 4) {
			System.out.println("The input parameters dont match. Please invoke the program with the following input parameters {NumberOfPages} {PathToSeedTextFile} {PathToOutputDir}"
					+ "{#OfHopsToCrawl}");
			System.exit(0);
		}*/
		//HomeController home = new HomeController();
		//MainCrawler home = new MainCrawler(15000, "/home/ajit/Desktop/seed.txt", "/home/ajit/Documents/Crawler_output", 10000);
	}
}
