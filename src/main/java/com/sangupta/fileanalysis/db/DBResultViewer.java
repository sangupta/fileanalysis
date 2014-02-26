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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.sangupta.fileanalysis.FileAnalysisHelper;
import com.sangupta.jerry.util.ConsoleUtils;

/**
 * A simple class to view the results of a DB query via JDBC
 * and display them on {@link System#out}.
 * 
 * @author sangupta
 *
 */
public class DBResultViewer {
	
	private Database database;
	
	public DBResultViewer(Database db) {
		this.database = db;
	}
	
	/**
	 * View results of a {@link Statement}.
	 * 
	 * @param statement
	 * @throws SQLException
	 */
	public void viewResult(Statement statement) throws SQLException {
		if(statement == null) {
			// nothing to do
			return;
		}
		
		if(statement.getResultSet() != null) {
			// results were obtained
			viewResult(statement.getResultSet());
			return;
		}
		
		// we do not have any result set
		// may be an update count etc?
		System.out.println("Records updated: " + statement.getUpdateCount());
	}

	/**
	 * View resutls of a {@link ResultSet}.
	 * 
	 * @param resultSet
	 * @throws SQLException 
	 */
	public void viewResult(ResultSet resultSet) throws SQLException {
		if(resultSet == null) {
			// nothing to do
			return;
		}
		
		// collect the meta
		ResultSetMetaData meta = resultSet.getMetaData();
		
		final int numColumns = meta.getColumnCount();
		final int[] displaySizes = new int[numColumns + 1];
		final int[] colType = new int[numColumns + 1];
		
		for(int index = 1; index <= numColumns; index++) {
			colType[index] = meta.getColumnType(index);
			displaySizes[index] = getColumnSize(meta.getTableName(index), meta.getColumnName(index), colType[index]);
		}
		
		// display the header row
		for(int index = 1; index <= numColumns; index++) {
			center(meta.getColumnLabel(index), displaySizes[index]);
		}
		System.out.println("|");
		for(int index = 1; index <= numColumns; index++) {
			System.out.print("+" + StringUtils.repeat('-', displaySizes[index] + 2));
		}
		System.out.println("+");
		
		// start iterating over the result set
		int rowsDisplayed = 0;
		int numRecords = 0;
		while (resultSet.next()) {
			// read and display the value
			rowsDisplayed++;
			numRecords++;
			
			for(int index = 1; index <= numColumns; index++) {
				switch(colType[index]) {
					case Types.DECIMAL:
					case Types.DOUBLE:
					case Types.REAL:
						format(resultSet.getDouble(index), displaySizes[index]);
						continue;
						
					case Types.INTEGER:
					case Types.SMALLINT:
						format(resultSet.getInt(index), displaySizes[index]);
						continue;
						
					case Types.VARCHAR:
						format(resultSet.getString(index), displaySizes[index], false);
						continue;
						
					case Types.TIMESTAMP:
						format(resultSet.getTimestamp(index), displaySizes[index]);
						continue;

					case Types.BIGINT:
						format(resultSet.getBigDecimal(index), displaySizes[index]);
						continue;
				}
			}
			
			// terminator for row and new line
			System.out.println("|");
			
			// check for rows displayed
			if(rowsDisplayed == 20) {
				// ask the user if more data needs to be displayed
				String cont = ConsoleUtils.readLine("Type \"it\" for more: ", true);
				if(!"it".equalsIgnoreCase(cont)) {
					break;
				}
				
				// continue;
				rowsDisplayed = 0;
				continue;
			}
		}
		
		System.out.println("\nTotal number of records found: " + numRecords);
	}

	private void format(Timestamp timestamp, int size) {
		if(timestamp == null) {
			format((String) null, size, true);
			return;
		}
		
		long l = timestamp.getTime();
		Date d = new Date(l);
		format(FileAnalysisHelper.LOG_DATE_FORMAT.format(d), size, false);
	}

	private int getColumnSize(final String tableName, final String columnName, final int columnType) {
		// check if we have the value in database cache
		int size = database.getColSize(tableName, columnName);
		if(size > 0) {
			return size;
		}
		
		switch(columnType) {
			case Types.VARCHAR:
				return 255;
				
			case Types.INTEGER:
				return 10;
				
			case Types.BOOLEAN:
				return 3;
				
			case Types.DOUBLE:
				return 14;
				
			case Types.BIGINT:
				return 20;
				
			case Types.TIMESTAMP:
				return 26;
		}
		
		return 10;
	}

	private static void format(double value, int size) {
		format(String.valueOf(value), size, true);
	}

	private static void format(int value, int size) {
		format(String.valueOf(value), size, true);
	}

	private void format(BigDecimal bigDecimal, int size) {
		format(bigDecimal.longValue(), size);
	}

	private static void center(String value, int size) {
		System.out.print("| " + StringUtils.center(value, size) + " ");
	}

	private static void format(String value, int size, boolean rightAligned) {
		if(rightAligned) {
			value = StringUtils.leftPad(value, size);
		} else {
			value = StringUtils.rightPad(value, size);
		}
		
		System.out.print("| " + value + " ");
	}
}
