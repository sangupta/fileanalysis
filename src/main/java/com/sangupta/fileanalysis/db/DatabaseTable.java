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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author sangupta
 *
 */
public class DatabaseTable {
	
	private final String name;
	
	private final List<DBColumn> columns = new ArrayList<DBColumn>();
	
	public DatabaseTable(String name) {
		this.name = DBHelper.sanitizeName(name);
	}

	public void addColumn(DBColumn column) {
		// check for col name
		for(DBColumn col : this.columns) {
			if(col.name.equals(column.name)) {
				// readjust column name
				column = column.getCloned(column.name + this.columns.size());
			}
		}
		this.columns.add(column);
	}
	
	public void addColumn(String name, DBColumnType type) {
		this.columns.add(new DBColumn(name, type));
	}
	
	/**
	 * Drop this table SQL query.
	 * 
	 * @return
	 */
	public String getDropTableQuery() {
		return "DROP TABLE IF EXISTS " + this.name;
	}
	
	/**
	 * Create query to create this table.
	 * 
	 * @return
	 */
	public String getCreateQuery() {
		StringBuilder builder = new StringBuilder(1024);
		
		builder.append("CREATE TABLE IF NOT EXISTS ");
		builder.append(this.name);
		builder.append(" ( LINENUM LONG AUTO_INCREMENT");
		
		// find primary key columns
		// run for all non key columns
		for(DBColumn col : this.columns) {
			builder.append(", " + col.name + " " + col.columnType.getDBType());
		}
		
		
		builder.append(");");
		
		return builder.toString();
	}

	/**
	 * Create query to insert data into this table
	 * 
	 * @return
	 */
	public String getInsertQuery() {
		StringBuilder builder = new StringBuilder(1024);
		
		builder.append("INSERT INTO ");
		builder.append(this.name);
		builder.append("( ");
		
		boolean first = true;
		for(DBColumn col : this.columns) {
			if(!first) {
				builder.append(", ");
			}
			first = false;
			builder.append(col.name);
		}
		builder.append(" ) VALUES (");
		
		first = true;
		for(int index = 0; index < this.columns.size(); index++) {
			if(!first) {
				builder.append(',');
			}
			first = false;
			builder.append(" ?");
		}
		
		builder.append(" );");
		
		return builder.toString();
	}
	
	// Usual accessors follow

	public String getName() {
		return name;
	}

	public List<DBColumn> getColumns() {
		return columns;
	}

	/**
	 * Update maximum column size for each item in the row
	 * 
	 * @param row
	 */
	public void updateMaxColSize(Object[] row) {
		int max = Math.min(row.length, this.columns.size());
		for(int index = 0; index < max; index++) {
			if(row[index] != null) {
				DBColumn col = this.columns.get(index);
				col.maxSize = Math.max(col.maxSize, row[index].toString().trim().length());
			}
		}
	}

}
