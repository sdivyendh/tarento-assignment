package com.fulfilment.application.monolith.fulfilments;

final class FulfilmentAssignmentException extends RuntimeException {

  final int status;

  private FulfilmentAssignmentException(int status, String message) {
    super(message);
    this.status = status;
  }

  static FulfilmentAssignmentException badRequest(String message) {
    return new FulfilmentAssignmentException(400, message);
  }

  static FulfilmentAssignmentException notFound(String message) {
    return new FulfilmentAssignmentException(404, message);
  }

  static FulfilmentAssignmentException conflict(String message) {
    return new FulfilmentAssignmentException(409, message);
  }
}
