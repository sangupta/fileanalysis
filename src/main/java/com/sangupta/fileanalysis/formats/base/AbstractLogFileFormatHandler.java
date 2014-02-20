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

package com.sangupta.fileanalysis.formats.base;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.sangupta.fileanalysis.db.DBColumnType;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.DatabaseTable;
import com.sangupta.jerry.util.ConsoleUtils;
import com.sangupta.jerry.util.StringUtils;

/**
 * Abstract handler for all application log files. Provides generic contract
 * to work and push their data in DB.
 * 
 * @author sangupta
 *
 */
public abstract class AbstractLogFileFormatHandler extends AbstractFileFormatHandler {

	/**
	 * Read line by line from file
	 */
	private LineIterator iterator;
	
	/**
	 * The database table
	 */
	private DatabaseTable table;
	
	/**
	 * Whether to store long messages or not
	 */
	private boolean storeLongMessages = false;
	
	/**
	 * Should debug level logs be analyzed and stored
	 */
	private boolean skipDebugLevel = false;
	
	/**
	 * Get the date format for the line.
	 * 
	 * @return
	 */
	protected abstract SimpleDateFormat getDateFormat();
	
	/**
	 * Parse the record line and fill the {@link LogRecord} object.
	 * 
	 * @param line
	 * @param record
	 */
	protected abstract void parseRecord(String line, LogRecord record);
	
	/**
	 * Check if the line needs to be skipped.
	 * 
	 * @param line
	 * @return
	 */
	protected abstract boolean isSkipLine(String line);
	
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
		this.storeLongMessages = StringUtils.getBoolean(ConsoleUtils.readLine("Store long error messages (" + this.storeLongMessages + "): ", true), this.storeLongMessages);
		this.skipDebugLevel = StringUtils.getBoolean(ConsoleUtils.readLine("Skip debug level messages (" + this.skipDebugLevel + "): ", true), this.skipDebugLevel);
	}

	/**
	 * Create the table
	 * 
	 */
	@Override
	public void createDBTables() {
		table = new DatabaseTable("logs");
		table.addColumn("date", DBColumnType.TIMESTAMP);
		table.addColumn("level", DBColumnType.STRNG);
		table.addColumn("thread", DBColumnType.STRNG);
		table.addColumn("class", DBColumnType.STRNG);
		table.addColumn("line", DBColumnType.INT);
		table.addColumn("msg", DBColumnType.STRNG);
		table.addColumn("message", DBColumnType.TEXT);
		table.addColumn("error", DBColumnType.TEXT);
		
		this.database.dropTable(table);
		this.database.createTable(table);
	}

	/**
	 * Load the file
	 */
	@Override
	public void loadFile() throws IOException {
		String line = null;
		
		LogRecord record = new LogRecord();
		
		while(iterator.hasNext()) {
			String segment = iterator.next();
			
			if(isSkipLine(segment)) {
				parseRecord(line, record);
				insertRecord(record);
				line = null;
				continue;
			}
			
			if(isNewLogLine(segment)) {
				parseRecord(line, record);
				insertRecord(record);
				line = segment;
				continue;
			}
			
			if(line == null) {
				line = segment;
			} else {
				line = line + "\n" + segment;
			}
		}
	}
	
	/**
	 * Check if this line is a valid log line.
	 * 
	 * @param segment
	 * @return
	 */
	protected abstract boolean isNewLogLine(String segment);

	/**
	 * Insert record in DB
	 * 
	 * @param line
	 */
	private void insertRecord(LogRecord record) {
		if(record.msg == null && record.thread == null && record.date == null) {
			return;
		}
		
		if(skipDebugLevel) {
			if("DEBUG".equals(record.level)) {
				return;
			}
		}
		
		if(record.msg != null) {
			record.msg = record.msg.trim();
			
			record.message = record.msg;
			if(record.msg.length() > 255) {
				record.msg = record.message.substring(0, 255);
			}
		}
		
		if(!storeLongMessages) {
			record.message = null;
			record.error = null;
		}
		
		if(record.level != null) {
			record.level = record.level.trim();
		}
		
		this.insertRow(table, new Object[] { record.date, record.level, record.thread, record.clazz, record.lineNumber, record.msg, record.message, record.error });
		
		record.reset();
	}
	
	/**
	 * The log record which gets filled for each line.
	 * 
	 * @author sangupta
	 *
	 */
	protected static class LogRecord {
		
		public Date date;
		
		public String thread;
		
		public String level;
		
		public String clazz;
		
		public int lineNumber;
		
		public String msg;
		
		public String message;
		
		public String error;

		public void reset() {
			date = null;
			thread = null;
			level = null;
			clazz = null;
			lineNumber = 0;
			msg = null;
			message = null;
			error = null;
		}
	}
	
}
