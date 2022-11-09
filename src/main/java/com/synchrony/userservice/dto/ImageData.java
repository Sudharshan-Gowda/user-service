package com.synchrony.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {
	private String id;
	private Object title;
	private Object description;
	private int datetime;
	private String type;
	private boolean animated;
	private int width;
	private int height;
	private int size;
	private int views;
	private int bandwidth;
	private Object vote;
	private boolean favorite;
	private boolean nsfw;
	private Object section;
	private Object account_url;
	private Object account_id;
	private boolean is_ad;
	private boolean in_most_viral;
	private boolean has_sound;
	private int ad_type;
	private String ad_url;
	private String edited;
	private boolean in_gallery;
	private String link;
}
