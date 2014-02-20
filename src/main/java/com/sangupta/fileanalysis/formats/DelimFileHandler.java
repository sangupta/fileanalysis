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

import com.sangupta.fileanalysis.formats.base.AbstractDelimitedFileFormatHandler;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;

/**
 * A delimited file handler that allows for custom delimiter
 * specification.
 * 
 * @author sangupta
 *
 */
public class DelimFileHandler extends AbstractDelimitedFileFormatHandler {
	
	private String delim;
	
	@Override
	public void readConfiguration() {
		this.delim = ConsoleUtils.readLine("Enter the file delimiter (default comma): ", true);
		if(AssertUtils.isEmpty(delim)) {
			this.delim = ",";
		}
		
		super.readConfiguration();
	}

	@Override
	protected String getDelimiterString() {
		return delim;
	}

}
