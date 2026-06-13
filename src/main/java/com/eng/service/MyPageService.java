package com.eng.service;

import com.eng.domain.AiFeedback;
import com.eng.domain.QuizHistory;
import com.eng.domain.StudyHistory;
import com.eng.domain.User;
import com.eng.dto.*;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.AiFeedbackRepository;
import com.eng.repository.MyPageRepository;
import com.eng.repository.QuizHistoryRepository;
import com.eng.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

@RequiredArgsConstructor
@Service
public class MyPageService {
    private final MyPageRepository myPageRepository;
    private final UserRepository userRepository;
    private final QuizHistoryRepository quizHistoryRepository;
    private final ChatModel chatModel;
    private final AiFeedbackRepository aiFeedbackRepository;

    // 사용자가 학습하기 or 퀴즈를 눌렀을 때 시간 db 저장
    public void saveStudyTime(MyPageRequestDto myPageRequestDto) {
        String status = myPageRequestDto.getStatus();
        Long time = myPageRequestDto.getTime();
        User user = userRepository.findByUsername(myPageRequestDto.getUsername()).orElseThrow(UserNotFoundException::new);
        LocalDate date = myPageRepository.findLastDay(user.getId());
        LocalDate today = LocalDate.now();
        if (date != null && date.isEqual(today)) {// 오늘 날짜에 저장된 값이 있을 경우
            StudyHistory lastTime = myPageRepository.findByUser_IdAndDate(user.getId(), today);
            if (status.equals("study")) {
                time += lastTime.getStudyTime();
                lastTime.addStudyTime(time);
            } else {
                time += lastTime.getQuizTime();
                lastTime.addQuizTime(time);
            }
            myPageRepository.save(lastTime);
        } else {
            StudyHistory studyHistory;
            if (status.equals("study")) {
                studyHistory = StudyHistory.addStudyHistory(user, today, time, 0L);
            } else {
                studyHistory = StudyHistory.addStudyHistory(user, today, 0L, time);
            }
            myPageRepository.save(studyHistory);
        }
    }

    public List<MyPageResponseDto> get7dayTime(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        LocalDate today = LocalDate.now();
        List<StudyHistory> by7daysTime = myPageRepository.findByUser_IdAndDateBetween(user.getId(), today.minusDays(6), today);
        return by7daysTime.stream().map(x ->
            MyPageResponseDto.of(
                x.getStudyTime(),
                x.getQuizTime(),
                x.getDate()
            )).toList();
    }

    public List<LevelResponseDto> getLevel(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        return myPageRepository.findLevelByUserId(user.getId());
    }

    // 복습 우선 순위
    public List<QuizHistoryResponseDto> getReview(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(UserNotFoundException::new);

        return quizHistoryRepository.getReview(user.getId()).stream()
            .map(QuizHistoryResponseDto::new)
            .collect(Collectors.toList());
    }

    public List<QuizHistoryResponseDto> getRecentWrongQuiz(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(UserNotFoundException::new);

        return quizHistoryRepository.getRecentWrongQuiz(user.getId()).stream()
            .map(QuizHistoryResponseDto::new)
            .collect(Collectors.toList());
    }

    // mypage ai 직접 분석 외 db 조회만 하는 분석들 전부
    public AiFeedbackResponseDto getAiData(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        AiMappingInterface mappingData = quizHistoryRepository.getAiData(user.getId());
        return new AiFeedbackResponseDto(mappingData);
    }

