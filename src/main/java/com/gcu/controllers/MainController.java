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


/**
 * This Main controller holds ALL Page Routes and Java functions that execute basic logic and MySQL database CRUD operations.
 * @author Shazeb Suhail
 * 
 */
@Controller
@RequestMapping("/") // mapped to our landing page.
public class MainController 
{	
	@Autowired
	@SuppressWarnings("unused")
	private DataSource dataSource; 
	
	@Autowired
	@SuppressWarnings("unused")
	private JdbcTemplate database; 

	private static Logger logger = LoggerFactory.getLogger(MainController.class);
	
	
	
	/**
	 * Class Constructor. 
	 * @param dataSource
	 */
	public MainController(DataSource dataSource)
	{
		this.dataSource = dataSource;
		this.database = new JdbcTemplate(dataSource);
	}
	
	
	
	/**
	 * Add new Status to database.
	 * @param status
	 */
	public void AddNewStatus(Status status)
	{
		logger.info("Entering MainController:AddNewStatus method.");
		
		try {
			String sql = "INSERT INTO statuses (Id, Author, Message, PhotoUrl, Datetime) VALUES (?, ?, ?, ?, ?)";
			int result = database.update(sql, 
										 status.getId(), 
										 status.getAuthor(),	
										 status.getMessage(), 
										 status.getPhotoUrl(), 
										 status.getDatetime());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error adding new Status to database.");
		}
		
		logger.info("Exiting MainController:AddNewStatus method.");
	}
	
	
	
	/**
	 * Delete a Status by matching its ID for removal from the database.
	 * @param status
	 * @param model
	 * @return
	 */
	@GetMapping("/deleteStatus/{id}")
	public String DeleteStatus(@PathVariable int id, Model model)
	{
		logger.info("Entering MainController:DeleteStatus method.");
		
		try 
		{
			String sql = "DELETE FROM statuses WHERE Id = ?";
	        database.update(sql, id);
	        logger.info("Successfully deleted StatusID '" + id + "' from database.");
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error deleting StatusID '" + id + "' in database.");
		}

		logger.info("Exiting MainController:DeleteStatus method.");
		
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
		logger.info("Entering MainController:DisplayHomePage method.");
		
		try {
			Integer count = database.queryForObject("SELECT COUNT(*) FROM statuses", Integer.class);
	    	if (count >= 0) 
	    		logger.info("Database table Statuses exists.");	
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			
			String createTableSql = 
				"CREATE TABLE statuses (" 
					+"Id INT PRIMARY KEY AUTO_INCREMENT, "
					+"Author VARCHAR(255) NOT NULL, "
					+"Message VARCHAR(1000) NOT NULL, "
					+"PhotoUrl VARCHAR(1000) NULL, "
					+"Datetime VARCHAR(100) NOT NULL);";			
			
			database.execute(createTableSql); 
			
			logger.info("Sql table Statuses did not exist so application created one.");
		}
				
		List<Status> statuses = GetAllStatuses();
		
		model.addAttribute("statuses", statuses);
		model.addAttribute("status", new Status()); 
		
		logger.info("Exiting MainController:DisplayHomePage method.");
		
		return "home.html"; 
	}
	
	
	
	/**
	 * Display landing page (index).
	 * @param model
	 * @return
	 */
	@GetMapping("/")
	public String DisplayIndexPage(Model model) 
	{
		logger.trace("Entering MainController:DisplayIndexPage method.");		

		logger.trace("Exiting MainController:DisplayIndexPage method.");
		
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
		logger.info("Entering MainController:DisplayPostPage method.");
		
		model.addAttribute("status", status);
		
		logger.info("Exiting MainController:DisplayPostPage method.");
		
		return "post.html"; 
	}
	
	
	
