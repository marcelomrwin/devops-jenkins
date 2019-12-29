package com.redhat.devops.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@GetMapping(path = "/")
	public String welcome(Model model) {
		logger.info("Processing index request");
		model.addAttribute("course", "DevOps");
		return "index";
	}

}
