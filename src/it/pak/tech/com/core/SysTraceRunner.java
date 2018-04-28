package it.pak.tech.com.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class SysTraceRunner
  implements Runnable
{
  private final int timeCapturing;
  private final String systraceFilename;
  private final String platformToolsFolder;
  
  
  SysTraceRunner(int timeCapturing, String systraceFilename, String platformToolsFolder)
  {
    this.timeCapturing = timeCapturing;
    this.systraceFilename = systraceFilename;
    this.platformToolsFolder = platformToolsFolder;
  }
  
  private void executeCommand(String command) {
    try {
      List<String> listCommands = new ArrayList();
      
      String[] arrayExplodedCommands = command.split(" ");
      listCommands.addAll(Arrays.asList(arrayExplodedCommands));
      ProcessBuilder pb = new ProcessBuilder(listCommands);
      pb.inheritIO();
      java.lang.Process commandProcess = pb.start();
      commandProcess.waitFor();
      Thread.sleep(3000L);
    } catch (IOException|InterruptedException ex) {
    	System.err.println("Exception Occur");
    	Logger.getLogger(SysTraceRunner.class.getName()).log(Level.SEVERE, null, ex.getMessage());
    }
  }
  
  public void stopProcess() {
	  
  }
  
  public void run()
  {
    String command = "python " + platformToolsFolder + "/systrace/systrace.py --time=" + timeCapturing + " freq idle -o " + systraceFilename;
    
    executeCommand(command);
    
    System.out.println("Systrace Completed");
  }
}
