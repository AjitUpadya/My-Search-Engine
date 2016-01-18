package com.ucr.beans;

public class ResultDoc implements Comparable<ResultDoc> {
	
	private String name;
	private String url;
	private String title;
	private String desc;
	private float score;
	
	public ResultDoc(String name, String url, String title, String desc, float score) {
		this.name = name;
		this.url = url;
		this.desc = desc;
		this.score = score;
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public float getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	
	public int compareTo(ResultDoc doc) {
		float score = doc.getScore();
		if(score > this.score) return 1;
		else return -1;
		//return score - this.score;
	}
}
