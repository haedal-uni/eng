package com.eng.exception.notfound;

import com.eng.exception.CustomException;
import com.eng.exception.ErrorCode;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(){
        super(ErrorCode.USER_NOT_FOUND);
    }
}