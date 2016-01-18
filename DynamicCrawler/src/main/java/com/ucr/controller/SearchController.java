package com.ucr.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.ucr.beans.ResultDoc;
import com.ucr.service.IndexService;
import com.ucr.service.PageRank;
import com.ucr.service.WebGraph;

public class SearchController extends HttpServlet {
	
	public static int tfIdf;
	public static int pageRank;
	public static int tf_PageRank;
	public static PageRank pr;
	
	public SearchController() {
		IndexService service = new IndexService();
		try {
			service.indexAllFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// PageRank calculation
		WebGraph graph = new WebGraph();
		graph.createWebGrpahOfAllPages( IndexService.filePaths, IndexService.urls );
		pr = new PageRank( WebGraph.WebGraph, WebGraph.mInComingLinks);
		pr.calculatePageRank();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		String searchStr = request.getParameter("searchStr");
		boolean showAll = Boolean.parseBoolean(request.getParameter("showAll"));
		String scoringType = request.getParameter("scoringType");
		if(scoringType.length() > 0) {
			if(scoringType.equals("tf")) {
				tfIdf = 1; pageRank = 0; tf_PageRank = 0;
			} else if(scoringType.equals("pagerank")) {
				tfIdf = 0; pageRank = 1; tf_PageRank = 0;
			} else { 
				tfIdf = 0; pageRank = 0; tf_PageRank = 1; 
			}
		} else {
			tfIdf = 1; pageRank = 0; tf_PageRank = 0;
		}
		IndexService service = new IndexService();
		try {
			//service.indexAllFiles();
			List<ResultDoc> list = service.searchIndex(searchStr, showAll);
			
			if(pageRank == 1 || tf_PageRank == 1) {
				System.out.println(PageRank.PageRank.size());
				list = service.calculatePageRankScore(list, PageRank.PageRank);
			}
			
			// 2. initiate jackson mapper
	        ObjectMapper mapper = new ObjectMapper();
	 
	        // 4. Set response type to JSON
	        response.setContentType("application/json");       
	        mapper.writeValue(response.getOutputStream(), list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
