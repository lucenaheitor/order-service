package com.marketplace.orders.exception;

public class UnprocessableEntity extends RuntimeException {

  public UnprocessableEntity(String message) {
    super(message);
  }
}
