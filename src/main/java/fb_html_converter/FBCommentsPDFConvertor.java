package fb_html_converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.licensing.base.LicenseKey;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class FBCommentsPDFConvertor {

	static {
		LicenseKey.loadLicenseFile(FBProjectConstants.LICENSE_FILE);
	}

	public static void main(String[] args) throws IOException, ParseException, ZipException {
		unzip(FBProjectConstants.ZIPFILE);
		for (String postsHtmlFile : getPostsFile(new File(FBProjectConstants.PARENT_FOLDER))) {
			createPDFFile(parseHTMLFile(new File(postsHtmlFile)));
		}
	}

	private static void unzip(String zipfile) throws ZipException {
		ZipFile zipFile = new ZipFile(zipfile);
		zipFile.extractAll(FBProjectConstants.PARENT_FOLDER);
	}

	private static List<PDFPageElements> parseHTMLFile(File htmlFile) throws IOException {
		org.jsoup.nodes.Document document = Jsoup.parse(htmlFile, "UTF-8");
		Elements elements = document.select("div[role=main]").select("div._3-95._a6-g");
		elements.remove(0); // Removing Born on Section
		List<PDFPageElements> pageElementsList = new ArrayList<PDFPageElements>();
		for (org.jsoup.nodes.Element element : elements) {
			String post = element.select("div._2pin > div").text();
			String date = element.select("div._a72d").text();
			Elements imgElement = element.select("img[src]");
			String imagePath = imgElement.isEmpty() ? null
					: FBProjectConstants.PARENT_FOLDER + File.separator + imgElement.first().attr("src");
			pageElementsList.add(new PDFPageElements(post, date, imagePath));
		}
		return pageElementsList;
	}

	private static void createPDFFile(List<PDFPageElements> pdfPageElementsList) throws IOException {
		File pdfFile = getPDFFile();
		try (FileOutputStream destinationFile = new FileOutputStream(pdfFile)) {
			FontProvider fontProvider = new FontProvider("Noto");
			fontProvider.addDirectory("font");
			PdfDocument document1 = new PdfDocument(
					new PdfWriter(destinationFile, new WriterProperties().addUAXmpMetadata()));
			try (Document document = new Document(document1, PageSize.A4)) {
				document.setFontProvider(fontProvider);
				document.setFontFamily("Noto");
				for (PDFPageElements pdfPageElement : pdfPageElementsList) {
					if (pdfPageElement.getImageURL() != null) {
						ImageData image = ImageDataFactory.create(pdfPageElement.getImageURL());
						document.add(new Image(image));
					}
					document.add(new Paragraph(pdfPageElement.getPost()));
					document.add(new Paragraph("Date & Time: " + pdfPageElement.getDate()));
					document.add(new AreaBreak(PageSize.A4));
				}
			}
		}
	}

	private static List<String> getPostsFile(File directory) throws IOException {
		List<String> fileNamesList = new ArrayList<>();
		try (Stream<Path> walk = Files.walk(Paths.get(FBProjectConstants.PARENT_FOLDER))) {
			fileNamesList = walk.map(x -> x.toString())
					.filter(f -> f.matches(FBProjectConstants.POSTS_HTML_FILE_PATTERN)).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileNamesList;
	}
	
	private static File getPDFFile() {
		return new File(FBProjectConstants.PDF_DESTINATION + File.separator + FBProjectConstants.PDF_PREFIX + Clock.systemDefaultZone().millis() + FBProjectConstants.PDF_SUFFIX  );
	}
}