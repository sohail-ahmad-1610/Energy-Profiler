package it.pak.tech.com.core.exceptions;

import java.io.PrintStream;

public class AppNameCannotBeExtractedException extends Exception
{
  public AppNameCannotBeExtractedException()
  {
    super("error: app name cannot be extract!");
    System.out.println("error: app name cannot be extract!");
  }
}
