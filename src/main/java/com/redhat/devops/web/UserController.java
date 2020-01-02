package com.redhat.devops.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.redhat.devops.entity.User;
import com.redhat.devops.service.UserService;

@Controller
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	protected final UserService service;

	@Autowired
	public UserController(UserService service) {
		super();
		this.service = service;
	}

	@GetMapping(path = "/")
	public String index(Model model) {
		logger.info("Processing index request");

		List<User> users = service.getAllUsersSortedById();
		model.addAttribute("users", users);

		return "index";
	}

	@GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<User>> listAllJson() {
		logger.info("Processing list request");
		return new ResponseEntity<List<User>>(service.getAllUsersSortedById(), HttpStatus.OK);
	}

}
