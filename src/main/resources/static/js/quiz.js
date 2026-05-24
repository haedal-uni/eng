let quizList = []; // 퀴즈 목록
let quizCurrentPage; // 퀴즈 현재 페이지
let chance = 1; // correct=true로 바꿀 수 있는 기회
let quizIdList = localStorage.getItem("quizIdList") ? JSON.parse(localStorage.getItem("quizIdList")) : [];
let quizStartTime; // quiz 각 문제풀이 시간(개별)

// 답 확인 기능 (학습하기)
function checkAnswer() {
  let card = quizList[quizCurrentPage];
  Swal.fire({
    icon: 'info',
    title: `정답: ${card.word}`,
    confirmButtonColor: '#ff6f61'
  });
}

// 답 확인 기능 (빈칸 채우기)
function checkFillBlank() {
  if (!quizList || quizList.length === 0) {
    Swal.fire({icon: 'warning', title: '퀴즈 데이터가 없습니다.', text: '빈칸 채우기를 다시 시작해 주세요.', confirmButtonColor: '#ff6f61'});
    return;
  }
  if (!quizList[quizCurrentPage] || !quizList[quizCurrentPage]["studyResponseDto"]) {
    Swal.fire({icon: 'warning', title: '문제 데이터를 불러오지 못했습니다.', text: '다음 문제로 넘어가 주세요.', confirmButtonColor: '#ff6f61'});
    return;
  }

  let card = quizList[quizCurrentPage]["studyResponseDto"];
  let userAnswer = document.getElementById('userAnswer').value.trim();
  let answerElement = document.querySelector('#fillBlankSentence');
  let showAnswerBtn = document.getElementById('showAnswerBtn');
  let showHint = document.getElementById('hintTrigger');

  if (userAnswer === "" || userAnswer==null){
    Swal.fire({icon: 'warning', title: '답을 입력해주세요.', confirmButtonColor: '#ff6f61'});
    return;
  }

  if (userAnswer.toLowerCase() === card.word.toLowerCase()) {
    answerElement.classList.add('correct');
    answerElement.innerText = card.sentence;

    saveQuizTime(quizList[quizCurrentPage], userAnswer, 1, quizStartTime);
    quizStartTime = Date.now();

    Swal.fire({icon: 'success', title: '정답입니다!', confirmButtonColor: '#ff6f61'})
      .then(() => {
        // 💡 팝업이 확실히 닫힌 후 실행되는 블록입니다.
        if (chance === 1) { // 한번에 정답을 맞췄을 경우(에만 퀴즈 리스트에서 삭제됨)
          if (!quizIdList.includes(quizList[quizCurrentPage]["quizId"])) {
            quizIdList.push(quizList[quizCurrentPage]["quizId"]); // 맞춘 quiz_id 저장
            localStorage.setItem("quizIdList", JSON.stringify(quizIdList)); // localstorage 저장
            quizList.splice(quizCurrentPage, 1);  // quizList에서 정답인 quiz 삭제
            quizCurrentPage -= 1;
          }
        }
        if (quizList.length === 0) {
          // 모달 창 강제 닫기
          let fillBlankModal = bootstrap.Modal.getInstance(document.getElementById('fillBlankModal'));
          if (fillBlankModal) fillBlankModal.hide();
          Swal.fire({icon: 'success', title: '🎉 모두 맞췄습니다!', text: 'quiz를 모두 맞췄습니다.', confirmButtonColor: '#ff6f61'});
        } else {
          nextFillBlank(); // 남은 문제가 있다면 다음 문제로 이동
        }
      });
  }
  else { // 답이 릍렸을 경우
    chance = 0;
    saveQuizTime(quizList[quizCurrentPage], userAnswer, 0, quizStartTime);
    quizStartTime = Date.now();

    answerElement.classList.add('incorrect');
    Swal.fire({icon: 'error', title: '틀렸습니다.', confirmButtonColor: '#ff6f61'});
    showAnswerBtn.style.display = 'inline-block'; // 틀렸을 경우 정답 버튼 보이기
    showHint.style.display = 'inline-block';
    const x = card.meaning.length * 28;
    $("#hintPopup").text(card.meaning).css('width', x + 'px');
  }


}

function fillBlankAnswer() {
  if (!quizList[quizCurrentPage] || !quizList[quizCurrentPage]["studyResponseDto"]) return;
  let card = quizList[quizCurrentPage]["studyResponseDto"];
  document.querySelector('#fillBlankSentence').innerText = card.sentence; // 빈칸에 정답 채우기
}

// 빈칸 채우기 모달에서 다음 문제로 이동 시에도 같은 처리
function nextFillBlank() {
  quizStartTime = Date.now()
  let showAnswerBtn = document.getElementById('showAnswerBtn');
  let showHint = document.getElementById('hintTrigger');
  showAnswerBtn.style.display = 'none';
  showHint.style.display = 'none';

  // quizList가 비어있으면 새로 가져오기
  if (quizList.length < 1) {
    getRandomQuiz();
    return;
  }

  quizCurrentPage = (quizCurrentPage + 1) % quizList.length;
  showQuiz();
}

