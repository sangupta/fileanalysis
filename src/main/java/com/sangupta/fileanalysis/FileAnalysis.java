/**
 *
 * FileAnalysis - SQLize files to analyze dumps and logs
 * Copyright (c) 2014-2015, Sandeep Gupta
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import com.sangupta.fileanalysis.db.DBResultViewer;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.SQLStatementConsumer;
import com.sangupta.jerry.print.ConsoleTable;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;
import com.sangupta.jerry.util.StringUtils;


/**
 * A simple file analysis tool that dumps data from a file
 * into a local database against which the queries then can be
 * run easily.
 * 
 * @author sangupta
 *
 */
public class FileAnalysis implements Closeable {
	
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
			long start = System.currentTimeMillis();
			handler.loadFile();
			long end = System.currentTimeMillis();
			
			System.out.println("Loaded data in " + (end - start) + " millis.");
		} catch (IOException e) {
			// unable to load file
			System.out.println("Unable to load data file: " + e.getMessage());
			return;
		} 
		
		// update the list of col sizes in database
		this.database.updateColSizes();
	}

	private void showHelp() {
		ConsoleTable table = new ConsoleTable();
		
		table.addHeaderRow("Command", "Description");
		table.addRow("count", "Show number of items in the dataset");
		table.addRow("data", "Show all data from default DATA table in a paginated way");
		table.addRow("desc", "Describe the default DATA table structure");
		table.addRow("exit", "Exit the FA console");
		table.addRow("export", "Export the data in one of CSV, XML, JSON format");
		table.addRow("help", "Show this help screen");
		table.addRow("quit", "Exit the FA console");
		table.addRow("tables", "Show a list of all tables that were created");
		
		table.write(System.out);
	}
	
	public void executeQuery(String query) {
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
	}

	public void doExport(String query) {
		String[] tokens = query.split(" ");
		if(tokens.length != 2) {
			System.out.println("Invalid syntax: use EXPORT <format>");
			System.out.println("\twhere format can be CSV, JSON, or XML");
			return;
		}
		
		final String format = tokens[1].toLowerCase();
		if(!StringUtils.contains(new String[] { "csv", "json", "xml"}, format)) {
			System.out.println("Invalid format: use EXPORT <format>");
			System.out.println("\twhere format can be CSV, JSON, or XML");
			return;
		}
		
		String selectQuery = ConsoleUtils.readLine("Enter SELECT query (empty for all): ", true);
		if(AssertUtils.isEmpty(selectQuery)) {
			selectQuery = "SELECT * FROM DATA;";
		}
		
		this.database.execute(selectQuery, new SQLStatementConsumer() {
			
			@Override
			public void consume(Statement statement) {
				// display the result appropriately
				try {
					new DBResultViewer(database).export(statement, format);
				} catch (SQLException e) {
					System.out.println("Unable to display results of the query");
					e.printStackTrace();
				}
			}
			
		});
	}
	
	@Override
	public void close() {
		// close the database
		if(this.database != null) {
			this.database.closeDatabase();
		}
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