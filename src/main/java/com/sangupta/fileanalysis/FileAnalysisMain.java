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

package com.sangupta.fileanalysis;

import java.io.File;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ConsoleUtils;

/**
 * Command line interface to the {@link FileAnalysis} tool.
 * 
 * @author sangupta
 *
 */
public class FileAnalysisMain {
	
	/**
	 * Start the interactive tool.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		File file = getInputFile();
		if(file == null) {
			return;
		}
		
		FileFormat format = getFileFormat(file);
		if(format == null) {
			System.out.println("File format not understood by the tool... exiting!");
			return;
		}
		
		// check the validity of the parameters
		System.out.println("Checking parameters...");

		// start the engine
		FileAnalysis analysis = null;
		try {
			analysis = new FileAnalysis(file, format);
			analysis.analyzeFile();
		} finally {
			analysis.close();
		}
	}

	/**
	 * Find the format of the file that we will be working upon, either
	 * by guessing, or by asking the user.
	 * 
	 * @param file
	 * @return
	 */
	private static FileFormat getFileFormat(File file) {
		// extract file extension
		String name = file.getName();
		int index = name.indexOf('.');
		if(index != -1) {
			String extension = name.substring(index + 1);
			
			// now compare this extension with valid files
			if("csv".equalsIgnoreCase(extension)) {
				return FileFormat.CSV;
			}
			
			if("tsv".equalsIgnoreCase(extension)) {
				return FileFormat.TSV;
			}
			
			if("pipe".equalsIgnoreCase(extension)) {
				return FileFormat.Pipe;
			}
			
			if("log4j".equalsIgnoreCase(extension)) {
				return FileFormat.Log4j;
			}
			
			if("logback".equalsIgnoreCase(extension)) {
				return FileFormat.LogBack;
			}
			
			System.out.println("Unable to detect file format from extension: " + name);
		}
		
		
		System.out.println("Valid file formats: csv, tsv, pipe, delim, httpd, log4j, logback");
		String fileFormat = ConsoleUtils.readLine("Enter file format: ", true);
		if(AssertUtils.isEmpty(fileFormat)) {
			System.out.println("No file format specified... exiting!");
			return null;
		}
		
		return FileFormat.valueOf(fileFormat);
	}

	/**
	 * Ask the user for file that we need to work upon.
	 * 
	 * @return
	 */
	private static File getInputFile() {
		String filePath = ConsoleUtils.readLine("Enter file path to be analyzed: ", true);
		if(AssertUtils.isEmpty(filePath)) {
			System.out.println("Nothing to do... exiting!");
			return null;
		}
		
		File file = new File(filePath);
		if(!file.exists()) {
			System.out.println("No such file exists... exiting!");
			return null;
		}
		
		if(!file.isFile()) {
			System.out.println("Not a valid file... exiting!");
			return null;
		}
		
		return file;
	}
	
}