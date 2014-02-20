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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.sangupta.fileanalysis.db.DBColumnType;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.DatabaseTable;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;
import com.sangupta.jerry.util.StringUtils;

/**
 * 
 * @author sangupta
 *
 */
public class LogbackFileHandler extends AbstractFileFormatHandler {
	
	public static final SimpleDateFormat LOGBACK_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
	
	private LineIterator iterator;
	
	private DatabaseTable table;
	
	private boolean storeLongMessages = false;
	
	private boolean skipDebugLevel = false;
	
	@Override
	public void initialize(Database database, File file) {
		super.setDBAndFile(database, file);
		
		try {
			iterator = FileUtils.lineIterator(this.file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void readConfiguration() {
		this.storeLongMessages = StringUtils.getBoolean(ConsoleUtils.readLine("Store long error messages: ", true), this.storeLongMessages);
		this.skipDebugLevel = StringUtils.getBoolean(ConsoleUtils.readLine("Skip debug level messages: ", true), this.skipDebugLevel);
	}

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

	@Override
	public void loadFile() throws IOException {
		String line = null;
		
		while(iterator.hasNext()) {
			String segment = iterator.next();
			
			if(segment.startsWith("#logback")) {
				parseAndInsertRecord(line);
				line = null;
				continue;
			}
			
			if(isNewLogLine(segment)) {
				parseAndInsertRecord(line);
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
	private boolean isNewLogLine(String segment) {
		return extractDate(segment) == null ? false : true;
	}
	
	/**
	 * 
	 * @param segment
	 * @return
	 */
	private Date extractDate(String segment) {
		int space = segment.indexOf(' ');
		if(space == -1) {
			return null;
		}
		
		space = segment.indexOf(' ', space + 1);
		if(space == -1) {
			return null;
		}
		
		String date = segment.substring(0, space);
		try {
			return LOGBACK_DATE_FORMAT.parse(date);
		} catch(ParseException e) {
		}
		
		return null;
	}

	/**
	 * Insert record in DB
	 * 
	 * @param line
	 * @param iterator2
	 */
	private void parseAndInsertRecord(String line) {
		if(AssertUtils.isEmpty(line)) {
			return;
		}
		
		int start = line.indexOf(' ');
		start = line.indexOf(' ', start + 1);
		
		String dateStr = line.substring(0, start);
		Date date = null;
		try {
			date = LOGBACK_DATE_FORMAT.parse(dateStr);
		} catch(ParseException e) {
			// eat up
		}
		
		int end = line.indexOf(' ', start + 1);
		final String level = line.substring(start + 1, end);
		
		if(this.skipDebugLevel && "DEBUG".equals(level)) {
			return;
		}
		
		start = end;
		end = line.indexOf(']', end);
		final String thread = line.substring(line.indexOf('[', start) + 1, end);
		
		start = end;
		end = line.indexOf(']', end + 1);
		final String clazz = line.substring(line.indexOf('[', start) + 1, end);
		
		start = end;
		end = line.indexOf('\n', end + 1);
		
		start = line.indexOf('-', start) + 1;
		String msg, message, error;
		if(end < 0) {
			msg = line.substring(start);
			error = "";
			end = line.length();
		} else {
			msg = line.substring(start, end);
			error = line.substring(end + 1);
		}
		
		message = msg;
		if(msg.length() > 255) {
			msg = message.substring(0, 255);
		}
		
		if(!this.storeLongMessages) {
			message = null;
			error = null;
		}
		
		this.insertRow(table, new Object[] { date, level, thread, clazz, "0", msg, message, error });
	}

}
