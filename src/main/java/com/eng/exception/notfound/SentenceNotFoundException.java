package com.eng.exception.notfound;

import com.eng.exception.CustomException;
import com.eng.exception.ErrorCode;

public class SentenceNotFoundException extends CustomException {
    public SentenceNotFoundException() {
        super(ErrorCode.Sentence_NOT_FOUND);
    }
}
