package it.pak.tech.com.core.exceptions;

import java.io.PrintStream;

public class ADBNotFoundException extends Exception
{
  public ADBNotFoundException()
  {
    super("error: adb not found!");
    System.out.println("error: adb not found!");
  }
}
