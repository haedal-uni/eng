package com.eng.exception.notfound;

import com.eng.exception.CustomException;
import com.eng.exception.ErrorCode;

public class WordNotFoundException extends CustomException {
    public WordNotFoundException(){
        super(ErrorCode.WORD_NOT_FOUND);
    }
}