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

package com.sangupta.fileanalysis.formats.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.sangupta.fileanalysis.db.DBColumn;
import com.sangupta.fileanalysis.db.DBColumnType;
import com.sangupta.fileanalysis.db.DBHelper;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.DatabaseTable;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;
import com.sangupta.jerry.util.StringUtils;

/**
 * Base class for file format handlers where fields are terminated using a
 * delimiter.
 * 
 * @author sangupta
 *
 */
public abstract class AbstractDelimitedFileFormatHandler extends AbstractFileFormatHandler {
	
	/**
	 * Read line by line from file
	 */
	private LineIterator iterator;
	
	/**
	 * The database table
	 */
	private DatabaseTable table;
	
	/**
	 * Has header row
	 */
	private boolean hasHeaderRow = true;
	
	/**
	 * Stores the first line if needed
	 */
	private String firstLine;

	/**
	 * Get the delimiter string.
	 * 
	 * @return
	 */
	protected abstract String getDelimiterString();
	
	/**
	 * Initialize the handler
	 */
	@Override
	public void initialize(Database database, File file) {
		super.setDBAndFile(database, file);
		
		try {
			iterator = FileUtils.lineIterator(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read user based configuration
	 */
	@Override
	public void readConfiguration() {
		this.hasHeaderRow = StringUtils.getBoolean(ConsoleUtils.readLine("Data has header row (" + this.hasHeaderRow + "): ", true), this.hasHeaderRow);
	}
	
	/**
	 * Create the database tables.
	 * 
	 */
	public void createDBTables() {
		this.table = new DatabaseTable("data");
		List<DBColumn> columns = extractColumns();
		for(DBColumn column : columns) {
			table.addColumn(column);
		}
		
		database.dropTable(table);
		database.createTable(table);
	}

	/**
	 * Find out the columns that are needed.
	 * 
	 * @return
	 */
	protected List<DBColumn> extractColumns() {
		String line = this.iterator.next();
		if(line == null) {
			return null;
		}
		
		// store first line for insertion if no header row
		if(!this.hasHeaderRow) {
			this.firstLine = line;
		}
		
		// parse the line
		List<DBColumn> columns = new ArrayList<DBColumn>();
		String[] tokens = org.apache.commons.lang3.StringUtils.splitByWholeSeparator(line, getDelimiterString());
		for(String token : tokens) {
			if(AssertUtils.isBlank(token)) {
				columns.add(new DBColumn("col", DBColumnType.STRNG));
				continue;
			}
			
			columns.add(new DBColumn(token, DBHelper.detectColumnType(token)));
		}
		
		return columns;
	}
	
	/**
	 * Load the file
	 */
	@Override
	public void loadFile() throws IOException {
		String[] lineItems;
		String line;
		
		if(firstLine != null) {
			lineItems = org.apache.commons.lang3.StringUtils.splitByWholeSeparator(firstLine, getDelimiterString());
			insertRow(this.table, lineItems);
		}
		
		while(iterator.hasNext()) {
			line = iterator.next();
			
			if(AssertUtils.isBlank(line)) {
				continue;
			}
			
			lineItems = org.apache.commons.lang3.StringUtils.splitByWholeSeparator(line, getDelimiterString());
			insertRow(this.table, lineItems);
		}
	}
	
}