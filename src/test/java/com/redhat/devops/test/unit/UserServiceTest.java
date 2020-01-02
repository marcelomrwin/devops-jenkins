package com.redhat.devops.test.unit;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.redhat.devops.entity.User;
import com.redhat.devops.repository.UserJpaRepository;
import com.redhat.devops.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	@Mock
	protected UserJpaRepository repository;

	@Mock
	protected RestTemplate restTemplate;

	@InjectMocks
	protected UserService mockService;

	@Test
	public void mockLocalAndRemoteBase() {

		List<User> listDB = new ArrayList<>();
		listDB.add(User.builder().id(1).name("FromDB").build());

		// Given
		Mockito.when(this.restTemplate.getForEntity("http://jsonplaceholder.typicode.com/users", User[].class))
				.thenReturn(new ResponseEntity<User[]>(new User[] { User.builder().id(2).name("FromREST").build() },
						HttpStatus.OK));
		Mockito.when(this.repository.findAll(Sort.by(Sort.Direction.ASC, "id"))).thenReturn(listDB);

		// When
		List<User> response = mockService.getAllUsersSortedById();

		// Then
		Assert.assertThat(response, Matchers.notNullValue());
		Assert.assertThat(response, Matchers.not(Matchers.empty()));
		Assert.assertThat(response, Matchers.hasSize(2));
		Assert.assertThat(response.get(1).getId(), Matchers.equalTo(2));

	}

}