// 모달을 유지한 상태에서 빈칸 채우기의 내용을 업데이트
function showQuiz() {
  if (!quizList[quizCurrentPage] || !quizList[quizCurrentPage]["studyResponseDto"]) return;

  chance = 1;
  let card = quizList[quizCurrentPage]["studyResponseDto"];
  let s_word;
  if (card.sentence.indexOf(card.word) !== -1) {
    s_word = card.word;
  } else {
    s_word = similarWord(card, card.word);
  }
  document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(s_word, `_____`);
  document.getElementById('fillBlankMeaning').innerText = card.sentence_meaning;
  document.getElementById('userAnswer').value = "";
}

function similarWord(card, word) {
  let arr = card.sentence.split(" ");
  let similar;
  let min = 999;
  for (let i = 0; i < arr.length; i++) {
    let x = getLevenshteinDistance(word, arr[i]);
    if (x < min) {
      min = x;
      similar = arr[i];
    }
  }
  return similar;
}

function getLevenshteinDistance(a, b) {
  const table = Array.from({length: a.length + 1}, () => Array(b.length + 1).fill(0));
  for (let i = 1; i <= a.length; i++) table[i][0] = i;
  for (let j = 1; j <= b.length; j++) table[0][j] = j;
  for (let i = 1; i <= a.length; i++) {
    for (let j = 1; j <= b.length; j++) {
      table[i][j] = Math.min(
        table[i - 1][j] + 1,
        table[i][j - 1] + 1,
        table[i - 1][j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1)
      );
    }
  }
  return table[a.length][b.length];
}

// 엔터키로 답 확인 및 알림창 제어
document.addEventListener('keydown', function (event) {
  if (event.key === 'Enter') {

    // 1. 만약 현재 화면에 SweetAlert(알림창)이 활성화되어 있다면
    if (Swal.isVisible()) {
      event.preventDefault();
      event.stopPropagation();

      // SweetAlert의 [확인] 버튼을 프로그램적으로 클릭하여 알림창만 닫히게 함
      Swal.getConfirmButton().click();
      return;
    }

    // 알림창이 없는 평소 상태일 때만 정답 체크 함수를 실행
    if (document.getElementById('fillBlankModal').classList.contains('show')) {
      event.preventDefault();
      checkFillBlank();
    }
  }
});

function getRandomQuiz() {
  startTime = Date.now();
  quizStartTime = Date.now();
  $.ajax({
    type: "GET",
    url: `/quiz/${username}`,
    contentType: false,
    processData: false,
    success: function (response) {
      if (response.length === 0) {
        Swal.fire({icon: 'success', title: '🎉 모두 맞췄습니다!', text: 'quiz를 모두 맞췄습니다.', confirmButtonColor: '#ff6f61'});
      } else {
        quizCurrentPage = 0;
        quizList = quizList.concat(response);
        console.log(" [quiz list] : ", quizList)
        showQuiz();
        let fillBlankModal = new bootstrap.Modal(document.getElementById('fillBlankModal'));
        fillBlankModal.show();
      }
    }
  });
}

function changeCorrect() {
  $.ajax({
    type: "PUT",
    url: `/quiz/${username}`,
    data: JSON.stringify({quizIdList}),
    contentType: 'application/json',
    processData: false,
    success: function () {
      quizIdList = []; // quiz_id 삭제
      localStorage.removeItem("quizIdList");
    }
  });
}

const quizModal = document.getElementById("fillBlankModal");
quizModal.addEventListener("hidden.bs.modal", () => {
  saveTime(startTime, Date.now(), "quiz"); // 문제 풀이 시간 저장
  document.activeElement?.blur();
  if (quizIdList.length > 0) {
    changeCorrect();
  }
});

// 개별 문제 풀이 시간 측정
function saveQuizTime(quizHistory, userAnswer, correct, quizStartTime) {
  let time = ((Date.now() - quizStartTime) / 1000).toFixed(1);
  let answerElement = document.querySelector('#fillBlankSentence');
    console.log(quizHistory["studyResponseDto"]["quiz_type"]);
    console.log(quizHistory["studyResponseDto"]["word"]);

  let data = {
    "quizId" : quizHistory['quizId'],
    "username": username,
    "quizType" : quizHistory["studyResponseDto"]["quiz_type"],
    "correct" : (correct === 1),
    "userAnswer" : userAnswer,
    "word" : quizHistory["studyResponseDto"]['word'],
    "quizSentence" : answerElement.innerText,
    "responseTime": time
  };

  $.ajax({
    type: "POST",
    url: `/quiz/history/${username}`,
    data: JSON.stringify(data),
    contentType: 'application/json',
    processData: false,
  });
}
