package it.pak.tech.com.core.exceptions;

import java.io.PrintStream;

public class ApkNotFoundException extends Exception
{
  public ApkNotFoundException()
  {
    super("error: apk not found!");
    System.out.println("error: apk not found!");
  }
}
