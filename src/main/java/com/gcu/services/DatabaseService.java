package com.gcu.services;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.gcu.models.Status;

/**
 * This class contains methods for interacting with SQL database connected to iStatus.
 * @author Shazeb Suhail
 * 10/5/2023
 *
 */
public class DatabaseService 
{	
	@Autowired
	@SuppressWarnings("unused")
	private JdbcTemplate database; 

	private static Logger logger = LoggerFactory.getLogger(DatabaseService.class);	
	
	/**
	 * Class Constructor. 
	 * @param dataSource
	 */
	public DatabaseService(DataSource dataSource)
	{
		this.database = new JdbcTemplate(dataSource);
	}
	
	/**
	 * Returns 'true' or 'false' dependent on success of Status table existence in database.
	 * @return
	 */
	public boolean CheckIfStatusTableExistsInDB()
	{
		logger.info("Entering DatabaseService:CheckIfStatusTableExistsInDB()");
		
		try {
			Integer count = database.queryForObject("SELECT COUNT(*) FROM statuses", Integer.class);
			
	        logger.info("Exiting DatabaseService:CheckIfStatusTableExistsInDB() with boolean: " + (count >= 0));
	        
	    	return count >= 0;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			
			logger.info("Error detecting if Status table exists in database.");
	        logger.info("Exiting DatabaseService:CheckIfStatusTableExistsInDB() with boolean: false");
	        
	        return false;
		}
	}
	
	/**
	 * Returns 'true' or 'false' dependent on success of database table creation.
	 * @return
	 */
	public boolean CreateStatusDBTable()
	{
		logger.info("Entering DatabaseService:CreateStatusDBTable()");
		
		try {	    	
			String createTableSql = 
					"CREATE TABLE statuses (" 
						+ "Id INT PRIMARY KEY AUTO_INCREMENT, "
						+ "Author VARCHAR(255) NOT NULL, "
						+ "Message VARCHAR(1000) NOT NULL, "
						+ "PhotoUrl VARCHAR(1000) NULL, "
						+ "Datetime VARCHAR(100) NOT NULL );";
			
			database.execute(createTableSql); 
			
			logger.info("Successfully created Status table in database.");
	        logger.info("Exiting DatabaseService:CreateStatusDBTable() with boolean: true");
	        
	        return true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			
			logger.info("Error creating Status table in database.");
	        logger.info("Exiting DatabaseService:CreateStatusDBTable() with boolean: false");
	        
	        return false;
		}
	}
	
	/**
	 * Return 'true' or 'false' dependent on success of deletion.
	 * @param id
	 * @return
	 */
	public boolean DeleteStatusByIdFromDB(int id)
	{
		logger.info("Entering DatabaseService:DeleteStatusByIdFromDB() with id: " + id);
		
		try {
			String sql = "DELETE FROM statuses WHERE Id = ?";
	        database.update(sql, id);
	        
	        logger.info("Successfully deleted Status ID '" + id + "' from database.");
	        logger.info("Exiting DatabaseService:DeleteStatusByIdFromDB() with boolean: true");
	        
	        return true;
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			
			logger.info("Error deleting Status ID '" + id + "' in database.");
	        logger.info("Exiting DatabaseService:DeleteStatusByIdFromDB() with boolean: false");
	        
	        return false;
		}
	}
	
	/**
	 * Returns 'true' or 'false' dependent on existence of matching Status ID in database.
	 * @param id
	 * @return
	 */
	public boolean DoesStatusIdExistInDB(int id)
	{
		logger.info("Entering DatabaseService:DoesStatusIdExistInDB() with id: " + id);
		
		int count = -1;		
		try {
			String sql = "SELECT COUNT(*) FROM statuses WHERE Id = ?";
	        count = database.queryForObject(sql, Integer.class, id);
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			
			logger.info("Error detecting if Status ID '" + id + "' exists in database.");			
		}
        
        logger.info("Exiting DatabaseService:DoesStatusIdExistInDB() with boolean: " + (count > 0));
        
        return count > 0;
	}
	
