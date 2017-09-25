package isd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

		try {
			Document doc = Jsoup.connect("http://imranserieskhazana.blogspot.com/p/ma-rahat_24.html").get();

			Elements links = doc.select("div[id=post-body-6389981693540669097] span a[href]");

			int counter = 1;

			for (Element e : links) {

				Document doc1 = Jsoup.connect(e.attr("href")).get();

				Element pdfLink = doc1.select("a[href*=\"mediafire\"]").first();

				if (pdfLink == null) {
					System.out.println("PDF Link not found for -> " + e.text());
				} else {
					String mfLink = pdfLink.attr("href");

					System.out.println(mfLink);

					try {
						UserAgent userAgent = new UserAgent(); // create new userAgent (headless browser).
						userAgent.visit(mfLink); // visit a url

						// String mfPageData = userAgent.doc.innerHTML();

						userAgent.doc.saveAs("f.html");

						File input = new File("f.html");
						Document doc2 = Jsoup.parse(input, "UTF-8", "http://example.com/");

						String mfPageData = doc2.select("div[class=download_link]").first().html();

						
						Pattern pattern = Pattern.compile("http:(.*?)\n", Pattern.DOTALL);

						Matcher matcher = pattern.matcher(mfPageData);

						System.out.println(matcher.group());

					} catch (JauntException e1) { // if an HTTP/connection error occurs, handle JauntException.
						System.err.println(e1);
					}
				}

				if (counter == 1)
					break;

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
