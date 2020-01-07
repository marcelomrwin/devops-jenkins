package com.redhat.devops.test.integration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.redhat.devops.entity.User;
import com.redhat.devops.service.UserService;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@RunWith(SpringRunner.class)
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

	@LocalServerPort
	private int port;

	@Autowired
	private UserService service;

	@Before
	public void setUp() throws Exception {
		RestAssured.port = this.port;
	}

	@Test
	public void testRestService() throws Exception {
		Response response = RestAssured.given().accept(ContentType.JSON).when().get("/list").then()
				.statusCode(org.apache.http.HttpStatus.SC_OK).contentType(ContentType.JSON).extract().response();

		assertThat(response.jsonPath().getString("username"), containsString("Bret"));
		List<String> reponseList = response.jsonPath().getList("$");
		assertThat(reponseList.size(), is(Matchers.greaterThanOrEqualTo(20)));
	}

	@Test
	public void testSaveUser() {
		int pk = service.createUser(User.builder().name("name").build());
		assertThat(pk, is(Matchers.greaterThan(0)));
	}

	@Test
	public void testGetUser() {
		List<User> users = service.getUser("name");
		assertThat(users, Matchers.is(notNullValue()));
		assertThat(users, Matchers.is(not(Matchers.empty())));

		Optional<User> user = service.getUser(1);
		assertThat(user, Matchers.is(notNullValue()));

	}

}