	/**
	 * Returns 'true' or 'false' dependent on matching-set Status columns in database.
	 * @param author
	 * @param message
	 * @param photoUrl
	 * @return
	 */
	public boolean DuplicateStatusCheck(String author, String message, String photoUrl)
	{
		try {
			String sql = "SELECT COUNT(*) FROM statuses WHERE Author = ? AND Message = ? AND PhotoUrl = ?;";
			int result = database.queryForObject(sql, Integer.class, author, message, photoUrl);
			
			if (result > 0)
			{
				logger.info("Exiting DatabaseService:DuplicateStatusCheck() with boolean: true");
				
				return true;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();			
			
			logger.info("Error while duplicate status check.");
		}
		
		logger.info("Exiting DatabaseService:DuplicateStatusCheck() with boolean: false");
		
		return false;
	}
	
	/**
	 * Returns a list of all items that exist in Status database table.
	 * @return
	 */
	public List<Status> GetAllStatusDescendingByIdFromDB()
	{
		logger.info("Entering DatabaseService:GetAllStatusDescendingByIdFromDB()");
		
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
			
			logger.info("Error retrieving all Status items from database.");
		}
		
		logger.info("Exiting DatabaseService:GetAllStatusDescendingByIdFromDB() with '" + statuses.size() + "' Statuses.");
		
		return statuses;
	}
	
	/**
	 * Returns Status object associated with matching ID in database.
	 * @return
	 */
	public Status GetStatusByIdFromDB(int id)
	{
		logger.info("Entering DatabaseService:GetStatusByIdFromDB()");

		Status item = null; 
		try {
			String sql = "SELECT * FROM statuses WHERE Id = ?";
			item = database.queryForObject(sql, (rs, rowNum) -> {
	            Status result = new Status();
	            result.setId(rs.getInt("Id"));
	            result.setAuthor(rs.getString("Author"));
	            result.setMessage(rs.getString("Message"));
	            result.setPhotoUrl(rs.getString("PhotoUrl"));
	            result.setDatetime(rs.getString("Datetime"));
	            return result;
	        }, id);
			
			logger.info("Exiting DatabaseService:GetStatusByIdFromDB() with Status: " + item.ToString());	
			
			return item;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			
			logger.info("Error finding Status with ID '" + id + "' in database.");
			logger.info("Exiting DatabaseService:GetStatusByIdFromDB() with Status: " + item.ToString());	
			
			return item; 
		}
	}
		
	/**
	 * Returns 'true' or 'false' dependent on success of insertion.
	 * @param status
	 * @return
	 */
	public boolean InsertStatusIntoDB(Status status)
	{
		logger.info("Entering DatabaseService:InsertStatusIntoDB() with Status: " + status.ToString());
		
		int queryResult = -1;		
		try {
			String sql = "INSERT INTO statuses (Id, Author, Message, PhotoUrl, Datetime) VALUES (?, ?, ?, ?, ?)";
			queryResult = database.update(sql, 
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
		
		logger.info("Exiting DatabaseService:InsertStatusIntoDB() with boolean: " + (queryResult > 0) );
		
		return queryResult > 0;
	}
	
	/**
	 * Returns 'true' or 'false' dependent on success of update.
	 * @param id
	 * @param status
	 * @return
	 */
	public boolean UpdateStatusInDB(int id, Status status)
	{
		logger.info("Entering DatabaseService:UpdateStatusInDB() with id: " + id + " and Status: " + status.ToString());
		
		try {
			String sql = "UPDATE statuses SET Author = ?, Message = ?, PhotoUrl = ?, Datetime = ? WHERE Id = ?";
	        database.update(sql, 
	        				status.getAuthor(), 
	        				status.getMessage(), 
	        				status.getPhotoUrl(), 
	        				status.getDatetime(), 
	        				id);
	        
	        logger.info("Successfully updated Status ID '" + id + "' in database");
	        logger.info("Exiting DatabaseService:UpdateStatusInDB() with boolean: true");
	        
	        return true;
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
			
			logger.info("Error updating Status with ID '" + id + "' in database.");
	        logger.info("Exiting DatabaseService:UpdateStatusInDB() with boolean: false");
	        
	        return false;
		}
	}
}























