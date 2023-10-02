package com.gcu.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gcu.models.Status;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/hello")
    public Status hello() 
    {    	
    	Status status = new Status(999, "shazebs", "Hello, World!", "...", "9/18/2023 1:07 PM");
    	
        return status;
    }
}