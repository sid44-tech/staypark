package staypark.parking.beans;

import lombok.Data;

@Data
public class TestimonialBean {

	private String comment;
	private String author;
	private String job;
	
	public TestimonialBean(final String comment, final String author, final String job) {
		this.comment = comment;
		this.author = author;
		this.job = job;
	}
}
