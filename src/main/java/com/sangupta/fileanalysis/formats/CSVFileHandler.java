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

package com.sangupta.fileanalysis.formats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.sangupta.fileanalysis.db.DBColumn;
import com.sangupta.fileanalysis.db.DBColumnType;
import com.sangupta.fileanalysis.db.DBHelper;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.DatabaseTable;
import com.sangupta.fileanalysis.formats.base.AbstractFileFormatHandler;

/**
 * 
 * @author sangupta
 *
 */
public class CSVFileHandler extends AbstractFileFormatHandler {
	
	private CSVReader reader;
	
	private DatabaseTable table;
	
	/**
	 * preserve the first row to be stored to be loaded when we
	 * start loading
	 */
	private String[] firstRow;

	/**
	 * Initialize the handler
	 * 
	 */
	public void initialize(Database database, File file) {
		super.setDBAndFile(database, file);

		try {
			this.reader = new CSVReader(new FileReader(this.file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
	 * Extract the columns names for the table
	 * 
	 * @return
	 */
	private List<DBColumn> extractColumns() {
		String[] columnNames = readLine();
		if(columnNames == null) {
			throw new RuntimeException("Header line not found in CSV");
		}
		
		List<DBColumn> columns = new ArrayList<DBColumn>();

		firstRow = readLine();
		if(firstRow == null) {
			// no data available
			// create an empty table
			for(String name : columnNames) {
				columns.add(new DBColumn(name, DBColumnType.STRNG));
			}
			
			return columns;
		}
		
		// the first row data is available
		// analyze each item
		int maxIndex = Math.max(columnNames.length, firstRow.length);
		for(int index = 0; index < maxIndex; index++) {
			String name;
			if(index < columnNames.length) {
				name = columnNames[index];
			} else {
				name = "col";
			}
			
			if(index < firstRow.length) {
				columns.add(new DBColumn(name, DBHelper.detectColumnType(firstRow[index])));
			} else {
				columns.add(new DBColumn(name, DBColumnType.STRNG));
			}
		}
		
		return columns;
	}
	
	/**
	 * Read next CSV line quietly.
	 * 
	 * @return
	 */
	private String[] readLine() {
		try {
			return this.reader.readNext();
		} catch (IOException e) {
			// eat up
		}
		
		return null;
	}

	/**
	 * Load the remaining chunk of file.
	 * 
	 */
	public void loadFile() throws IOException {
		// load the first row
		insertRow(this.table, firstRow);
		
		// load rest of the rows
		String[] lineItems;
		while((lineItems = this.reader.readNext()) != null) {
			insertRow(this.table, lineItems);
		}
		
		try {
			this.reader.close();
		} catch (IOException e) {
			// eat up
		}
	}

}