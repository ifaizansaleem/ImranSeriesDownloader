package isd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Document doc;
		File input = null;
		String mfLink = "";
		String fileName = "";
		String downloadLink = "";
		Double systemFileSize;
		double roundedFileSize = 0;
		String fileSize = "";

		Pattern pattern;
		Matcher matcher;

		try {
			doc = Jsoup.connect("http://imranserieskhazana.blogspot.com/p/mushtaq-ahmed-qureshi.html").get();

			Elements links = doc.select("div[id*=post-body-] span a[href]");

			int counter = 1;

			for (Element e : links) {

				doc = Jsoup.connect(e.attr("href")).get();

				Element pdfLink = doc.select("a[href*=\"mediafire\"]").first();

				if (pdfLink == null) {
					System.out.println("PDF Link not found for -> " + e.text());
				} else {
					mfLink = pdfLink.attr("href");

					// System.out.println(mfLink);

					try {
						UserAgent userAgent = new UserAgent(); // create new userAgent (headless browser).
						userAgent.visit(mfLink); // visit a url

						fileName = userAgent.doc.findFirst("<title>").getText() + ".pdf";
						fileSize = userAgent.doc.findFirst("<ul class=\"dlInfo-Details\">").findFirst("<li>").findFirst("<span>").getText();
						input = new File("pdfs/" + fileName);

						systemFileSize = input.length() / (1024.0*1024.0);
						roundedFileSize = new BigDecimal(systemFileSize).setScale(2, RoundingMode.HALF_UP).doubleValue();
						
						if (!input.exists()) {

							userAgent.doc.saveAs("f.html");

							input = new File("f.html");
							doc = Jsoup.parse(input, "UTF-8");

							String mfPageData = doc.select("div[class=download_link]").first().html();
//							fileSize = doc.select("ul[class=dlInfo-Details] li span").first().text();

							mfPageData = mfPageData.replaceAll("\\\\n", "\n");

							pattern = Pattern.compile("kNO = \"(.*?)\"", Pattern.DOTALL);

							matcher = pattern.matcher(mfPageData);

							while (matcher.find()) {
								downloadLink = matcher.group(1);
							}

							
							// Downloading file
//							System.out.println("~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~");
							System.out.print("Downloading " + fileName + ", " + fileSize);

							URL website = new URL(downloadLink);
							ReadableByteChannel rbc = Channels.newChannel(website.openStream());
							FileOutputStream fos = new FileOutputStream("pdfs/"+ fileName);
							fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
							
							
							input = new File("pdfs/"+ fileName);

							System.out.println(" :: Done -> " + input.getAbsolutePath());

							
						} else {
							if(String.valueOf(roundedFileSize).concat(" MB").equals(fileSize)) {
//								System.out.println("~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~#~");
								System.out.println(fileName + ", " + roundedFileSize + " MB : Already Downloaded -> " + input.getAbsolutePath());
							}
							else {
								System.out.println(fileName + " download is incomplete. :: " +  String.valueOf(roundedFileSize).concat(" MB") + ":"+fileSize);
							}
						}

					} catch (JauntException e1) { // if an HTTP/connection error occurs, handle JauntException.
						System.err.println(e1);
					}
				} 

				/*
				if (counter++ == 5)
					break;
				*/

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
