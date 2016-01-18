package com.ucr.controller;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ucr.crawler.MainCrawler;

public class HomeController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		String seedPath = readProperty("seedPath");
		String crawlerOutput = readProperty("crawlerOutput");
		String numOfHops = readProperty("numOfHops");
		String numOfPages = readProperty("numOfPages");
		if(seedPath != null && crawlerOutput != null && numOfHops != null && numOfPages != null ) {
			MainCrawler home = new MainCrawler(Integer.parseInt(readProperty("numOfPages")), readProperty("seedPath"), readProperty("crawlerOutput"), Integer.parseInt(readProperty("numOfHops")));
		}
		RequestDispatcher rd = null;
		rd = request.getRequestDispatcher("/home.jsp");
		try {
			rd.forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readProperty(String key) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		String value = "";
		try {
			properties.load(classLoader.getResourceAsStream("config.properties"));
			value = properties.getProperty(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "";
		}
		return value;

	}
}
