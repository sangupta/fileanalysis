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

import com.sangupta.fileanalysis.formats.ApacheLogFileHandler;
import com.sangupta.fileanalysis.formats.CSVFileHandler;
import com.sangupta.fileanalysis.formats.DoNothingHandler;
import com.sangupta.fileanalysis.formats.Log4jFileHandler;
import com.sangupta.fileanalysis.formats.LogbackFileHandler;
import com.sangupta.fileanalysis.formats.TSVFileHandler;

/**
 * Various supported file formats.
 * 
 * @author sangupta
 *
 */
public enum FileFormat {
	
	CSV,
	
	TSV,
	
	ApacheLog,
	
	Log4j,
	
	LogBack;

	public FileFormatHandler getHandler() {
		switch(this) {
			case CSV:
				return new CSVFileHandler();
				
			case ApacheLog:
				return new ApacheLogFileHandler();
				
			case Log4j:
				return new Log4jFileHandler();
				
			case LogBack:
				return new LogbackFileHandler();
				
			case TSV:
				return new TSVFileHandler();
				
			default:
				break;
		}
		
		return new DoNothingHandler();
	}

}
