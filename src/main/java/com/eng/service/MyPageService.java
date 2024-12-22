package com.eng.service;

import com.eng.domain.StudyHistory;
import com.eng.domain.User;
import com.eng.dto.MyPageRequestDto;
import com.eng.dto.MyPageResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.MyPageRepository;
import com.eng.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MyPageService {
    private final MyPageRepository myPageRepository;
    private final UserRepository userRepository;

    // 사용자가 학습하기 or 퀴즈를 눌렀을 때 시간 db 저장
    public void saveStudyTime(MyPageRequestDto myPageRequestDto){
        String status = myPageRequestDto.getStatus();
        Long time = myPageRequestDto.getTime();
        User user = userRepository.findByUsername(myPageRequestDto.getUsername()).orElseThrow(UserNotFoundException::new);
        LocalDate date = myPageRepository.findLastDay(user.getId());
        LocalDate today = LocalDate.now();
        if(date!=null && date.isEqual(today)){// 오늘 날짜에 저장된 값이 있을 경우
            StudyHistory lastTime = myPageRepository.findLastTime(user.getId(), today);
            if(status.equals("study")){
                time+=lastTime.getStudy_time();
                lastTime.addStudy_time(time);
            }else{
                time+= lastTime.getQuiz_time();
                lastTime.addQuiz_time(time);
            }
            myPageRepository.save(lastTime);
        }else{
            StudyHistory studyHistory;
            if(status.equals("study")){
                studyHistory = StudyHistory.addStudyHistory(user, today, time, 0L);
            }else{
                studyHistory = StudyHistory.addStudyHistory(user, today, 0L, time);
            }
            myPageRepository.save(studyHistory);
        }
    }

    public List<MyPageResponseDto> get7dayTime(String username){
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        LocalDate today = LocalDate.now();
        List<StudyHistory> by7daysTime = myPageRepository.findBy7daysTime(today.minusDays(6), today, user.getId());
        return by7daysTime.stream().map(x->
                MyPageResponseDto.of(
                x.getStudy_time(),
                x.getQuiz_time(),
                x.getDate()
        )).toList();
    }
}
