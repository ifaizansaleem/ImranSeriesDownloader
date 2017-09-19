package isd;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImranSeriesDownloader {
	
	public static void main(String[] args) {
		try {
			Document doc = Jsoup.connect("http://imranserieskhazana.blogspot.com/p/ma-rahat_24.html").get();
			
			Elements links = doc.select("div[id=post-body-6389981693540669097] span a[href]");
			
			for(Element e : links) {
				
				
				
//				System.out.println(e.attr("href"));
				
				
				Document doc1 = Jsoup.connect(e.attr("href")).get();
				
				Element pdfLink = doc1.select("a[href*=\"mediafire\"]").first();
				
				if(pdfLink == null) {
					System.out.println("PDF Link not found for -> " + e.text());
				}
				else {
					System.out.println(pdfLink.attr("href"));
				}
				
				
				
				
				
			}
			
			
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
