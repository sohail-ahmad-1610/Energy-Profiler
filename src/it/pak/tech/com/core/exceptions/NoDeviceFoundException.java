package it.pak.tech.com.core.exceptions;

import java.io.PrintStream;

public class NoDeviceFoundException extends Exception
{
  public NoDeviceFoundException()
  {
    super("error: no device/emulator found!");
    System.out.println("error: no device/emulator found!");
  }
}
