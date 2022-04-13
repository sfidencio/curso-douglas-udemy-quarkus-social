package io.github.sfidencio.quarkussocial.rest.dto;

import io.github.sfidencio.quarkussocial.domain.model.Follower;
import lombok.Data;

@Data
public class FollowerResponse {
	private Long id;
	private String name;

	public FollowerResponse() {
	}

	public FollowerResponse(Follower follower) {
		this(follower.getId(), follower.getFollower().getName());
	}

	public FollowerResponse(Long id2, String name2) {
		this.id = id2;
		this.name = name2;
	}

}
