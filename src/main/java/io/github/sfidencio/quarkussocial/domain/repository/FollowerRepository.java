package io.github.sfidencio.quarkussocial.domain.repository;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.github.sfidencio.quarkussocial.domain.model.Follower;
import io.github.sfidencio.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;

@ApplicationScoped
public class FollowerRepository implements PanacheRepository<Follower> {
	public Boolean isFollower(User follower, User user) {
		Map<String, Object> params = Parameters.with("user", user).and("follower", follower).map();
		var query = find("follower = :follower and user = :user ", params);
		return query.firstResultOptional().isPresent();
	}

	public List<Follower> findByUser(Long userId) {
		return find("user.id", userId).list();
	}

	public void deleteByFollowerAndUser(Long followerId, Long userId) {
		Map<String, Object> params = Parameters.with("followerId", followerId).and("userId", userId).map();
		delete("follower.id = :followerId and user.id = :userId ", params);
	}
}
