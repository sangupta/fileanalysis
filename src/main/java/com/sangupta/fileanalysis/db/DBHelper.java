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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 
 * @author sangupta
 *
 */
public class DBHelper {
	
	public static DBColumnType detectColumnType(String item) {
		// check for number
		try {
			Long.parseLong(item);
			return DBColumnType.LONG;
		} catch(NumberFormatException e) {
			
		}
		
		// check for double
		try {
			Double.parseDouble(item);
			return DBColumnType.DOUBLE;
		} catch(NumberFormatException e) {
			
		}
		
		return DBColumnType.STRNG;
	}

	/**
	 * Sanitize name of table or column
	 * 
	 * @param name
	 * @return
	 */
	public static String sanitizeName(String name) {
		return name.toLowerCase();
	}

	/**
	 * Populate a prepared statement with the values for given columns
	 * 
	 * @param statement
	 * @param list 
	 * @param items
	 * @throws SQLException
	 */
	public static void populatePreparedStatement(PreparedStatement statement, List<DBColumn> columns, Object[] items) throws SQLException {
		if(items.length > columns.size()) {
			throw new RuntimeException("Number of columns are less than number of values in rows");
		}
		
		for(int index = 1; index <= items.length; index++) {
			Object item = items[index - 1];
			DBColumnType type = columns.get(index - 1).columnType;

			switch(type) {
				case LONG:
					statement.setLong(index, type.parseLong(item));
					continue;
					
				case INT:
					statement.setInt(index, type.parseInt(item));
					continue;
					
				case DOUBLE:
					statement.setDouble(index, type.parseDouble(item));
					continue;
					
				case STRNG:
					statement.setString(index, item.toString());
					continue;
					
				case TIMESTAMP:
					statement.setTimestamp(index, type.parseTimestamp(item));
					continue;
					
				case TEXT:
					statement.setCharacterStream(index, type.parseText(item));
					continue;
					
				default:
					throw new RuntimeException("Not yet implemented");
			}
		}		
	}

}
