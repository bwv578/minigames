package com.games.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainController {
	
	private String testString = "test";
	
	@RequestMapping("/")	
	public ModelAndView main(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView("main");
		return mav;
	}
	
	@RequestMapping("/yahtzee")
	public ModelAndView yatzy(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView("games/yahtzee/main");
		
		testString += "@";
		System.out.println(testString);
		
		return mav;
	}
	
	@RequestMapping("/playYahtzee")
	public ModelAndView playYahtzee(HttpServletRequest req) {
		ModelAndView mav = new ModelAndView("games/yahtzee/play");
		return mav;
	}
	
}
