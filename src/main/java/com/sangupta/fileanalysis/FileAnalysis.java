/**
 *
 * FileAnalysis - SQLize files to analyze dumps and logs
 * Copyright (c) 2014, Sandeep Gupta
 * 
 * http://sangupta.com/projects/fileanalysis
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.sangupta.fileanalysis;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import com.sangupta.fileanalysis.db.DBResultViewer;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.SQLStatementConsumer;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;


/**
 * A simple file analysis tool that dumps data from a file
 * into a local database against which the queries then can be
 * run easily.
 * 
 * @author sangupta
 *
 */
public class FileAnalysis {
	
	/**
	 * The database we are currently connected to
	 * 
	 */
	private final Database database;
	
	/**
	 * The input file
	 */
	private final File file;
	
	/**
	 * The file format of the file
	 */
	private final FileFormat fileFormat;
	
	/**
	 * Create a new instance.
	 * 
	 * @param file
	 * @param format
	 */
	public FileAnalysis(File file, FileFormat format) {
		this.file = file;
		this.fileFormat = format;
		
		String databaseName = getDBName(file, format);
		this.database = new Database(databaseName);
	}
	
	/**
	 * Handle the input and start {@link FileAnalysis} query engine.
	 * 
	 * @param args
	 */
	public void analyzeFile() {
		// obtain the correct handler
		FileFormatHandler handler = fileFormat.getHandler();
		
		// create the database

		// initialize the handler
		handler.initialize(database, file);
		
		// read configuration
		System.out.println("Reading configuration...");
		handler.readConfiguration();
		
		// create the db tables
		System.out.println("Creating required tables...");
		handler.createDBTables();
		
		// start loading data
		System.out.println("Populating data...");
		try {
			handler.loadFile();
		} catch (IOException e) {
			// unable to load file
			System.out.println("Unable to load data file: " + e.getMessage());
			return;
		}
		
		// update the list of col sizes in database
		this.database.updateColSizes();
		
		// show the query prompt
		do {
			String query = ConsoleUtils.readLine("\nfa query$ ", true);
			if(AssertUtils.isBlank(query)) {
				continue;
			}
			
			if("exit".equalsIgnoreCase(query)) {
				break;
			}
			
			if("quit".equalsIgnoreCase(query)) {
				break;
			}
			
			this.database.execute(query, new SQLStatementConsumer() {
				
				@Override
				public void consume(Statement statement) {
					// display the result appropriately
					try {
						new DBResultViewer(database).viewResult(statement);
					} catch (SQLException e) {
						System.out.println("Unable to display results of the query");
						e.printStackTrace();
					}
				}
				
			});
		} while(true);
		
		// close the database
		database.closeDatabase();
		
		System.out.println("File Analysis complete.");
	}
	
	/**
	 * Get the database name that we will use for storing data.
	 * 
	 * @param file
	 * @param format
	 * @return
	 */
	private String getDBName(File file, FileFormat format) {
		return file.getName();
	}

}
