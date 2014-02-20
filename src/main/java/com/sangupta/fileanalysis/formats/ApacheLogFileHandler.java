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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import com.sangupta.fileanalysis.FileAnalysisHelper;
import com.sangupta.fileanalysis.db.DBColumn;
import com.sangupta.fileanalysis.db.DBColumnType;
import com.sangupta.fileanalysis.db.Database;
import com.sangupta.fileanalysis.db.DatabaseTable;
import com.sangupta.fileanalysis.formats.base.AbstractFileFormatHandler;

/**
 * 
 * @author sangupta
 *
 */
public class ApacheLogFileHandler extends AbstractFileFormatHandler {
	
	private static final String[] EMPTY_STRING_ARRAY = new String[] { };

	private static final String[] HTTP_VERBS = { "GET", "PUT", "POST", "DELETE", "PATCH", "TRACE" };
	
	private static final String MAC_OS = "osx";
	
	private static final String WINDOWS = "windows";
	
	private static final String OTHER_OS = "other";
	
	private static final String[] OS_TYPES = { MAC_OS, WINDOWS, OTHER_OS };
	
	private static final String[] RESPONSE_TYPE = { "text/html", "text/css", "text/javascript", "image/png", "image/gif" };
	
	private static final String[] SCHEMES = { "http://", "https://" };
	
	private static final String[] PROTOS = { "HTTP/", "HTTPS/" };

	private static final String[] OS_DECIPHER_WORDS = { "Macintosh", "Mac OS X" };

	private static final String[] BROWSER_DECIPHER_WORDS = { "AppleWebKit", "Gecko", "Chrome", "KHTML", "Safari" };

	private DatabaseTable table;
	
	private String firstLine;
	
	private LineIterator iterator;
	
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
	public void createDBTables() {
		this.firstLine = iterator.next();
		if(this.firstLine == null) {
			return;
		}

		// create the db table
		table = new DatabaseTable("logs");
		
		// find column names
		List<String> tokens = parseLogLine(this.firstLine);
		DBColumn column = null;
		
		for(int index = 0; index < tokens.size(); index++) {
			String token = tokens.get(index);
			column = detectDBColumn(token, column);
			if(column == null) {
				// ask the user for column type
				column = getDBColumnFromUser(token);
				
				if(column == null) {
					column = new DBColumn("col", DBColumnType.STRNG);
				}
			}
			
			table.addColumn(column);
		}
		
		// create the table now
		database.dropTable(table);
		database.createTable(table);
	}

	private DBColumn detectDBColumn(String token, DBColumn lastDetectedColumn) {
		if(token.equals("-")) {
			return new DBColumn("col", DBColumnType.STRNG);
		}
		
		// for last column
		if(lastDetectedColumn != null) {
			if("verb".equals(lastDetectedColumn.name)) {
				return new DBColumn("path", DBColumnType.STRNG);
			}
		}
		
		// check if this is an IP
		int dots = StringUtils.countMatches(token, ".");
		if(dots == 3) {
			return new DBColumn("ip", DBColumnType.STRNG);
		}
		
		// check for date and time
		if(token.contains("/") && token.contains(":")) {
			return new DBColumn("date", DBColumnType.TIMESTAMP);
		}
		
		// check for verb
		if(FileAnalysisHelper.isAnyOf(token, HTTP_VERBS)) {
			return new DBColumn("verb", DBColumnType.STRNG);
		}

		// check for os
		if(FileAnalysisHelper.isAnyOf(token, OS_TYPES)) {
			return new DBColumn("os", DBColumnType.STRNG);
		}
		
		// response type
		if(FileAnalysisHelper.isAnyOf(token, RESPONSE_TYPE)) {
			return new DBColumn("response_type", DBColumnType.STRNG);
		}
		
		// referrer
		if(FileAnalysisHelper.startsWithAny(token, SCHEMES)) {
			return new DBColumn("referrer", DBColumnType.STRNG);
		}
		
		// protocol
		if(FileAnalysisHelper.startsWithAny(token, PROTOS)) {
			return new DBColumn("protocol", DBColumnType.STRNG);
		}
		
		if(FileAnalysisHelper.containsAny(token, OS_DECIPHER_WORDS) && FileAnalysisHelper.containsAny(token, BROWSER_DECIPHER_WORDS)) {
			return new DBColumn("user_agent", DBColumnType.STRNG);
		}
		
		return null;
	}

	@Override
	public void loadFile() throws IOException {
		String[] tokens = parseLogLine(this.firstLine).toArray(EMPTY_STRING_ARRAY);
		insertRow(this.table, tokens);
		
		// start iterating over other rows
		String line;
		while(this.iterator.hasNext()) {
			line = iterator.next();
			tokens = parseLogLine(line).toArray(EMPTY_STRING_ARRAY);
			insertRow(this.table, tokens);
		}
	}
	
	private static List<String> parseLogLine(final String logLine) {
		List<String> tokens = parseBaseTokens(logLine);
		
		// bifurcate the request method etc as well
		int endIndex = tokens.size();
		for(int index = 0; index < endIndex; index++) {
			String token = tokens.get(index);
			
			// check for http verb
			if(FileAnalysisHelper.startsWithAny(token, HTTP_VERBS)) {
				int space = token.indexOf(' ');
				if(space != -1) {
					String verb = token.substring(0, space);
					if(FileAnalysisHelper.isAnyOf(verb, HTTP_VERBS)) {
						// add a new token here - verb comes first
						tokens.add(index, verb);
						index++;
						endIndex++;
						
						// extract protocol
						int lastSpace = token.lastIndexOf(' ');
						if(lastSpace != space) {
							String protocol = token.substring(lastSpace + 1);
							tokens.add(index + 1, protocol);
							index++;
							endIndex++;
						}
						
						// trim the current path
						if(space != lastSpace) {
							token = token.substring(space + 1, lastSpace);
							tokens.set(index - 1, token);
						} else {
							token = token.substring(space + 1);
							tokens.set(index - 1, token);
						}
						
						continue;
					}
				}
			}
			
			// check for OS
			if(FileAnalysisHelper.containsAny(token, OS_DECIPHER_WORDS) && FileAnalysisHelper.containsAny(token, BROWSER_DECIPHER_WORDS)) {
				String os = decipherOSFromUserAgent(token);
				if(os != null) {
					tokens.add(index, os);
					index++;
					endIndex++;
					continue;
				}
			}
		}
		
		return tokens;
	}

	/**
	 * Find the user agent details from the string.
	 * 
	 * @param token
	 * @return
	 */
	private static String decipherOSFromUserAgent(String token) {
		return null;
	}

	/**
	 * 
	 * @param logLine
	 * @return
	 */
	private static List<String> parseBaseTokens(final String logLine) {
		List<String> tokens = new ArrayList<String>();
		
		StringBuilder builder = new StringBuilder(100);
		char[] charArray = logLine.toCharArray();
		
		boolean inQuotes = false;
		boolean inBrackets = false;
		
		for (int index = 0; index < charArray.length; index++) {
			if (charArray[index] == '"') {
				inQuotes = inQuotes ? false : true;
			} else if (charArray[index] == '[') {
				inBrackets = true;
			} else if (charArray[index] == ']') {
				if (inBrackets) {
					inBrackets = false;
				}
			} else if (charArray[index] == ' ' && (!inQuotes) && (!inBrackets)) {
				tokens.add(builder.toString());
				builder.setLength(0);
			} else {
				builder.append(charArray[index]);
			}
		}
		
		if (builder.length() > 0) {
			tokens.add(builder.toString());
		}
		
		return tokens;
	}

}
