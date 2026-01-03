package org.example.nasa;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class NASA {

	private String copyright;
	private String date;
	private String explanation;
	private String hdurl;
	private String mediaType;
	private String serviceVersion;
	private String title;
	private String url;

	public NASA(@JsonProperty("copyright") String copyright, @JsonProperty("date") String date,
			@JsonProperty("explanation") String explanation, @JsonProperty("hdurl") String hdurl,
			@JsonProperty("media_type") String mediaType, @JsonProperty("service_version") String serviceVersion,
			@JsonProperty("title") String title, @JsonProperty("url") String url) {
		this.copyright = copyright;
		this.date = date;
		this.explanation = explanation;
		this.hdurl = hdurl;
		this.mediaType = mediaType;
		this.serviceVersion = serviceVersion;
		this.title = title;
		this.url = url;
	}

}
