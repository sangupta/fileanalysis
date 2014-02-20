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

import java.io.File;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;

/**
 * 
 * @author sangupta
 *
 */
public class FileAnalysisMain {
	
	public static void main(String[] args) {
		String filePath = ConsoleUtils.readLine("Enter file path to be analyzed: ", true);
		if(AssertUtils.isEmpty(filePath)) {
			System.out.println("Nothing to do... exiting!");
			return;
		}
		
		File file = getInputFile(filePath);
		if(file == null) {
			return;
		}
		
		System.out.println("Valid file formats: csv, log4j, logback");
		String fileFormat = ConsoleUtils.readLine("Enter file format: ", true);
		if(AssertUtils.isEmpty(fileFormat)) {
			System.out.println("No file format specified... exiting!");
			return;
		}
		
		FileFormat format = getFileFormat(fileFormat, file);

		// check the validity of the parameters
		System.out.println("Checking parameters...");

		// start the engine
		FileAnalysis analysis = new FileAnalysis(file, format);
		analysis.analyzeFile();
	}

	/**
	 * Find the format of the file that we will be working upon.
	 * 
	 * @param fileFormat
	 * @param file
	 * @return
	 */
	private static FileFormat getFileFormat(String fileFormat, File file) {
		return FileFormat.LogBack;
	}

	/**
	 * Extract the filename that we need to work upon.
	 * 
	 * @param filePath
	 * @return
	 */
	private static File getInputFile(String filePath) {
		return new File("c:/users/sangupta/desktop/jobsearch.log");
	}
	
}
