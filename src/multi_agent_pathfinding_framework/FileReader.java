/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.yaml.snakeyaml.Yaml;

/* Class responsible for reading scenario files. */
public class FileReader {

	private Path inputPath;
	
	public File files[];
	
		
	/* A FileReader accepts parameters including a path to a file or 
	 * and stores a reference to this file or all the files in case
	 * of a directory. Subdirectories however are ignored.
	 * 
	 * Afterwards those files can be read and processed by
	 * the processFile() function.
	 * Use fileCount() to check how many files there are that can be processed
	 * in the first place.
	 */
	public FileReader(Parameters parameters) throws IOException {
		
		this.inputPath = parameters.inputPath();
		
		if (!Files.exists(inputPath)) {
			throw new IOException("File or directory of given input path do "
					+ "not exist.");
		}
		
		/* There's just a single input file. Store it in the files array. */
		else if (Files.isRegularFile(inputPath)) {
			
			files = new File[] { inputPath.toFile() };
		}		
		
		/* There's a whole directory, posssibly containing multiple input
		 * files. Store all those in the files array but skip the
		 * subdirectories. */
		else if (Files.isDirectory(inputPath)) {
			
			files = inputPath.toFile().listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {

					return Files.isRegularFile(pathname.toPath());
				}});			
		}
	}
		

	public int fileCount() { return files.length; }	
	
	
	/* Read a single file given by the index in the files field and use it
	 * to create a standard scenario. */
	public Scenario processFile(int index) throws IOException,
	    CorruptedFileException {
		
		File file = files[index];
		
		/* Read the first line of the file to infer the structure type
		 * of the file. */
		YamlFileType type = getYamlFileType(file);
		
        /* Prepare the file import. */			
		Yaml yaml = new Yaml();
		
		InputStream inputStream = new FileInputStream(file.toString());
	
		/* Interpret the file's content according to its type. */
		if (type == YamlFileType.CLASSIC) {
		
			YamlClassicScenario yamlClassicScenario = yaml.load(inputStream);
			
			inputStream.close();
			
			return new Scenario(yamlClassicScenario);
			
		} else if (type == YamlFileType.DYNAMIC) {
			
			YamlDynamicScenario yamlDynamicScenario = yaml.load(inputStream);
			
			inputStream.close();
			
			return new Scenario(yamlDynamicScenario);
		}
		
		inputStream.close();
	
		return null;
		
	}	
	
	
	/* This function reads the first line of a given file and tries to match
	 * it to a yaml file type. */
	private YamlFileType getYamlFileType(File file) throws IOException,
	CorruptedFileException {
		
		BufferedReader reader
		    = new BufferedReader(new java.io.FileReader(file));
		
		String header = reader.readLine();
		
		reader.close();
		
		if (header == null) { throw new CorruptedFileException("Input file is empty.");}
		
		else if (header.equals(
				"!!multi_agent_pathfinding_framework.YamlClassicScenario")) {
			
			return YamlFileType.CLASSIC;
		}
		
		else if (header.equals(
				"!!multi_agent_pathfinding_framework.YamlDynamicScenario")) {
			
			return YamlFileType.DYNAMIC;
		}
		
		throw new CorruptedFileException("The first line of the file "
		+ file.toString() + " does not match a yaml file type.");
	}
}
