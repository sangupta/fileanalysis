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

import com.sangupta.jerry.util.AssertUtils;

/**
 * 
 * @author sangupta
 *
 */
public class DBColumn {
	
	public final String name;
	
	public final DBColumnType columnType;
	
	int maxSize;
	
	public DBColumn(String name, DBColumnType type) {
		if(AssertUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Cannot create DBColumn with empty/null name");
		}
		
		if(type == null) {
			type = DBColumnType.STRNG;
		}
		
		this.name = DBHelper.sanitizeName(name);
		this.columnType = type;
	}
	
	public DBColumn getCloned(String name) {
		DBColumn col = new DBColumn(name, this.columnType);
		return col;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof DBColumn)) {
			return false;
		}
		
		DBColumn other = (DBColumn) obj;
		return this.name.equals(other.name);
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public String toString() {
		return this.name + "[" + this.columnType + "]";
	}

}