package com.gcu.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gcu.models.Status;
import com.gcu.services.DatabaseService;


/**
 * This Main controller holds ALL Page Routes and Java functions that execute basic logic and MySQL database CRUD operations.
 * @author Shazeb Suhail
 * 
 */
@Controller
@RequestMapping("/") // mapped to our landing page.
public class MainController 
{	
	private Logger logger = LoggerFactory.getLogger(MainController.class);
	
	private DatabaseService dbService; 
	
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a, M-dd-yyyy");
	
	/**
	 * Class Constructor. 
	 * @param dataSource
	 */
	public MainController(DataSource dataSource)
	{		
		this.dbService = new DatabaseService(dataSource);
	}		
	
	/**
	 * Delete a Status by matching its ID for removal from the database.
	 * @param status
	 * @param model
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public String DeleteStatus(@PathVariable int id, Model model)
	{
		logger.info("Entering MainController:DeleteStatus() with id: " + id);
		
		boolean resultOfDeletion = dbService.DeleteStatusByIdFromDB(id);

		logger.info("Exiting MainController:DeleteStatus() with result of id '" + id + "' deletion: " + resultOfDeletion);
		
		return DisplayHomePage(model);
	}
	
	/**
	 * Display Home page. 
	 * @param model
	 * @return
	 */
	@GetMapping("/home")
	public String DisplayHomePage(Model model) 
	{
		logger.info("Entering MainController:DisplayHomePage()");
		
		List<Status> statuses = new ArrayList<Status>();		
		if (dbService.CheckIfStatusTableExistsInDB())
		{
			statuses = dbService.GetAllStatusDescendingByIdFromDB();
		}
		else 
		{
			dbService.CreateStatusDBTable();
		}
		
		model.addAttribute("statuses", statuses);
		model.addAttribute("status", new Status()); 
		
		logger.info("Exiting MainController:DisplayHomePage() with '" + statuses.size() + "' Statuses.");
		
		return "home.html"; 
	}
	
	/**
	 * Display index landing page.
	 * @param model
	 * @return
	 */
	@GetMapping("/")
	public String DisplayIndexPage(Model model) 
	{
		logger.trace("Entering and Exiting MainController:DisplayIndexPage()");	
		
		return "index.html"; 
	}	
	
	/**
	 * Display Post page.
	 * @param model
	 * @return
	 */
	@GetMapping("/post")
	public String DisplayPostPage(@ModelAttribute Status status, Model model) 
	{				
		logger.info("Entering and Exiting MainController:DisplayPostPage() with Status: " + status.ToString());

		model.addAttribute("status", status);
		
		return "post.html"; 
	}	
	
	/**
	 * Edit Status item in Post page.
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/post/{id}")
	public String EditPost(@PathVariable int id, Model model)
	{
		logger.info("Entering MainController:EditPost() with id: " + id);
		
		Status status = new Status();		
		if (dbService.DoesStatusIdExistInDB(id))
		{
			status = dbService.GetStatusByIdFromDB(id);
		}
		
		model.addAttribute("status", status);
		
		logger.info("Exiting MainController:EditPost() with Status: " + status.ToString());
		
		return "post.html";
	}	
	
	/**
	 * Post a Status to database.
	 * @param status
	 * @param model
	 * @return
	 */
	@PostMapping("/post")
	public String SubmitStatus(@ModelAttribute Status status, Model model)
	{		
		logger.info("Entering MainController:SubmitStatus() with Status: " + status.ToString());
        
    	LocalDateTime timestamp = LocalDateTime.now();  
        status.setDatetime(timestamp.format(formatter));
		
        // for existing status updates 
		if (dbService.DoesStatusIdExistInDB(status.getId()) 
				&& !dbService.GetStatusByIdFromDB(status.getId()).ToString().contains(status.ToString()))
		{	
			dbService.UpdateStatusInDB(status.getId(), status);
		}        
        // for new status updates 
		else if (!dbService.DuplicateStatusCheck(status.getAuthor(), status.getMessage(), status.getPhotoUrl()))
	    {
	    	dbService.InsertStatusIntoDB(status);
	    }	
		
		List<Status> statuses = dbService.GetAllStatusDescendingByIdFromDB();

		model.addAttribute("statuses", statuses);
		model.addAttribute("status", new Status()); 
		
		logger.info("Exiting MainController:SubmitStatus() with '" + statuses.size() + "' Statuses and new Status: " + status.ToString());
		 
		return "home.html";
	}
}























