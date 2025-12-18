package com.ebanking.assistant.action;

public class ActionExecutionException extends Exception {

  public ActionExecutionException(String message) {
    super(message);
  }

  public ActionExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
