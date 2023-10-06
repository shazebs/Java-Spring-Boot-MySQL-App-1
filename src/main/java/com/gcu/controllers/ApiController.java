package com.gcu.controllers;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gcu.models.Status;
import com.gcu.services.DatabaseService;

@RestController
@RequestMapping("/api")
public class ApiController 
{
	private DatabaseService dbService; 
	
	private Logger logger = LoggerFactory.getLogger(ApiController.class);
	
	public ApiController(DataSource dataSource)
	{
		this.dbService = new DatabaseService(dataSource);
	}
	
	/**
	 * GET mapping for retrieving all items in Status table from database.
	 * @return
	 */
    @GetMapping("/status")
    public List<Status> GET_Status() 
    {    	  	
    	logger.info("Entering ApiController:GET_Status()");
    	
    	List<Status> statuses = dbService.GetAllStatusDescendingByIdFromDB();
    	
    	logger.info("Exiting ApiController:GET_Status() with '" + statuses.size() + "' Statuses.");
    	
        return statuses;
    }
}