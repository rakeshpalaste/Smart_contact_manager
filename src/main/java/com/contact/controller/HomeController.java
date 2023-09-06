package com.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title", "Home Page");
		
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title", "About Page");
		return "about";
	}
	
	@GetMapping("/register")
	public String register(Model m)
	{
		m.addAttribute("title", "Registration Page");
		m.addAttribute("user", new User());
		return "register";
	}
	
	//handler for registering user
	@PostMapping("/submit")                                                                 //By Default type cast string into value       
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,  Model model, HttpSession session)
	{
		
		try {
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user", user);
				return "register";
			}
			
			user.setRole("ROLE_ADMIN");
			user.setEnabled(true);
			user.setImage("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			System.out.println("USER"+ user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			return "register";
			
		} catch (Exception e) 
		{
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! " , "alert-danger"));
			return "register";
		}
		
		
	}

	@GetMapping("/login")
	public String login(Model m)
	{
		m.addAttribute("title", "Login Page");
		return "login";
	}
	
}
