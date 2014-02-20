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

import com.sangupta.fileanalysis.FileFormatHandler;
import com.sangupta.fileanalysis.formats.base.AbstractLogFileFormatHandler;
import com.sangupta.jerry.util.AssertUtils;

/**
 * {@link FileFormatHandler} for working with <code>Logback</code> log file. Logback
 * is an implementation to <code>slf4j</code>.
 * 
 * @author sangupta
 *
 */
public class LogbackFileHandler extends AbstractLogFileFormatHandler {
	
	public static final SimpleDateFormat LOGBACK_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
	
	@Override
	protected SimpleDateFormat getDateFormat() {
		return LOGBACK_DATE_FORMAT;
	}

	@Override
	protected void parseRecord(final String line, final LogRecord record) {
		if(AssertUtils.isEmpty(line)) {
			return;
		}
		
		int start = line.indexOf(' ');
		start = line.indexOf(' ', start + 1);
		
		String dateStr = line.substring(0, start);
		try {
			record.date = LOGBACK_DATE_FORMAT.parse(dateStr);
		} catch(ParseException e) {
			// eat up
		}
		
		int end = line.indexOf(' ', start + 1);
		record.level = line.substring(start + 1, end);
		
		start = end;
		end = line.indexOf(']', end);
		record.thread = line.substring(line.indexOf('[', start) + 1, end);
		
		start = end;
		end = line.indexOf(']', end + 1);
		record.clazz = line.substring(line.indexOf('[', start) + 1, end);
		
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

	/**
	 * Check if this line is a valid log line.
	 * 
	 * @param segment
	 * @return
	 */
	@Override
	protected boolean isNewLogLine(String segment) {
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
			return getDateFormat().parse(date);
		} catch(ParseException e) {
		}
		
		return null;
	}
	
	@Override
	protected boolean isSkipLine(String line) {
		if(line.startsWith("#logback.classic pattern:")) {
			return true;
		}
		
		return false;
	}

}
