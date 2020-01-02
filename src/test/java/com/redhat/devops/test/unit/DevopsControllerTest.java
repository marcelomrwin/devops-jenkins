package com.redhat.devops.test.unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.devops.entity.User;
import com.redhat.devops.service.UserService;
import com.redhat.devops.web.UserController;

@RunWith(MockitoJUnitRunner.class)
public class DevopsControllerTest {

	@Mock
	protected UserService mockService;

	protected UserController controller;

	protected List<User> mockList;

	@Before
	public void setup() throws IOException {
		this.controller = new UserController(this.mockService);

		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<User>> typeReference = new TypeReference<List<User>>() {
		};
		InputStream inputStream = TypeReference.class.getResourceAsStream("/test-users.json");		
		mockList = mapper.readValue(inputStream, typeReference);
	}

	@Test
	public void getListOfEntities() {

		// Given
		Mockito.when(this.mockService.getAllUsersSortedById()).thenReturn(mockList);
		// When
		ResponseEntity<List<User>> response = this.controller.listAllJson();
		// Then
		Assert.assertThat(response.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
		Assert.assertThat(response.getBody(), Matchers.not(Matchers.empty()));

	}

}
