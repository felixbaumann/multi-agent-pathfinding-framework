/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


/* Class for writing computed common plans to a file.
 * Either in a humanly readable way or in a format requested by simulation
 * software. */
public class FileWriter {
	
	/* Given a common plan and a file. This function will first convert
	 * the action plans of the common plan to positional plans and then write
	 * those into the given file. */
	public void writeCommonPlanToFile(File file, CommonPlan commonPlan)
			throws IOException {		
		
		String outputString = createOutputString(commonPlan);
		

		writeToFile(file, outputString, false);
	
	}

	
	/* Given some positional plans, this function creates a large string
	 * representing those in the way required by the simulation software
	 * visualize.py. */
	private String createOutputString(
			CommonPlan commonPlan) {
		
        String outputString = "schedule:";
		
		for (Plan positionalPlan : commonPlan.commonPlan()) {
			
			String agentName = positionalPlan.agent.name();
			
			if (agentName.equals(null)) {
				
				agentName = Integer.toString(positionalPlan.agent.id());
				}
			
			outputString += ("\n    " + agentName + ":" );
			
			for (TimedPosition timedPosition :
				positionalPlan.plan()) {
				
				outputString += ("\n        - x: " 
				+ Integer.toString(timedPosition.x));
				
				outputString += ("\n          y: " 
				+ Integer.toString(timedPosition.y));
				
				outputString += ("\n          t: " 
				+ Integer.toString(timedPosition.t));
			}
		}
		return outputString;
	}
	
	
	/* Simply write a string to a given file.
	 * Use append=true to append to the file and overwrite the file otherwise.
	 */
	private void writeToFile(File file, String outputString, boolean append)
			throws IOException {
		
		java.io.FileWriter fileWriter = new java.io.FileWriter(file, append);
		PrintWriter writer = new PrintWriter(fileWriter);
		
		writer.println(outputString);
		
		writer.close();
	}


	/* Given an input file and an evaluation object, this function writes
	 * a line with evaluation results to some given evaluation file.
	 * The line is titled with the input file's name. */
    public void writeEvaluationToFile(File inputFile,
    								   File evaluationFile,
    								   Evaluation evaluation,
    								   boolean first)
    								       throws IOException {
    	
    	String outputString = "";
    	
    	if (first) {
    	
    		outputString = "\n;;SZENARIO;;"
    			+ "UNDIRECTED TIME (ns);"
        		+ "DIRECTED TIME (ns);"
        		+ "DYNAMIC TIME (ns);"
        		+ "Dir/Undir time ratio;"
        		+ "Dyn/Undir time ratio;"
        		+ "Dyn/Dir time ratio;;"
        		+ "UNDIRECTED makespan;"
        		+ "DIRECTED makespan;"
        		+ "DYNAMIC makespan;"
        		+ "Dir/Undir makespan ratio;"
        		+ "Dyn/Undir makespan ratio;"
        		+ "Dyn/Dir makespan ratio;;"
        		+ "UNDIRECTED flowtime;"
        		+ "DIRECTED flowtime;"
        		+ "DYNAMIC flowtime;"
        		+ "Dir/Undir flowtime ratio;"
        		+ "Dyn/Undir flowtime ratio;"
        		+ "Dyn/Dir flowtime ratio;"
        		+ "UNDIRECTED service time;"
        		+ "DIRECTED service time;"
        		+ "DYNAMIC service time;"
        		+ "Dir/Undir service time ratio;"
        		+ "Dyn/Undir service time ratio;"
        		+ "Dyn/Dir service time ratio;"
        		+ "\n";  
    	}    	    	
    		
    	outputString += createEvaluationString(inputFile, evaluation);
    	
    	writeToFile(evaluationFile, outputString, !first);
    }
    
    
    private String createEvaluationString(File inputFile,
    									  Evaluation evaluation) {

    	String name = inputFile.getName();
    	
    	/*
    	String timeUndir = evaluation.commonPlanUndirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.planningTimeUndirected());
    	String timeDir = evaluation.commonPlanDirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.planningTimeDirected());
    	String timeDyn = evaluation.commonPlanDynamic() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.planningTimeDynamic());
    	*/
    	String timeUndir = String.valueOf(evaluation.planningTimeUndirected());
    	
    	String timeDir = String.valueOf(evaluation.planningTimeDirected());
    	
    	String timeDyn = String.valueOf(evaluation.planningTimeDynamic());
    	
    	String makespanUndir = evaluation.commonPlanUndirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.makespanUndirected());
    	String makespanDir = evaluation.commonPlanDirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.makespanDirected());
    	String makespanDyn = evaluation.commonPlanDynamic() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.makespanDynamic());
    	
    	String flowtimeUndir = evaluation.commonPlanUndirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.flowtimeUndirected());
    	String flowtimeDir = evaluation.commonPlanDirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.flowtimeDirected());
    	String flowtimeDyn = evaluation.commonPlanDynamic() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.flowtimeDynamic());
    	
    	String serviceTimeUndir = evaluation.commonPlanUndirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.serviceTimeUndirected());
    	String serviceTimeDir = evaluation.commonPlanDirected() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.serviceTimeDirected());
    	String serviceTimeDyn = evaluation.commonPlanDynamic() == null ?
    			"unsuccessful" :
    			String.valueOf(evaluation.serviceTimeDynamic());
    	
    	String timeRatio = "";
    	String makespanRatio = "";    	
    	String flowtimeRatio = "";
    	String serviceTimeRatio = "";
    	
    	return "\n;;" + name + ";;"
    	    + timeUndir + ";" + timeDir + ";" + timeDyn + ";"
    	    + timeRatio + ";" + timeRatio + ";" + timeRatio + ";;"    
    	    + makespanUndir + ";" + makespanDir + ";" + makespanDyn + ";"
    	    + makespanRatio + ";" + makespanRatio + ";" + makespanRatio + ";;"
    	    + flowtimeUndir + ";" + flowtimeDir + ";" + flowtimeDyn + ";"
    	    + flowtimeRatio + ";" + flowtimeRatio + ";" + flowtimeRatio + ";"
    	    + serviceTimeUndir + ";" + serviceTimeDir + ";" + serviceTimeDyn
    	    + ";"
    	    + serviceTimeRatio + ";" + serviceTimeRatio + ";"
    	    + serviceTimeRatio + ";";
    }
}


