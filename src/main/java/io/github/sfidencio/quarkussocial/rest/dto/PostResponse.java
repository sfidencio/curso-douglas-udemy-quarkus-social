package io.github.sfidencio.quarkussocial.rest.dto;

import java.time.LocalDateTime;

import io.github.sfidencio.quarkussocial.domain.model.Post;
import lombok.Data;

@Data
public class PostResponse {
	private String text;
	private LocalDateTime dateTime;

	public static PostResponse fromEntity(Post post) {
		var postResponse = new PostResponse();
		postResponse.setText(post.getText());
		postResponse.setDateTime(post.getDateTime());
		return postResponse;
	}
}