package com.eng.exception.notfound;


import com.eng.exception.CustomException;
import com.eng.exception.ErrorCode;

public class StudyNotFoundException extends CustomException {
    public StudyNotFoundException(){
        super(ErrorCode.STUDY_NOT_FOUND);
    }
}

