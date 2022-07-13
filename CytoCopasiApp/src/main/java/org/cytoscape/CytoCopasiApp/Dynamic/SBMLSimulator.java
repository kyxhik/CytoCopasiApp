package org.cytoscape.CytoCopasiApp.Dynamic;

import java.io.*;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.CytoCopasiApp.CyActivator;
import org.cytoscape.CytoCopasiApp.Report.ParsingReportGenerator;

public class SBMLSimulator {

	File sbmlSimulatorJarFile;
	private Thread executeCommandThread;
	private String command;

	public SBMLSimulator() {
		try {
			sbmlSimulatorJarFile = CyActivator.getSBMLSimulatorJar();
		} catch (FileNotFoundException e) {
			sbmlSimulatorJarFile = null;
		}
	}
	
	public void TimeCourseDynamicSim(File sbmlFile, Double duration, Double stepSize, File csvFile, TaskMonitor taskMonitor) {
		if(sbmlSimulatorJarFile==null || !sbmlSimulatorJarFile.exists()) {
			ParsingReportGenerator.getInstance().appendLine("SBMLsimulator jar file could not be found");
		}
		
		
		command = String.format("java -jar %s --gui --sbml-input-file=%s --time-series-file=%s --ode-solver=%s --sim-start-time=%f --sim-end-time=%f --sim-step-size=%f", 
				sbmlSimulatorJarFile.getAbsolutePath(), sbmlFile.getAbsolutePath(), csvFile.getAbsolutePath(),
				"org.simulator.math.odes.RosenbrockSolver",0.0,duration,stepSize);
		ExecuteCommandTask executeCommandTask = new ExecuteCommandTask(command);
		executeCommandThread = new Thread(executeCommandTask);
		
		 int maxTime = 50000;
	        long initTime = System.currentTimeMillis();
	        long maxExecutionTime = initTime + maxTime;
	        executeCommandThread.start();
	        while (executeCommandThread.isAlive()) {
	            if (System.currentTimeMillis() > maxExecutionTime) {
	                String message = "The converter took more than "
	                        + maxTime / 1000 + " s to execute. ";
	                ParsingReportGenerator.getInstance().appendLine(message);
	                taskMonitor.setStatusMessage(message);
	              //  executeCommandThread.interrupt();
	              //  destroyProcess(executeCommandTask);
	               
	                break;
	            }

	            try {
	                Thread.yield();
	                Thread.sleep(50000);
	            } catch (InterruptedException t) {
	                t.printStackTrace();
	            }
	        }
	if (executeCommandThread.isAlive()) {
        executeCommandThread.interrupt();
        destroyProcess(executeCommandTask);
       
    }
//    File outFile = new File(outFilePath);
//    success = outFile.exists() && outFile.length() >0 ;

    return;
	}


private void destroyProcess(ExecuteCommandTask executeCommandTask) {
    executeCommandTask.destroyProcess();
}

private class ExecuteCommandTask implements Runnable {
    private String command;
    private Process process = null;
    private InputStream inputStream;
    private InputStream errorStream;
    private BufferedReader reader;

    ExecuteCommandTask(String command) {
        this.command = command;
    }

    @Override
    public void run() {

        Runtime runtime = Runtime.getRuntime();
        try {
            process = runtime.exec(command);
        } catch (IOException e) {
            ParsingReportGenerator.getInstance().appendLine(e.getMessage());
        }

        try {
            inputStream = process.getInputStream();
            if (inputStream != null && inputStream.available() > 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                if (reader.ready())
                    while ((line = reader.readLine()) != null) {
                        ParsingReportGenerator.getInstance().appendLine(line);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            errorStream = process.getErrorStream();
            if (errorStream != null) {
                String line = "";
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                if (errorReader.ready())
                    while ((line = errorReader.readLine()) != null) {
                        ParsingReportGenerator.getInstance().appendLine(line);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void destroyProcess() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (errorStream != null)
                errorStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (process != null)
            process.destroy();
    }


}

}

