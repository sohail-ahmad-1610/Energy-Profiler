package it.pak.tech.com.core.exceptions;

import java.io.PrintStream;

public class NumberOfTrialsExceededException extends Exception
{
  public NumberOfTrialsExceededException()
  {
    super("error: too many trials performed!");
    System.out.println("error: too many trials performed!");
  }
}
