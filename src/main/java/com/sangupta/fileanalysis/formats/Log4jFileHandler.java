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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sangupta.jerry.util.AssertUtils;

/**
 * Handle <code>Apache log4j</code> log files.
 * 
 * @author sangupta
 *
 */
public class Log4jFileHandler extends AbstractLogFileFormatHandler {
	
	public static final SimpleDateFormat LOG4J_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy hh:mm:ss,SSS");
	
	@Override
	protected SimpleDateFormat getDateFormat() {
		return LOG4J_DATE_FORMAT;
	}

	@Override
	protected void parseRecord(final String line, final LogRecord record) {
		if(AssertUtils.isEmpty(line)) {
			return;
		}
		
		int start = line.indexOf('[');
		
		String dateStr = line.substring(0, start).trim();
		try {
			record.date = LOG4J_DATE_FORMAT.parse(dateStr);
		} catch(ParseException e) {
			// eat up
		}
		
		int end = line.indexOf(']', start + 1);
		record.thread = line.substring(start + 1, end);
		
		// find separator
		start = end;
		start = line.indexOf("[:] ", start);
		end = line.indexOf(' ', start + 4);
		record.level = line.substring(start + 3, end);
		
		start = end;
		end = line.indexOf(" - ", end + 1);
		record.clazz = line.substring(start + 1, end);
		
		start = end;
		end = line.indexOf('\n', end + 1);
		
		start = line.indexOf('-', start) + 1;
		if(end < 0) {
			record.msg = line.substring(start);
			record.error = null;
			end = line.length();
		} else {
			record.msg = line.substring(start, end);
			record.error = line.substring(end + 1);
		}
	}

	@Override
	protected boolean isSkipLine(String line) {
		return false;
	}

	@Override
	protected boolean isNewLogLine(String segment) {
		return extractDate(segment) == null ? false : true;
	}

	private Date extractDate(String segment) {
		int start = segment.indexOf('[');
		if(start == -1) {
			return null;
		}
		
		String dateStr = segment.substring(0, start).trim();
		try {
			return LOG4J_DATE_FORMAT.parse(dateStr);
		} catch(ParseException e) {
			// eat up
		}
		
		return null;
	}

}
