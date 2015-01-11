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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sangupta.jerry.util.AssertUtils;

/**
 * Simple utility class to work with H2 database using plain
 * simple JDBC.
 * 
 * @author sangupta
 *
 */
public class Database {
	
	/**
	 * The database connection
	 */
	private final Connection connection;
	
	final Map<String, PreparedStatement> statements = new HashMap<String, PreparedStatement>();
	
	final Map<String, Integer> maxColSizes = new HashMap<String, Integer>();
	
	final DatabaseTable colSizes;
	
	final List<DatabaseTable> tables = new ArrayList<DatabaseTable>();

	/**
	 * Initialize the database
	 */
	public Database(String databaseName) {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:" + databaseName);
		} catch (ClassNotFoundException e) {
			// no DB driver found
			throw new RuntimeException("No H2 driver found - use the JAR with embedded dependencies");
		} catch (SQLException e) {
			throw new RuntimeException("Unable to obtain connection with database");
		}
		
		// create a new database table to hold col sizes
		colSizes = new DatabaseTable("FA_COL_SIZES");
		colSizes.addColumn(new DBColumn("COL_NAME", DBColumnType.STRNG));
		colSizes.addColumn(new DBColumn("COL_SIZE", DBColumnType.INT));
		
		execute(colSizes.getCreateQuery());
	}
	
	/**
	 * Create a new database table.
	 * 
	 * @param tableName
	 * @param columns
	 */
	public void createTable(DatabaseTable table) {
		execute(table.getCreateQuery());
		tables.add(table);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public boolean execute(String query) {
		return this.execute(query, null);
	}
	
	/**
	 * Execute a simple string query.
	 * 
	 * @param query
	 * @return
	 */
	public boolean execute(String query, SQLStatementConsumer consumer) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			boolean success = statement.execute(query);
			
			if(consumer != null) {
				consumer.consume(statement);
			}
			
			return success;
		} catch (SQLException e) {
			System.out.println("SQL Error: " + e.getMessage());
			return false;
		} finally {
			if(statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Execute the given query.
	 * 
	 * @param query
	 */
	public void executeQuery(String query) {
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if(resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Close the database.
	 * 
	 */
	public void closeDatabase() {
		if(this.connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean insertRecord(DatabaseTable table, Object[] row) {
		PreparedStatement ps = null;
		try {
			ps = prepareStatement(table, row);

			table.updateMaxColSize(row);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return execute(ps);
	}
	
	/**
	 * 
	 * @param ps
	 * @return
	 */
	private boolean execute(PreparedStatement ps) {
		try {
			ps.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * Get a cached {@link PreparedStatement} with populated values for the given types.
	 * 
	 * @param table
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement prepareStatement(DatabaseTable table, Object[] row) throws SQLException {
		PreparedStatement ps = statements.get(table.getName());
		if(ps == null) {
			ps = this.connection.prepareStatement(table.getInsertQuery());
			statements.put(table.getName(), ps);
		}
		
		DBHelper.populatePreparedStatement(ps, table.getColumns(), row);
		return ps;
	}

	/**
	 * 
	 * @param table
	 */
	public void dropTable(DatabaseTable table) {
		execute(table.getDropTableQuery());
	}

	/**
	 * 
	 */
	public void updateColSizes() {
		if(this.tables.size() == 0) {
			return;
		}
		
		for(DatabaseTable table : this.tables) {
			for(DBColumn col : table.getColumns()) {
				String name = getColSizeKey(table.getName(), col.name);
				int size = col.maxSize;
				
				insertRecord(colSizes, new String[] { name, String.valueOf(size) });
				maxColSizes.put(name, size);
			}
		}
	}

	/**
	 * Return the column size of the given column.
	 * 
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	public int getColSize(String tableName, String columnName) {
		if(AssertUtils.isEmpty(tableName) || AssertUtils.isEmpty(columnName)) {
			return 0;
		}

		String key = getColSizeKey(DBHelper.sanitizeName(tableName), DBHelper.sanitizeName(columnName));
		Integer colSize = maxColSizes.get(key);
		if(colSize != null) {
			return colSize;
		}
		
		// TODO: add functionality that if value not found in cache
		// we try read from database table
		return 0;
	}

	/**
	 * 
	 * @param tablename
	 * @param columnName
	 * @return
	 */
	private static String getColSizeKey(String tablename, String columnName) {
		return "FA_" + tablename + "_" + columnName;
	}
}