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

package com.sangupta.fileanalysis.formats;

import java.io.File;

import com.sangupta.fileanalysis.FileFormatHandler;
import com.sangupta.fileanalysis.db.DBColumn;
import com.sangupta.fileanalysis.db.DBColumnType;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.DatabaseTable;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;

/**
 * 
 * @author sangupta
 *
 */
public abstract class AbstractFileFormatHandler implements FileFormatHandler {
	
	/**
	 * The database we are connected to
	 */
	protected Database database;

	/**
	 * The file we are working on
	 */
	protected File file;
	
	/**
	 * Initialize 
	 * 
	 * @param database
	 * @param file
	 */
	public void setDBAndFile(Database database, File file) {
		this.database = database;
		this.file = file;
	}
	
	/**
	 * Read the configuration from the user.
	 * 
	 */
	@Override
	public void readConfiguration() {
		// do nothing
	}
	
	/**
	 * Insert data as a row into the database for the given table.
	 * 
	 * @param table
	 * @param row
	 */
	protected void insertRow(DatabaseTable table, Object[] row) {
		if(row == null) {
			return;
		}
		
		// all blank items?
		boolean allBlank = true;
		for(Object item : row) {
			if(item instanceof String) {
				if(AssertUtils.isNotBlank((String) item)) {
					allBlank = false;
					break;
				}
			} else {
				if(item != null) {
					allBlank = false;
					break;
				}
			}
		}
		
		if(allBlank) {
			return;
		}
		
		// insert the row
		this.database.insertRecord(table, row);
	}
	
	/**
	 * Read {@link DBColumn} details from the command line.
	 * 
	 * @param token
	 * @return
	 */
	protected DBColumn getDBColumnFromUser(String token) {
		System.out.println("Valid column types: int, long, date, str, text");
		String colNameType = ConsoleUtils.readLine("Column name and type for (" + token + "): ", true);
		
		if(AssertUtils.isNotEmpty(colNameType)) {
			String[] split = colNameType.split(",");
			if(split.length == 2) {
				String colName = split[0];
				DBColumnType colType = DBColumnType.decipherColumnType(split[1]);
				
				return new DBColumn(colName, colType);
			}
		}
		
		return null;
	}
}
