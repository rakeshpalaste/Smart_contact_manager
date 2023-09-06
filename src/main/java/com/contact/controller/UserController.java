package com.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepository;
import com.contact.dao.UserRepository;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute                    // this method works for all handlers add run every time
	public void addCommonData(Model model,Principal principal) {
		
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);
		
		//Get the user using username(Email)
		
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER"+user);
		
		model.addAttribute("user", user);
		
	}
	
	@GetMapping("/index")
	public String dashboard(Model m)
	{
		m.addAttribute("title", "User Dashboard");
		return"normal/dashboard";
	}


//	@GetMapping("/admin/myfile")
//	public String admin()
//	{
//		return"admin/myfile";
//	}
	
	//open add form handler
	@GetMapping("/addcontact")
	public String openAddContactForm(Model m)
	{
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/addcontact";
	}
	
	//processing add contact form 
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal, HttpSession session){
		try {
			
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploding file...
		
		if(file.isEmpty())
		{
			System.out.println("file is empty");
			contact.setImage("contact.png");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			
			File savefile = new ClassPathResource("static/image").getFile();
			
			Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("image is uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		
		
		System.out.println("added");
		
		//message success
		session.setAttribute("message", new Message(" Contact Successfuly Added !!", "success"));
		
		}catch (Exception e) {
			e.printStackTrace();
			
			//error message
			session.setAttribute("message", new Message(" Something Went Wrong !! Try Again !!", "danger"));
			
			
		}
		return "normal/addcontact";
	}
	
	
	@GetMapping("/show_contacts/{page}")
	public String viewContacts(@PathVariable("page") Integer page, Model m,Principal principal)
	{
		//To send contact list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//parent ke variable me child ka object store karna
		Pageable pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		m.addAttribute("title", "User Contacts");
		return"normal/show_contacts";
	}
	
	@GetMapping("/contact/{id}")
	public String profileDetail(@PathVariable("id")Integer id,Model m, Principal principal)
	{
		
		Optional<Contact> contactoptional = this.contactRepository.findById(id);
		
		Contact contact = contactoptional.get();
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		if(user.getId()==contact.getUser().getId()) 
		{
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}
		System.out.println("Id:"+id);
		
		return"normal/profile";
	}
	
	@GetMapping("/delete/{id}")
	@Transactional
	public String deleteContact(@PathVariable("id") Integer id,Model m, HttpSession session,Principal principal)
	{
//		Optional<Contact> optionalContact = this.contactRepository.findById(id);
//		Contact contact = optionalContact.get();
		System.out.println("Id:"+id);
		
		Contact contact = this.contactRepository.findById(id).get();
		System.out.println("contact"+contact.getId());
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		if(user.getId()==contact.getUser().getId())
		{
			System.out.println("contact"+contact.getId());
			User user2 = this.userRepository.getUserByUserName(principal.getName());
			
			user2.getContacts().remove(contact);
			
			this.userRepository.save(user2);
			
			System.out.println("DELETED");
			session.setAttribute("message", new Message("Contact deleted Succesfully...", "success"));
		}
		
		return"redirect:/user/show_contacts/0";
	}
	
	//Open update form handler
	@PostMapping("/update/{id}")
	public String updateContact(@PathVariable("id") Integer id,Model m) 
	{
		Contact contact = this.contactRepository.findById(id).get();
		
		m.addAttribute("contact", contact);
		m.addAttribute("title", contact.getName());
		
		return"normal/update_profile";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,Model m, HttpSession session,Principal principal)
	{
		try {
			//old contact details
			Contact oldcontactDetail = this.contactRepository.findById(contact.getId()).get();
			
			//image..
			if(!file.isEmpty())
			{
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				File dfile=new File(deleteFile,oldcontactDetail.getImage());
				dfile.delete();
				
				//update new photo
				File savefile = new ClassPathResource("static/image").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("your contact is updated...", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT NAME: "+contact.getName());
		System.out.println("CONTACT ID: "+contact.getId());
		
		return "redirect:/user/contact/"+contact.getId();
	}
	
	@GetMapping("/user_profile")
	public String userProfile(Model m)
	{
		m.addAttribute("title", "Profile Page");
		
		return "normal/user_profile";
	}
	
}

/**    //To send contact list (first way)
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		List<Contact> contacts = user.getContacts();
		m.addAttribute("contacts", contacts); **/