	/**
	 * Check database to see if a Status item exists with a specified ID.
	 * @param id
	 * @return
	 */
	public boolean DoesStatusExist(int id)
	{
		logger.info("Entering MainController:DoesStatusExist method.");
		
		int count = -1;		
		try {
			String sql = "SELECT COUNT(*) FROM statuses WHERE Id = ?";
	        count = database.queryForObject(sql, Integer.class, id);
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error detecting if StatusID " + id + " exists in database.");			
		}
        
        logger.info("Exiting MainController:DoesStatusExist method.");
        
        return count > 0;
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
		logger.info("Entering MainController:EditPost method.");
		
		Status status = new Status();		
		try 
		{
			if (DoesStatusExist(id))
			{
				status = GetStatusById(id);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error updating StatusID " + id + " in database.");
		}
		
		model.addAttribute("status", status);
		
		logger.info("Exiting MainController:EditPost method.");
		
		return DisplayPostPage(status, model);
	}
	
	
	
	/**
	 * Retrieve a list of all Status items from database.
	 * @return
	 */
	public List<Status> GetAllStatuses()
	{
		logger.info("Entering MainController:GetAllStatuses method.");
		
		List<Status> statuses = new ArrayList<Status>();
		try {
			String sql = "SELECT * FROM statuses ORDER BY Id DESC";
			SqlRowSet record = database.queryForRowSet(sql);
			while (record.next())
			{
				statuses.add(new Status(
						record.getInt("Id"),
						record.getString("Author"),
						record.getString("Message"),
						record.getString("PhotoUrl"),
						record.getString("Datetime")));
			}
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error retrieving all Statuses from database.");
		}
		
		logger.info("Exiting MainController:GetAllStatuses method.");
		
		return statuses;
	}
	
	
	
	/**
	 * Get a Status item by its ID from the database.
	 * @param id
	 * @return
	 */
	public Status GetStatusById(int id)
	{
		logger.info("Entering MainController:GetStatusById method");
		
		Status statusItem = null;
		try {
			String sql = "SELECT * FROM statuses WHERE Id = ?";
			statusItem = database.queryForObject(sql, (rs, rowNum) -> {
	            Status result = new Status();
	            result.setId(rs.getInt("Id"));
	            result.setAuthor(rs.getString("Author"));
	            result.setMessage(rs.getString("Message"));
	            result.setPhotoUrl(rs.getString("PhotoUrl"));
	            result.setDatetime(rs.getString("Datetime"));
	            return result;
	        }, id);
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error retrieving StatusID " + id + " from database.");
		}
		
		logger.info("Exiting MainController:GetStatusById method");
		
		return statusItem;
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
		logger.info("Entering MainController:SubmitStatus method.");
		
    	LocalDateTime timestamp = LocalDateTime.now();   
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a, M-dd-yyyy");
        
        
        status.setDatetime(timestamp.format(formatter));

		try {
			Integer count = database.queryForObject("SELECT COUNT(*) FROM statuses", Integer.class);
	    	if (count >= 0) 
	    	{
	    		logger.info("Database table 'statuses' exists, processing new status save to DB now...");
	    	}				
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			
			String createTableSql = "CREATE TABLE statuses (" +  
					"Id INT PRIMARY KEY AUTO_INCREMENT, " +
					"Author VARCHAR(255) NOT NULL, " + 
					"Message VARCHAR(1000) NOT NULL, " +
					"PhotoUrl VARCHAR(1000) NULL, " + 
					"Datetime VARCHAR(100) NOT NULL" +
				");";
			
			database.execute(createTableSql); 			
			
			logger.info("Sql table statuses did not exist so application created one.");
		}
		
		if (DoesStatusExist(status.getId()))
		{			
			if (!GetStatusById(status.getId()).ToString().contains(status.ToString()))
			{
				UpdateStatusById(status.getId(), status);
			}
		}
		else 
		{
			AddNewStatus(status);	
		}	
		
		List<Status> statuses = GetAllStatuses();

		model.addAttribute("statuses", statuses);
		model.addAttribute("status", new Status()); 
		
		logger.info("Exiting MainController:SubmitStatus method.");
		 
		return "home.html";
	}
	
	
	
	/**
	 * Update Status record by its ID in database.
	 * @param id
	 * @param status
	 */
	public void UpdateStatusById(int id, Status status)
	{
		logger.info("Entering MainController:UpdateStatusById method.");
		
		try {
			String sql = "UPDATE statuses SET Author = ?, Message = ?, PhotoUrl = ?, Datetime = ? WHERE Id = ?";
	        database.update(sql, 
	        				status.getAuthor(), 
	        				status.getMessage(), 
	        				status.getPhotoUrl(), 
	        				status.getDatetime(), 
	        				id);
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.info("Error updating StatusID " + id + " in database.");
		}
		
		logger.info("Exiting MainController:UpdateStatusById method");
	}
	
	
	
}
