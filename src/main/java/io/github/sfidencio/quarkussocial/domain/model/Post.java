package io.github.sfidencio.quarkussocial.domain.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "posts")
@Getter
@Setter
@ToString
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "post_text", length = 144, nullable = false)
	private String text;
	@Column(name = "date_time", nullable = false)
	private LocalDateTime dateTime;
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@PrePersist
	private void prePersist() {
		this.dateTime = LocalDateTime.now();
	}
}
