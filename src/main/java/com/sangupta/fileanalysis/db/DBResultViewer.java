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

package com.sangupta.fileanalysis.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import com.sangupta.jerry.print.ConsoleTable;
import com.sangupta.jerry.util.ConsoleUtils;

/**
 * A simple class to view the results of a DB query via JDBC
 * and display them on {@link System#out}.
 * 
 * @author sangupta
 *
 */
public class DBResultViewer {
	
	protected Database database;
	
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
		final int[] colType = new int[numColumns + 1];
		
		String[] columns = new String[numColumns];
		
		// display the header row
		for(int index = 1; index <= numColumns; index++) {
			colType[index] = meta.getColumnType(index);
			columns[index - 1] = meta.getColumnLabel(index);
		}
		final String[] headers = Arrays.copyOf(columns, columns.length);
		
		// start iterating over the result set
		int rowsDisplayed = 0;
		int numRecords = 0;
		ConsoleTable table = new ConsoleTable();
		table.addHeaderRow(headers);
		while (resultSet.next()) {
			// clean up columns
			Arrays.fill(columns, "");
			
			// read and display the value
			rowsDisplayed++;
			numRecords++;
			
			for(int index = 1; index <= numColumns; index++) {
				switch(colType[index]) {
					case Types.DECIMAL:
					case Types.DOUBLE:
					case Types.REAL:
						columns[index - 1] = String.valueOf(resultSet.getDouble(index));
						continue;
						
					case Types.INTEGER:
					case Types.SMALLINT:
						columns[index - 1] = String.valueOf(resultSet.getInt(index));
						continue;
						
					case Types.VARCHAR:
						columns[index - 1] = resultSet.getString(index);
						continue;
						
					case Types.TIMESTAMP:
						columns[index - 1] = String.valueOf(resultSet.getTimestamp(index));
						continue;

					case Types.BIGINT:
						columns[index - 1] = resultSet.getBigDecimal(index).toString();
						continue;
				}
			}
			table.addRow((Object[]) columns);
			
			// check for rows displayed
			if(rowsDisplayed == 20) {
				// display rows
				table.write(System.out);
				
				// ask the user if more data needs to be displayed
				String cont = ConsoleUtils.readLine("\nType \"it\" for more: ", true);
				if(!"it".equalsIgnoreCase(cont)) {
					table = null;
					break;
				}
				
				System.out.println();
				
				// continue;
				rowsDisplayed = 0;
				table = new ConsoleTable();
				table.addHeaderRow(headers);
				continue;
			}
		}
		
		if(table != null) {
			table.write(System.out);
		}
		
		System.out.println("\nTotal number of rows displayed: " + numRecords);
	}

}
