package com.ucr.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;

import com.ucr.beans.ResultDoc;
import com.ucr.controller.HomeController;
import com.ucr.controller.SearchController;

public class IndexService {
	private String indexFilePath = HomeController.readProperty("indexPath");
	private String crawledFilesPath = HomeController.readProperty("crawlerOutput");
	private static StandardAnalyzer analyzer = new StandardAnalyzer();
	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();
	private JTidyHTMLHandler jTidy;
	
	public static ArrayList<String> filePaths = new ArrayList<String>();
	public static ArrayList<String> urls = new ArrayList<String>();
	public static ArrayList<File> files = new ArrayList<File>();
	
	public void indexAllFiles() throws Exception {
		jTidy = new JTidyHTMLHandler();
		createIndexDirectory();
		//pr.printFinalPageRank(4);
	}
	 
	public void createIndexDirectory() {
		File file = new File(indexFilePath);
		if(file.isDirectory()) {
			if(!(file.list().length == 0)) {
				return;
			}
		}
		try {
			Path path = Paths.get(indexFilePath);
		    FSDirectory dir = FSDirectory.open(path);
		    IndexWriterConfig config = new IndexWriterConfig(analyzer);
		    writer = new IndexWriter(dir, config);
		    indexFileOrDirectory(crawledFilesPath, writer);
		    closeIndex();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//return writer;
	}
	
	/**
   * Indexes a file or directory
   * @param fileName the name of a text file or a folder we wish to add to the index
   * @throws java.io.IOException when exception
   */
  public void indexFileOrDirectory(String fileName, IndexWriter writer) throws IOException {
    //===================================================
    //gets the list of files in a folder (if user has submitted
    //the name of a folder) or gets a single file name (is user
    //has submitted only the file name) 
    //===================================================
    addFiles(new File(fileName));
    
    int originalNumDocs = writer.numDocs();
    for (File f : queue) {
      FileReader fr = null;
      if(f.getPath().indexOf("record") == -1) {
	      try {
	        Document doc = new Document();
	
	        //===================================================
	        // add contents of file
	        //===================================================
	        fr = new FileReader(f);
	        BufferedReader br = new BufferedReader(fr);
	        String lastLine = "";
	        String sCurrentLine = "";
	        
	        while ((sCurrentLine = br.readLine()) != null)  { 
	            lastLine = sCurrentLine.trim();
	        }
	        doc = jTidy.getDocument(new FileInputStream(f));
	        doc.add(new Field("url", lastLine, Field.Store.YES, Field.Index.ANALYZED));
	        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
	        doc.add(new StringField("filename", f.getName(), Field.Store.YES));
	        
	        writer.addDocument(doc);
	        System.out.println("Added: " + f);
	        
	        //for pagerank
	        files.add(f);
	        urls.add(lastLine);
	        filePaths.add(f.getPath());
	        
	      } catch (Exception e) {
	        System.out.println("Could not add: " + f);
	      } finally {
	        fr.close();
	      }
      }
    }
    
    int newNumDocs = writer.numDocs();
    System.out.println("");
    System.out.println("************************");
    System.out.println((newNumDocs - originalNumDocs) + " documents added.");
    System.out.println("************************");

    queue.clear();
  }
  
  private void addFiles(File file) {
    if (!file.exists()) {
      System.out.println(file + " does not exist.");
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        addFiles(f);
      }
    } else {
      String filename = file.getName().toLowerCase();
      //===================================================
      // Only index text files
      //===================================================
      if (filename.endsWith(".htm") || filename.endsWith(".html") || 
              filename.endsWith(".xml") || filename.endsWith(".txt")) {
        queue.add(file);
      } else {
        System.out.println("Skipped " + filename);
      }
    }
  }
  
