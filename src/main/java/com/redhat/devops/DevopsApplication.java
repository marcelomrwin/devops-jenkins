package com.redhat.devops;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.devops.entity.User;
import com.redhat.devops.service.UserService;

@SpringBootApplication
public class DevopsApplication {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {

		Optional<String[]> optionalArgs = Optional.ofNullable(args);
		List<Optional<String>> newArgs = new ArrayList<>();
		optionalArgs.ifPresent(s -> Stream.of(s).forEach(x -> newArgs.add(Optional.of(x))));

		// sanitize
		List<String> argsString = newArgs.stream().filter(Optional::isPresent).map(Optional::get)
				.filter(s -> Objects.nonNull(s) && !s.isEmpty()).collect(Collectors.toList());

		SpringApplication.run(DevopsApplication.class, argsString.toArray(new String[argsString.size()]));
	}

	@Bean
	CommandLineRunner runner(UserService userService) {
		return args -> {
			// read json and write to db
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<List<User>> typeReference = new TypeReference<List<User>>() {
			};
			InputStream inputStream = TypeReference.class.getResourceAsStream("/json/users.json");
			try {
				List<User> users = mapper.readValue(inputStream, typeReference);
				userService.saveAll(users);
				logger.info("Users Saved!");
			} catch (IOException e) {
				logger.error("Unable to save users: {}", e.getMessage());
			}
		};
	}

	@Bean
	RestTemplate getTemplate() {
		return new RestTemplate(getClientHttpRequestFactory());
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 5000;
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(timeout);
		return clientHttpRequestFactory;
	}

}
