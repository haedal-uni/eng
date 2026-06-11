package com.eng.exception.notfound;

import com.eng.exception.CustomException;
import com.eng.exception.ErrorCode;

public class QuizNotFoundException extends CustomException {
  public QuizNotFoundException() {
    super(ErrorCode.QUIZ_NOT_FOUND);
  }
}
