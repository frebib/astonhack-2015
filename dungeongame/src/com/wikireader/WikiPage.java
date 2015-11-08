package com.wikireader;

public class WikiPage {
	public String[] 	links;
	public String[]		imageURLs;
	public String 		Title;
	
	
	public WikiPage(String[] links, String[] imageURLs, String Title) {
		this.links = links;
		this.imageURLs = imageURLs;
		this.Title = Title;
	}
	
}
