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

package com.sangupta.fileanalysis.formats.base;

import java.io.File;

import com.sangupta.fileanalysis.FileFormatHandler;
import com.sangupta.fileanalysis.db.Database;

/**
 * A simple {@link FileFormatHandler} that does nothing, absolutely
 * nothing.
 * 
 * @author sangupta
 *
 */
public class DoNothingHandler implements FileFormatHandler {

	@Override
	public void initialize(Database database, File file) {
		// do nothing
	}

	@Override
	public void readConfiguration() {
		// do nothing
	}

	@Override
	public void createDBTables() {
		// do nothing
	}

	@Override
	public void loadFile() {
		// do nothing
	}

}
