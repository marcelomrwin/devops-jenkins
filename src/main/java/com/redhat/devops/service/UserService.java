package com.redhat.devops.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.redhat.devops.entity.User;
import com.redhat.devops.repository.UserJpaRepository;

@Service
public class UserService {

	@Autowired
	public UserService(UserJpaRepository userRepo, RestTemplate restTemplate) {
		super();
		this.userRepo = userRepo;
		this.restTemplate = restTemplate;
	}

	private final UserJpaRepository userRepo;
	protected final RestTemplate restTemplate;

	public Optional<User> getUser(int id) {
		return userRepo.findById(id);
	}

	public int createUser(User user) {
		return userRepo.save(user).getId();
	}

	public List<User> getAllUsersSortedById() {

		List<User> users = this.getAllUsersSorted("id");
		users.sort(Comparator.comparing(User::getId));

		return users;
	}

	public List<User> getAllUsersSorted(String paramname) {

		// retrive users from local database
		List<User> users = userRepo.findAll(Sort.by(Sort.Direction.ASC, paramname));
		users.addAll(getUsersFromExternalService());

		return users;
	}

	public List<User> getUsersFromExternalService() {
		// retrive users from web
		ResponseEntity<User[]> response = restTemplate.getForEntity("http://jsonplaceholder.typicode.com/users",
				User[].class);
		return Arrays.asList(response.getBody());
	}

	public Page<User> getAllUsersPaged(int page, int size) {
		return userRepo.findAll(PageRequest.of(page, size));
	}

	public List<User> getUser(String name) {
		return userRepo.findByName(name);
	}

	public List<User> saveAll(Iterable<User> entities) {
		return userRepo.saveAll(entities);
	}
}