  public List<ResultDoc> searchIndex(String str, boolean showAll) throws IOException {
	List<ResultDoc> results = new ArrayList<ResultDoc>();   
	ResultDoc doc;
    Path path = Paths.get(indexFilePath);
    IndexReader reader = DirectoryReader.open(FSDirectory.open(path));
    IndexSearcher searcher = new IndexSearcher(reader);
    TopScoreDocCollector collector;
    String queryForSnippet = str;
      try {
    	if(showAll) collector = TopScoreDocCollector.create(100000);
    	else collector = TopScoreDocCollector.create(10);
        if(SearchController.tfIdf == 1 || SearchController.tf_PageRank == 1) {
        	str += "text:" + str + "^1" + "title:" + str+ "^1.5" + "meta:" + str+ "^1.5";
        } else if(SearchController.pageRank == 1) {
        	str += "text:" + str + "^1" + "title:" + str+ "^1" + "meta:" + str+ "^1";
        }
        Query q = new QueryParser("contents", analyzer).parse(str);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        //display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId); 
          String contents = d.get("contents"); 
          System.out.println((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
          String snippet = generateSnippets(contents, queryForSnippet);
          BigDecimal bd = new BigDecimal(Float.toString(hits[i].score)).setScale(2, BigDecimal.ROUND_HALF_UP);
          doc = new ResultDoc(d.get("filename"), d.get("url") , d.get("title"), snippet, bd.floatValue());
          results.add(doc);
        }
      } catch (Exception e) {
        System.out.println("Error searching " + str + " : " + e.getMessage());
      }
      
      // sort the results based on score in decreasing order
      Collections.sort(results);
      return results;
  }
  
  private String generateSnippets(String content, String qTerm) {
	  //split string into tokens
	  StringBuilder sb = new StringBuilder(); 
	  String[] tokens = content.trim().split(" ");
	  boolean flag = false;
	  for(int i = 0; i<tokens.length; i++) {
		  if(tokens[i].length() > 0) {
			  if(tokens[i].toLowerCase().equals(qTerm.toLowerCase())) {
				//if the term is in the beginning
				if(i-10 < 0) {
					for(int j=0; j<=i+20; j++) {
						if(tokens[j] != null && tokens[j].indexOf("=") == -1) {
					  		sb.append(tokens[j] + " ");
					  		flag = true;
				  		}
					}
				} else if(i+10 > tokens.length) {
					for(int j=i; j<=tokens.length; j++) {
						if(tokens[j] != null && tokens[j].indexOf("=") == -1) {
					  		sb.append(tokens[j] + " ");
					  		flag = true;
				  		}
					}
				} else {
				  	for(int j=i-10; j<= i+10; j++) {
				  		if(tokens[j] != null && tokens[j].indexOf("=") == -1) {
					  		sb.append(tokens[j] + " ");
					  		flag = true;
				  		}
				  	}
				}
			  	if(flag) break;
			  }
		  }
	  }
	  if(sb.length() > 0) return sb.toString().trim();
	  else return "";
  }
  
  public List<ResultDoc> calculatePageRankScore(List<ResultDoc> list, Map<String, Float> pageRank) {
	  pageRank = PageRank.PageRank;
	  String url = "";
	  float score = (float) 0.0;
		float pr = (float) 0.0;
		List<ResultDoc> rankedList = new ArrayList<ResultDoc>();
		
		for( ResultDoc sr : list ) {
			url = sr.getUrl();
			if( pageRank.get( url ) != null) {
				score = sr.getScore();
				pr = pageRank.get(url);
				score = score * pr; 
				//BigDecimal bd = new BigDecimal(Float.toString(score)).setScale(4, BigDecimal.ROUND_HALF_UP);
				//score = bd.floatValue();
				ResultDoc doc = new ResultDoc( sr.getName(), sr.getUrl(), sr.getTitle(), sr.getDesc(), score );
				rankedList.add( doc );
			}
		}
		
		Collections.sort(rankedList);
		return rankedList;
  }
  
  /**
   * Close the index.
   * @throws java.io.IOException when exception closing
   */
  public void closeIndex() throws IOException {
    writer.close();
  }
}
