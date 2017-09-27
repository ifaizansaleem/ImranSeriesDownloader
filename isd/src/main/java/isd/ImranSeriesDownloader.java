package isd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jaunt.JauntException;
import com.jaunt.UserAgent;


class ISDUtils {
	
	public double convertFromBytesToMegabytes(Long sizeInBytes, Integer roundOffScale) {
		
		Double systemFileSize = sizeInBytes / (1024.0*1024.0);
		double roundedFileSize = new BigDecimal(systemFileSize).setScale(roundOffScale, RoundingMode.HALF_UP).doubleValue();
		
		return roundedFileSize;
	}
	
	public long downloadFromLink(String downloadLink, String filePathWithName) {

		long downloadedFileSize = 0;
		
		try {
			URL website = new URL(downloadLink);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos;
			fos = new FileOutputStream(filePathWithName);
			downloadedFileSize = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return downloadedFileSize;
	}
	
	
	
	public MFUrl checkDownloadLink(ArrayList<MFUrl> downloadLinks, String fileName) {
		
		for(MFUrl dl : downloadLinks) {
			if(dl.getFileName().equals(fileName)) {
				return dl;
			}
		}
		
		return null;
	}
	
	
}

class MFUrl {
	String fileName;
	String fileSize;
	String downloadLink;
	Boolean downloadStatus;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public String getDownloadLink() {
		return downloadLink;
	}
	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}
	public Boolean getDownloadStatus() {
		return downloadStatus;
	}
	public void setDownloadStatus(Boolean downloadStatus) {
		this.downloadStatus = downloadStatus;
	}
	
	public void displayData() {
		System.out.println(fileName + " | " + fileSize + " | " + downloadStatus + " | " + downloadLink);
	}
}

public class ImranSeriesDownloader {

	public static void main(String[] args) {

		ISDUtils utils = new ISDUtils();
		
		Document doc;
		File input = null;
		String mfLink = "";
		String fileName = "";
		String filePath = "";
		String extractedDownloadLink = "";
		double roundedFileSize = 0;
		String fileSize = "";

		Pattern pattern;
		Matcher matcher;
		
		ArrayList<MFUrl> downloadLinks = new ArrayList<>();

		try {
			doc = Jsoup.connect("http://imranserieskhazana.blogspot.com/p/mushtaq-ahmed-qureshi.html").get();

			Elements links = doc.select("div[id*=post-body-] span a[href]");


			for (Element e : links) {
				MFUrl downloadLink = new MFUrl();

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
						downloadLink.setFileName(fileName);
						
						fileSize = userAgent.doc.findFirst("<ul class=\"dlInfo-Details\">").findFirst("<li>").findFirst("<span>").getText();
						downloadLink.setFileSize(fileSize);
						
						filePath = "pdfs/" + fileName;
						input = new File(filePath);

						roundedFileSize = utils.convertFromBytesToMegabytes(input.length(), 2);
						
						MFUrl dl;
						
						if(((dl = utils.checkDownloadLink(downloadLinks, fileName)) != null) && dl.getDownloadStatus() && String.valueOf(roundedFileSize).concat(" MB").equals(fileSize)) {
							
							System.out.println(fileName + ", " + roundedFileSize + " MB : Already Downloaded -> " + input.getAbsolutePath());
						}
						else if (dl != null && !dl.getDownloadStatus() && !String.valueOf(roundedFileSize).concat(" MB").equals(fileSize)) {
							
							System.out.println(fileName + " download is incomplete. :: " +  String.valueOf(roundedFileSize).concat(" MB") + ":"+fileSize);
							
							System.err.print("Deleting corrupted file " + fileName + " -> ");
							if(input.delete())
								System.out.println("Done");
							
						}
						else if (!input.exists()) {
							
							doc = Jsoup.parse(userAgent.doc.outerHTML());
							String mfPageData = doc.select("div[class=download_link]").first().html();
							mfPageData = mfPageData.replaceAll("\\\\n", "\n");
							pattern = Pattern.compile("kNO = \"(.*?)\"", Pattern.DOTALL);
							matcher = pattern.matcher(mfPageData);

							while (matcher.find()) {
								extractedDownloadLink = matcher.group(1);
							}

							// Downloading file
							System.out.println("Extracted " + fileName + ", " + fileSize);
							downloadLink.setDownloadLink(extractedDownloadLink);

						}

					} catch (JauntException e1) { // if an HTTP/connection error occurs, handle JauntException.
						System.err.println(e1);
					}
				}

				/*
				if (counter++ == 5)
					break;
				*/
				
				
				
				downloadLink.setDownloadStatus(false);
				downloadLinks.add(downloadLink);
				downloadLink.displayData();

			} // END - Collecting all download links
			
			
			/*
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 */
			
			for (MFUrl dlUrl : downloadLinks) {

				try {
					fileName = dlUrl.getFileName();
					fileSize = dlUrl.getFileSize();
					filePath = "pdfs/" + fileName;
					input = new File(filePath);
	
					roundedFileSize = utils.convertFromBytesToMegabytes(input.length(), 2);
					
					if(input.exists() && String.valueOf(roundedFileSize).concat(" MB").equals(fileSize)) {
						
						System.out.println(fileName + ", " + roundedFileSize + " MB : Already Downloaded -> " + input.getAbsolutePath());
					}
					else if (input.exists() && !String.valueOf(roundedFileSize).concat(" MB").equals(fileSize)) {
						
						System.out.println(fileName + " download is incomplete. :: " +  String.valueOf(roundedFileSize).concat(" MB") + ":"+fileSize);
						
						System.err.print("Deleting corrupted file " + fileName + " -> ");
						if(input.delete())
							System.err.println("Done");
						
						System.out.print("Re-Downloading " + fileName + ", " + fileSize);
						
						// Downloading file
						utils.downloadFromLink(dlUrl.getDownloadLink(), filePath);
						
						input = new File(filePath);
	
						System.out.println(" :: Done -> " + input.getAbsolutePath());
						
					}
					else if (!input.exists()) {
						
						
						// Downloading file
						System.out.print("Downloading " + fileName + ", " + fileSize);
						utils.downloadFromLink(dlUrl.getDownloadLink(), filePath);
						
						input = new File(filePath);
	
						System.out.println(" :: Done -> " + input.getAbsolutePath());
	
						
					}
	
				} catch (Exception e1) { // if an HTTP/connection error occurs, handle JauntException.
					System.err.println(e1);
				}

				/*
				if (counter++ == 5)
					break;
				*/

			} // END - Collecting all download links
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			System.err.println("Finshed Downloading All");
			

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
