/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException,
	CorruptedFileException, InvalidPlanException, DistanceTableException {				
		
		Parameters parameters = new ParameterReader().readParameters(args);

		/* Used to read the scenario file(s). */
		FileReader fileReader = new FileReader(parameters);
		
		FileWriter fileWriter = new FileWriter();
		
		boolean writePlansToFile = false;
		
		/* If at least one file will be processed, create an evaluation file
		 * that stores runtime, makespan and flowtime for all processed
		 * scenarios.
		 * Distinguishes between the directed, the undirected and the dynamic
		 * graph.*/
		if (fileReader.fileCount() == 0) { return; }
		
		String outputPath = fileReader.files[0].getParent()
				+ "_evaluation.csv";
		
		File evaluationFile = new File(outputPath);	
		
		/* Create a directed and undirected scenario from each file, run
		 * the chosen MAPF algorithm, store the resulting plans in the same
		 * directory, evaluate the results and write those to an evaluation
		 * file. */
		for (int file = 0; file < fileReader.fileCount(); file++) {
			
			Agent.resetIdTracker();
			
			Scenario directedScenario = fileReader.processFile(file);
				        	        
	        /* This will run the given scenario according to the parameters.
	         * The evaluation object then contains quality measures of the
	         * solution. */
	        Evaluation evaluation = new Evaluation(directedScenario,
	        		                               parameters);
	        	        	       
	        /* -------------------- DIRECTED GRAPH -------------------- */
	        
	        System.out.print("\n\n\n\n\n\nPlanning for a scenario with "
	            + directedScenario.agents().length
	            + " agents. This is scenario " + String.valueOf(file + 1)
	            + " of " + String.valueOf(fileReader.fileCount()) + ".\n");	        	        
	        
	        /* Pathfinding process may be unsuccessful and the common plan
	         * returned be null. */
	        if (evaluation.commonPlanDirected() != null) {
	        	
	        	System.out.print("\nComputation time on the directed graph" +
	        	    " in nanoseconds: "
	        		+ formatNumber(evaluation.planningTimeDirected()) + "\n");
	        
	        	System.out.print("\nThe common plan on the directed graph" +
	        	    " has a length (makespan) of " + 
	        		evaluation.makespanDirected() + "\n");
	        	
	        	System.out.print("\nThe common plan on the directed graph" +
		        	" has a flowtime (sum of costs) of " + 
		        	evaluation.flowtimeDirected() + "\n");						        	
	        	
	        	System.out.print("\nThe common plan on the directed graph" +
			        	" has a service time of " + 
			        	evaluation.serviceTimeDirected() + "\n");	
	        		        	
	        	String fileName = fileReader.files[file].getName()
	        			+ "_result_directed.txt";
	        	
	        	File outputFile = new File(parameters.outputPath().toFile(),
	        			                   fileName);
	        	
	        	if (writePlansToFile) {
	        	
	        		fileWriter.writeCommonPlanToFile(outputFile, 
	    	        		evaluation.commonPlanDirected());
	        	}
	        	 	
	        }	        
	        else {
	        	
	        	System.out.print("\nPlanning on the directed graph" +
	        	                 " was unsuccessful.\n\n");
	        }	        	        
	        
	        /* -------------------- UNDIRECTED GRAPH -------------------- */
	        
	        /* Pathfinding process may be unsuccessful and the common plan
	         * returned be null. */
	        if (evaluation.commonPlanUndirected() != null) {
	        	
	        	System.out.print("\n\nComputation time on the undirected" +
	        	    " graph in nanoseconds: "
	        		+ formatNumber(evaluation.planningTimeUndirected())
	        		+ "\n");
	        
	        	System.out.print("\nThe common plan on the undirected" +
	        	" graph has a length (makespan) of "
	        	+ evaluation.makespanUndirected() + "\n");
	        	
	        	System.out.print("\nThe common plan on the undirected graph" +
			        " has a flowtime (sum of costs) of " + 
			        evaluation.flowtimeUndirected() + "\n");		        	
	        	
	        	System.out.print("\nThe common plan on the undirected graph" +
				        " has a service time of " + 
				        evaluation.serviceTimeUndirected() + "\n");	
	        	
	        	String fileName = fileReader.files[file].getName()
	        			+ "_result_undirected.txt";
	        	
	        	File outputFile = new File(parameters.outputPath().toFile(),
	        			                   fileName);
	        	
	        	if (writePlansToFile) {
	        		
	        		fileWriter.writeCommonPlanToFile(outputFile, 
	        				evaluation.commonPlanUndirected());
	        	}
	        }	        
	        else {
	        	
	        	System.out.print("\n\nPlanning on the undirected graph" +
	        	                 " was unsuccessful.\n\n");
	        }			        	        
	        
	        /* -------------------- DYNAMIC GRAPH -------------------- */
	        
	        /* Pathfinding process may be unsuccessful and the common plan
	         * returned be null. */
	        if (evaluation.commonPlanDynamic() != null) {
	        	
	        	System.out.print("\n\nComputation time on the dynamic" +
	        	    " graph in nanoseconds: "
	        		+ formatNumber(evaluation.planningTimeDynamic()) + "\n");
	        
	        	System.out.print("\nThe common plan on the dynamic" +
	        	" graph has a length (makespan) of "
	        	+ evaluation.makespanDynamic() + "\n");
	        	
	        	System.out.print("\nThe common plan on the dynamic graph" +
			        " has a flowtime (sum of costs) of " + 
			        evaluation.flowtimeDynamic() + "\n");		        	
	        	
	        	System.out.print("\nThe common plan on the dynamic graph" +
				        " has a service time of " + 
				        evaluation.serviceTimeDynamic() + "\n");
	        	
	        	String fileName = fileReader.files[file].getName()
	        			+ "_result_dynamic.txt";
	        	
	        	File outputFile = new File(parameters.outputPath().toFile(),
	        			                   fileName);
	        	
	        	if (writePlansToFile) {
	        		
	        		fileWriter.writeCommonPlanToFile(outputFile, 
	        				evaluation.commonPlanDynamic());
	        	}
	        }	        
	        else {
	        	
	        	System.out.print("\n\nPlanning on the dynamic graph" +
	        	                 " was unsuccessful.\n\n");
	        }	     
			
			fileWriter.writeEvaluationToFile(fileReader.files[file],
                    						 evaluationFile,
                    						 evaluation,
                    						 file == 0);
		}
	}
	
	
	/* Formats the given number such that there's a space between each block
	 * of three digits. */
	private static String formatNumber(long number) {
		
		ArrayList<String> blocks = new ArrayList<String>();
				
		while (number > 1000) {
		
			String padding = "000";
			
			String value = String.valueOf(number % 1000);
			
			blocks.add(0, (padding.substring(0, 3 - value.length())) + value);
						
			number = number / 1000;
		}
		
		blocks.add(0, String.valueOf(number));
				
		String result = "";
		
		for (String block: blocks) { result += " " + block; }
		
		return result;
	}
}
