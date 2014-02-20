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

package com.sangupta.fileanalysis.db;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Date;

import com.sangupta.fileanalysis.FileAnalysisHelper;
import com.sangupta.jerry.util.AssertUtils;

/**
 * 
 * @author sangupta
 *
 */
public enum DBColumnType {
	
	INT("INT"),
	
	LONG("BIGINT"),
	
	DOUBLE("DOUBLE"),
	
	TIME("TIME"),
	
	DATE("DATE"),
	
	TIMESTAMP("TIMESTAMP"),
	
	STRNG("VARCHAR(255)"),
	
	TEXT("CLOB");
	
	private String dbType;
	
	private DBColumnType(String type) {
		this.dbType = type;
	}
	
	public String getDBType() {
		return this.dbType;
	}
	
	public long parseLong(Object obj) {
		if(obj instanceof String) {
			String item = (String) obj;
			if(AssertUtils.isBlank(item)) {
				return 0;
			}
			
			return Long.parseLong(item);
		}
		
		if(obj instanceof Integer) {
			return ((Integer) obj).longValue();
		}
		
		if(obj instanceof Long) {
			return (Long) obj;
		}
		
		return 0;
	}
	
	public int parseInt(Object obj) {
		if(obj instanceof String) {
			String item = (String) obj;
			if(AssertUtils.isBlank(item)) {
				return 0;
			}
			
			return Integer.parseInt(item);
		}
		
		if(obj instanceof Integer) {
			return (Integer) obj;
		}
		
		if(obj instanceof Long) {
			return ((Long) obj).intValue();
		}
		
		return 0;
	}
	
	public double parseDouble(Object obj) {
		if(obj instanceof String) {
			String item = (String) obj;
			if(AssertUtils.isBlank(item)) {
				return 0d;
			}
			
			return Double.parseDouble(item);
		}
		
		if(obj instanceof Integer) {
			return ((Integer) obj).doubleValue();
		}
		
		if(obj instanceof Long) {
			return ((Long) obj).doubleValue();
		}
		
		if(obj instanceof Double) {
			return (Double) obj;
		}
		
		return 0d;
	}

	public static DBColumnType decipherColumnType(String str) {
		if(AssertUtils.isEmpty(str)) {
			return null;
		}
		
		if("int".equalsIgnoreCase(str)) {
			return INT;
		}
		
		if("long".equalsIgnoreCase(str)) {
			return LONG;
		}
		
		if("str".equalsIgnoreCase(str)) {
			return STRNG;
		}
		
		if("text".equalsIgnoreCase(str)) {
			return TEXT;
		}
		
		if("date".equalsIgnoreCase(str)) {
			return TIMESTAMP;
		}
		
		return null;
	}

	public Timestamp parseTimestamp(Object item) {
		if(item instanceof Date) {
			return new Timestamp(((Date) item).getTime());
		}
		
		return FileAnalysisHelper.tryDateParse((String) item);
	}

	public Reader parseText(Object item) {
		if(item == null) {
			return new StringReader("");
		}
		
		String text = item.toString();
		return new StringReader(text);
	}
	
}
