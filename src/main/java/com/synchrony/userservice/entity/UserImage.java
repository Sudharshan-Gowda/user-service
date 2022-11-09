package com.synchrony.userservice.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long userImageId;
	private String imgurId;
	private String imageUrl;
}
