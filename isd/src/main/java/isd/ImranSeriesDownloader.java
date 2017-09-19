package isd;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.jaunt.JauntException;
import com.jaunt.UserAgent;

public class ImranSeriesDownloader {
	
	public static void main(String[] args) {
		
		WebClient webClient = new WebClient(); 
		
		try {
			Document doc = Jsoup.connect("http://imranserieskhazana.blogspot.com/p/ma-rahat_24.html").get();
			
			Elements links = doc.select("div[id=post-body-6389981693540669097] span a[href]");
			
			int counter = 1;
			
			for(Element e : links) {
				
				Document doc1 = Jsoup.connect(e.attr("href")).get();
				
				Element pdfLink = doc1.select("a[href*=\"mediafire\"]").first();
				
				if(pdfLink == null) {
					System.out.println("PDF Link not found for -> " + e.text());
				}
				else {
					String mfLink = pdfLink.attr("href");
					
					System.out.println(mfLink);
					
					
					try {
						UserAgent userAgent = new UserAgent(); // create new userAgent (headless browser).
						userAgent.visit(mfLink); // visit a url
						
						
//						String mfPageData = userAgent.doc.innerHTML();
						
						userAgent.doc.saveAs("f.html");
						
						
						StringBuilder contentBuilder = new StringBuilder();
						try (Stream<String> stream = Files.lines(Paths.get("f.html"), StandardCharsets.UTF_8)) {
							stream.forEach(s -> contentBuilder.append(s).append("\n"));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						String mfPageData = contentBuilder.toString();
						
						
						Pattern pattern = Pattern.compile("kNO\\s=\\s(.*?)\\n", Pattern.DOTALL);
						
						Matcher matcher = pattern.matcher(mfPageData);
						
						while(matcher.matches()) {
							System.out.println(matcher.group());
						}
						
						
						
//						System.out.println(dLink.getText());
						
						
						
					} catch (JauntException e1) { // if an HTTP/connection error occurs, handle JauntException.
						System.err.println(e1);
					}

					
					/*
					
					Document doc2 = response.parse();
					
					
					Element dlLink = doc2.select("div[class=download_link] a").first();
					
					
					
					
					
					System.out.println(dlLink.attr("href"));
					
					
					String fName = doc2.select("div[class=fileName]").first().text();
					
					URL website = new URL(dlLink.attr("href"));
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					FileOutputStream fos = new FileOutputStream(fName);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					*/
				}
				
				
				if(counter == 1)
					break;
				
				
			}
			
			
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
