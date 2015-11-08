package com.wikireader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class WikiReader {

	public static JSONParser jsp = new JSONParser();
	
	public static void main(String[] args) throws Exception {
		
		// Get Random page
		String randPage = getRandomPageTitle();
		
		getPage( randPage );
	}
	
	public static WikiPage getPage( String name ) throws Exception {
				
		// Get Random page
		//String randPage = getRandomPageTitle();
		//System.out.println(randPage);
		
		// Get wiki page
		JSONObject wikiData = getWikiPageData( name );
		String[] ss 		= getImageURLSFromWikiData( wikiData );
		String[] links		= getPageLinks( wikiData );
		System.out.println(wikiData);
		System.out.println(Arrays.toString(ss));
		
		// Call on first image (Rather than all for now)
		int max = Math.min(5, ss.length);
		String[] slinks = new String[max];
		if( ss.length > 0 ) {
			
			for( int i=0; i < max; i++ ) {
				slinks[i] = getFullImageURL(ss[i]);
			}

		}else{
			System.out.println("ERROR! No images on URL");
		}
		
		if( links.length > 0 ) {
			System.out.println(Arrays.toString(links));
		}else{
			System.out.println("ERROR! No links on page");
		}
		return new WikiPage(links, slinks, name);
		
	}

	public static String getText(String url) throws Exception {
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		StringBuilder response = new StringBuilder();
		String inputLine;

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);

		in.close();

		return response.toString();
	}
	
	public static String getRandomPage() throws Exception{
		return getText( "https://en.wikipedia.org/w/api.php?action=query&list=random&format=json&rnnamespace=0");
	}
	
	public static String getRandomPageTitle() throws Exception {
		String website = getRandomPage();
		JSONObject o   = (JSONObject) jsp.parse(website);
		JSONArray ja   = (JSONArray) ((JSONObject) o.get("query")).get("random");
		return  (String) ((JSONObject) ja.get(0)).get("title");
	}
	
	@SuppressWarnings("deprecation")
	public static JSONObject getWikiPageData(String title) throws Exception{
		return (JSONObject) jsp.parse(getText("https://en.wikipedia.org/w/api.php?action=parse&format=json&page="
						+URLEncoder.encode(title)+"&prop=links%7Cimages%7Ctext"));
	}
	
	public static String[] getImageURLSFromWikiData(JSONObject json){
		JSONArray imageURLS;
		try{
			imageURLS = (JSONArray) ((JSONObject) json.get("parse")).get("images");
			
			String[] s = new String[imageURLS.size()];
			Iterator i = imageURLS.iterator();
			
			int count = 0;
			while( i.hasNext()) {
				s[count++] = (String)i.next();	
			}
			return s;
			
		} catch( Exception e){
			return new String[0];
		}
	}
	
	public static String getFullImageURL( String imageName ) throws Exception {
		String link = "https://en.wikipedia.org/w/api.php?action=query&titles=Image%3a" + URLEncoder.encode(imageName) + "&prop=imageinfo&iiprop=url&meta=siteinfo&siprop=rightsinfo&format=json";
		JSONObject a = (JSONObject) jsp.parse(getText(link));
		
		String result = "";
		try{
		
			JSONObject ja = ((JSONObject) ((JSONObject) a.get("query")).get("pages"));
			String elem = "";
			for( Object s : ((HashMap) ja).keySet()) {
				elem = (String)s;
				break;
			}
			JSONArray ar = (JSONArray) ((JSONObject) ja.get(elem)).get("imageinfo");

			return (String)((JSONObject) ar.get(0)).get("url");
			
		} catch(Exception e){
			System.out.println(e);
			return "";
		}
	}
	
	public static String[] getPageLinks( JSONObject wikidata ) {
		
		try {
			JSONArray ja = ((JSONArray) ((JSONObject) wikidata.get("parse")).get("links"));
			
			ArrayList<String> links = new ArrayList<>();
			Random r = new Random();
			if( ja.size() <= 0 ) throw new Exception();
			
			while( links.size() < Math.min(ja.size(), 5)) {
				JSONObject jo = (JSONObject) ja.get(r.nextInt(ja.size()-1));
				if( jo.get("ns").toString().equals("0") || ja.size() < 5){
					String s = (String) jo.get("*");
					if( !links.contains(s)){
						links.add(s);
					}
					
				}
			}
			String[] s = new String[5];
			links.toArray(s);
			return s;
			
		} catch (Exception e) {
			return new String[0];
		}
		
	}
	
	
}