    // ai 피드백 db 조회
    public AiFeedbackResponseDto getAiFeedback(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        AiFeedback feedback = aiFeedbackRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId());
        if(feedback==null){
            return getRefreshFeedback(username);
        }
        return AiFeedbackResponseDto.builder()
            .aiFeedback(feedback.getAiFeedback())
            .errorPattern(feedback.getErrorPattern())
            .build();
    }

    // ai 패드백 새로고침
    public AiFeedbackResponseDto getRefreshFeedback(String username) { // refresh
        try {
            User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
            AiMappingInterface aiData = quizHistoryRepository.getAiData(user.getId());
            List<QuizHistory> wrongSamples = quizHistoryRepository.findTop3ByUserIdAndCorrectFalseOrderByIdDesc(user.getId());

            if (aiData == null) {
                return AiFeedbackResponseDto.builder()
                    .errorPattern("기록 없음")
                    .aiFeedback("아직 풀이 기록이 없습니다.")
                    .build();
            }

            StringBuilder wrongText = new StringBuilder();
            for (QuizHistory q : wrongSamples) {
                wrongText.append(String.format("- 문제: %s (원래정답: %s / 유저오답: %s)\n",
                    q.getQuizSentence(), q.getWord(), q.getUserAnswer()));
            }

            // 2. 메시지 본문(Prompt) 조립
            String instruction = String.format(
                "당신은 전문적인 영어 교육 AI 튜터입니다. 아래 제공된 학습자의 통계와 오답 샘플을 바탕으로 정밀 진단을 내리세요.\n\n" +
                    "[학습자 통계]\n" +
                    "- 어휘 정답률: %.1f%% / 문법 정답률: %.1f%% / 평균 풀이 시간: %.1f초\n\n" +
                    "[최근 오답 샘플]\n" +
                    "%s\n" +
                    "[작성 규칙]\n" +
                    "★중요: 결과는 반드시 '오답패턴 요약 : 피드백 문장' 구조로만 시작해야 합니다. 패턴 요약과 피드백 사이의 ' : ' 기호를 절대 생략하거나 줄바꿈하지 마세요.\n" +
                    "1. '오답패턴 요약'은 가장 두드러지는 핵심 취약점을 10자 이내의 단어로 요약하세요. (예: 품사혼동, 시제오류, 철자오류 등)\n" +
                    "2. '피드백 문장'은 통계 수치와 오답 문장을 융합하여 취약한 부분과 솔루션을 친절하게 HTML 형식으로 작성하세요.\n" +
                    "   - 핵심구문은 <strong> 태그를 사용하세요.\n" +
                    "   - 실천가이드는 시작할 때 줄을 바꾸고 <em style='color:#5A4B81'>→ ...</em> 태그를 조합하세요.\n" +
                    "   - ★[중요] 가독성을 위해 각 문장이 끝나는 온점(.) 뒤에는 문자 '\\n' 대신 반드시 HTML 줄바꿈 태그인 '<br>'을 붙여서 한 줄씩 줄바꿈을 해주세요.\n\n" +
                    "[출력 예시]\n" +
                    "철자오류 : 학습자님의 <strong>어휘 정답률</strong>은 잦은 철자 오류에서 비롯된 것으로 보입니다.<br>단어의 정확한 형태를 기억하는 데 어려움이 있습니다.<br><em style='color:#5A4B81'>→ 연습을 병행해 보세요.</em>",
                aiData.getVocabularyCorrectRate(), aiData.getGrammarCorrectRate(), aiData.getAvgResponseTime(), wrongText.toString()
            );

            ChatResponse response = chatModel.call(
                new Prompt(
                    instruction,
                    GoogleGenAiChatOptions.builder()
                        .model("gemini-2.5-flash")
                        .temperature(0.3)
                        .build()
                )
            );

            String responseText = response.getResult().getOutput().getText().trim();

            String[] parts = responseText.split(" : ", 2);
            String errorPattern = parts.length > 0 ? parts[0] : "분석 완료";
            String aiFeedback = parts.length > 1 ? parts[1] : responseText;

            AiFeedback ai = AiFeedback.builder()
                .user(user)
                .errorPattern(errorPattern)
                .aiFeedback(aiFeedback)
                .build();
            aiFeedbackRepository.save(ai);

            return AiFeedbackResponseDto.builder()
                .errorPattern(errorPattern)
                .aiFeedback(aiFeedback)
                .build();

        } catch (Exception e) {
            e.printStackTrace();
            return AiFeedbackResponseDto.builder()
                .errorPattern("분석 지연")
                .aiFeedback("현재 분석 로딩이 원활하지 않습니다. 잠시 후 <strong>[새로 분석]</strong>을 시도해주세요.")
                .build();
        }
    }
}
