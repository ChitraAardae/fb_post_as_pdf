package fb_html_converter;

public class PDFPageElements {
	private String post;
	
	private String date;
	
	private String imageURL;

	public PDFPageElements(String post, String date, String imageURL) {
		super();
		this.post = post;
		this.date = date;
		this.imageURL = imageURL;
	}

	/**
	 * @return the post
	 */
	public String getPost() {
		return post;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return the imageURL
	 */
	public String getImageURL() {
		return imageURL;
	}

	@Override
	public String toString() {
		return "PDFPageElements [" + (post != null ? "post=" + post + ", " : "")
				+ (date != null ? "date=" + date + ", " : "") + (imageURL != null ? "imageURL=" + imageURL : "") + "]";
	}
}
