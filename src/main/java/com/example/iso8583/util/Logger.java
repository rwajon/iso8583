package com.example.iso8583.util;

public class Logger {
  // private static final Logger logger = LoggerFactory.getLogger("ISO8583Client");
  private final String className;

  public Logger(String name) {
    this.className = name;
  }

  public Logger(Class<?> clazz) {
    this.className = clazz.getName();
  }

  public void error(String message) {
    System.err.println(this.className + ": " + message);
  }

  public void info(String message) {
    System.out.println(this.className + ": " + message);
  }
}
