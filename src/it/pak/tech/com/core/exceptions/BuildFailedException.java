package it.pak.tech.com.core.exceptions;

public class BuildFailedException extends Exception
{
	  public BuildFailedException()
	  {
	    super("BUILD FAILED");
	    System.out.println("BUILD FAILED EXCEPTION");
	  }
	}