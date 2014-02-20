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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author sangupta
 *
 */
public class FileAnalysisHelper {
	
	public static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
	
	public static boolean isAnyOf(String value, String[] list) {
		for(String item : list) {
			if(item.equals(value)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean startsWithAny(String value, String[] list) {
		for(String item : list) {
			if(value.startsWith(item)) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean containsAny(String value, String[] list) {
		for(String item : list) {
			if(value.contains(item)) {
				return true;
			}
		}
		
		return false;
	}

	public static Timestamp tryDateParse(String item) {
		try {
			Date date = LOG_DATE_FORMAT.parse(item);
			return new Timestamp(date.getTime());
		} catch(ParseException e) {
			// eat up
		}
		
		return null;
	}
}
