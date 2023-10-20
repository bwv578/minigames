package com.games.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainController {
  
	@RequestMapping("/")	
	public ModelAndView main(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView("main");
		return mav;
	}
	
}
