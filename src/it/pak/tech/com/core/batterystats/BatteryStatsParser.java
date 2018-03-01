package it.pak.tech.com.core.batterystats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatteryStatsParser
{
  public BatteryStatsParser() {}
  
  public static ArrayList<EnergyInfo> parseFile(String fileName, int traceviewStart)
    throws IOException
  {
    EnergyInfo previousEnergyInfo = null;
    ArrayList<EnergyInfo> energyInfoArray = new ArrayList();
    File file = new File(fileName);
    

    BufferedReader readAll = new BufferedReader(new FileReader(file));Throwable localThrowable3 = null;
    try {
      Pattern energyRowPattern = Pattern.compile("\\s*(\\d|.\\d*m*\\d*s*\\d*ms*)\\s.\\d.\\s\\d{3}\\s(.*)");
      Pattern phoneSignalPattern = Pattern.compile("phone_signal_strength=(\\d)");
      String line;
      while ((line = readAll.readLine()) != null) {
        Matcher matcher1 = energyRowPattern.matcher(line);
        

        if (matcher1.find()) {
          int variationTime = toMillisec(matcher1.group(1));
         // System.out.println("vt"+variationTime);
          int realTime = variationTime + traceviewStart;
          EnergyInfo energyInfo;
         // EnergyInfo energyInfo;
          if (previousEnergyInfo != null) {
            energyInfo = new EnergyInfo(previousEnergyInfo);
          } else {
            energyInfo = new EnergyInfo();
          }
          
          energyInfo.setEntrance(realTime);
          if (energyInfoArray.size() > 0) {
            ((EnergyInfo)energyInfoArray.get(energyInfoArray.size() - 1)).setExit(realTime);
          }
          
          String devices = matcher1.group(2).split(":")[0];
          String[] devicesArray = devices.split(" ");
          for (String device : devicesArray) {
            if (device.contains("volt=")) {
              energyInfo.setVoltage(Integer.parseInt(device.replaceFirst("volt=", "")));
            }
            if (device.contains("-")) {
              String deviceDeactivated = device.replaceFirst("-", "");
              energyInfo.removeDevice(deviceDeactivated);
            }
            if ((device.contains("+")) && 
              (!device.replaceFirst("\\+", "").startsWith("top"))) {
              energyInfo.addDevice(device.replaceFirst("\\+", ""));
            }
            
            Matcher phoneSignalMatcher = phoneSignalPattern.matcher(device);
            if (phoneSignalMatcher.find()) {
              energyInfo.setPhoneSignalStrength(Integer.parseInt(phoneSignalMatcher.group(1)));
            }
          }
          
          energyInfoArray.add(energyInfo);
          
          if ((previousEnergyInfo != null) && (energyInfo.getEntrance() == previousEnergyInfo.getEntrance())) {
            energyInfoArray.remove(previousEnergyInfo);
          }
          
          previousEnergyInfo = energyInfo;
        }
      }
    }
    catch (Throwable localThrowable1)
    {
      localThrowable3 = localThrowable1;throw localThrowable1;


























    }
    finally
    {

























      if (readAll != null) if (localThrowable3 != null) try { readAll.close(); } catch (Throwable localThrowable2) { localThrowable3.addSuppressed(localThrowable2); } else readAll.close(); }
    String line;
    ((EnergyInfo)energyInfoArray.get(energyInfoArray.size() - 1)).setExit(Integer.MAX_VALUE);
    
    return energyInfoArray;
  }
  
  private static int toMillisec(String time) {
    int h = 0;
    int m = 0;
    int s = 0;
    int ms = 0;
    Pattern pattern1 = Pattern.compile("(\\d)h(\\d{2})m(\\d{2})s(\\d{3})ms");
    Pattern pattern2 = Pattern.compile("(\\d*)m(\\d{2})s(\\d{3})ms");
    Pattern pattern3 = Pattern.compile("(\\d*)s(\\d{3})ms");
    Pattern pattern4 = Pattern.compile("(\\d*)ms");
    Matcher matcher1 = pattern1.matcher(time);
    Matcher matcher2 = pattern2.matcher(time);
    Matcher matcher3 = pattern3.matcher(time);
    Matcher matcher4 = pattern4.matcher(time);
    if (matcher1.find()) {
      h = Integer.parseInt(matcher1.group(1));
      m = Integer.parseInt(matcher1.group(2));
      s = Integer.parseInt(matcher1.group(3));
      ms = Integer.parseInt(matcher1.group(4));
    } else if (matcher2.find()) {
      m = Integer.parseInt(matcher2.group(1));
      s = Integer.parseInt(matcher2.group(2));
      ms = Integer.parseInt(matcher2.group(3));
    } else if (matcher3.find()) {
      s = Integer.parseInt(matcher3.group(1));
      ms = Integer.parseInt(matcher3.group(2));
    } else if (matcher4.find()) {
      ms = Integer.parseInt(matcher4.group(1));
    }
    if (s != 0) {
      s *= 1000;
    }
    if (m != 0) {
      m *= 60000;
    }
    if (h != 0) {
      h *= 3600000;
    }
    return h + m + s + ms;
  }
}
