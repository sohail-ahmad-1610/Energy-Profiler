package it.pak.tech.com.core.traceview ;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.scenario.animation.shared.PulseReceiver;

public class TraceViewParser
{
  public TraceViewParser() {}
  
  public static TraceviewStructure parseFile(String fileName, String filter)
    throws IOException, NumberFormatException
  {
    File file = new File(fileName);
    Date time1 = new Date();
    int firstRowTime = 0;
    int actualRowTime = 0;
    
    Pattern traceViewPattern = Pattern.compile("(\\d*)\\s(\\w{3})\\s*(\\d*)[\\s|-](.*)");
    Pattern processPattern = Pattern.compile("(\\d*)\\smain");
    
    ArrayList<TraceLine> tracelines = new ArrayList();
    BufferedReader readAll = new BufferedReader(new FileReader(file));Throwable localThrowable3 = null;
    try { boolean firstRow = true;
      int processId = 0;
      String readLine; 
      Date time2 = new Date();
      long timespent = time2.getTime() - time1.getTime();
      
      int timeCapturing = (int)((timespent + 10000L) / 1000L);
     // System.out.println("TC: "+timeCapturing);
      while ((readLine = readAll.readLine()) != null) {
        if (processId == 0) {
          Matcher matcher1 = processPattern.matcher(readLine);
          if (matcher1.find()) {
            processId = Integer.parseInt(matcher1.group(1));
            //System.out.println("PID: " + processId);
          }
        }
        Matcher matcher2 = traceViewPattern.matcher(readLine);
       // System.out.println("ReadLine: " + readLine);
        if ((!readLine.contains("methodId")) && 
          (matcher2.find())) {
        	//System.out.println("Group1: " + matcher2.group(1));
        	
        	String s =  matcher2.group(1);
        	String action = "" ;
        	String signature = "" ;
        	boolean toFilter = false;
     
        	if (!(s.equals("")) && !(s.equals("Key"))) {
        	
        		int traceID = Integer.parseInt(matcher2.group(1));
                
        		action = matcher2.group(2);
                actualRowTime = Integer.parseInt(matcher2.group(3));
                
                if (firstRow) {
                  firstRowTime = actualRowTime;
                  firstRow = false;
                }
                
                signature = matcher2.group(4);
               //System.out.println("Sig: " + signature);
                // System.out.println("TID: " + traceID);
                if (traceID == processId) {
                	toFilter = signature.contains(filter);
                	//System.out.println("Filter: " + toFilter);
                }
        	}
          if (toFilter) {
        	  
        	 // System.out.println("Sig: " + signature);
        	  String sig = PurifySignature(signature);
        	  if (!sig.equals("")) {
        		  
        		  if (action.equals("ent")) {
                     
        			  TraceLine tl = new TraceLine();
                      tl.setEntrance(actualRowTime);
                      tl.setSignature(sig);
                      tracelines.add(tl);
                    } else if (action.equals("xit")) {
                      for (int i = 0; i < tracelines.size(); i++) {
                        TraceLine tl = (TraceLine)tracelines.get(i);
                        if ((tl.getSignature().equals(signature)) && (tl.getExit() == 0)) {
                          tl.setExit(actualRowTime);
                          tracelines.set(i, tl);
                        }
                      }
                    }
        	  }
          }
        }
      }
      
      readAll.close();
    }
    catch (Throwable localThrowable1)
    {
      localThrowable3 = localThrowable1;throw localThrowable1;
    }
    finally
    {
      if (readAll != null) if (localThrowable3 != null) try { readAll.close(); } catch (Throwable localThrowable2) { localThrowable3.addSuppressed(localThrowable2); } else readAll.close(); }
    String readLine; int timeLength = actualRowTime - firstRowTime;
    
    return new TraceviewStructure(tracelines, firstRowTime, timeLength);
  }
  
  private static String PurifySignature(String signature) {
	  
	  String retSignature = "";
	  String [] contents = signature.split("\\s");
  	
  	  if ( (!contents[0].contains("$")) && ((!contents[0].endsWith(".<init>"))) ) {
  		
  		  if (contents[0].startsWith(".")) {
  			
  			  Boolean isFound = false ;
  			  int j = 0 ;
  			  for ( ; ((j < contents[0].length()) && (!isFound)) ; j++) {
  				  
  				  if (Character.isLetter(contents[0].charAt(j))) {
  					  isFound = true ;
  				  }
  			  }
  			  
  			  int endIndex = contents[0].length();
  			  //System.out.println("SIG: " + contents[0].substring((j-1), endIndex));
  			  retSignature = contents[0].substring((j-1), endIndex);
  		}
  		else {
  			
  			int endIndex = contents[0].length();
	    	//System.out.println("SIG: " + contents[0].substring(0 , endIndex));
  			retSignature = contents[0].substring(0 , endIndex);
  		}
  	  }
  	  
  	  return retSignature;
  }
}
