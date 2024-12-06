let quizList;
let quizCurrentPage

// 답 확인 기능 (학습하기)
function checkAnswer() {
    let card = quizList[quizCurrentPage];
    alert(`정답: ${card.word}`);
}

// 답 확인 기능 (빈칸 채우기)
function checkFillBlank() {
    let card = quizList[quizCurrentPage];
    let userAnswer = document.getElementById('userAnswer').value.trim();
    let answerElement = document.querySelector('#fillBlankSentence strong');
    let showAnswerBtn = document.getElementById('showAnswerBtn'); // 정답 버튼

    if (userAnswer.toLowerCase() === card.word.toLowerCase()) {
        answerElement.classList.add('correct');
        answerElement.innerText = card.word;
        alert("정답입니다!");
    } else {
        answerElement.classList.add('incorrect');
        alert("틀렸습니다.");
        showAnswerBtn.style.display = 'inline-block'; // 틀렸을 경우 정답 버튼 보이기
    }
}

function fillBlankAnswer(){
    let card = quizList[quizCurrentPage];
    let answerElement = document.querySelector('#fillBlankSentence strong');
    answerElement.innerText = card.word; // 빈칸에 정답 채우기
}

// 빈칸 채우기 모달에서 다음 문제로 이동 시에도 같은 처리
function nextFillBlank() {
    quizCurrentPage = (quizCurrentPage + 1) % quizList.length;

    // 모달을 유지한 상태에서 빈칸 채우기의 내용을 업데이트
    let card = quiz[quizCurrentPage];
    document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(card.word, '<strong>__</strong>');
    document.getElementById('fillBlankMeaning').innerText = card.meaning;
    document.getElementById('userAnswer').value = ""; // 입력 필드 초기화
}

// 엔터키로 답 확인 기능 추가
document.addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        if (document.getElementById('fillBlankModal').classList.contains('show')) {
            checkFillBlank();
        }
    }
});

function getRandomQuiz(){
    $.ajax({
        type: "GET",
        url: `/quiz/${username}`,
        headers: {},
        data: {},
        contentType: false,
        processData: false,
        success: function (response) {
            quizList = response;
            console.log("quiz list : " + response);
            let card = quizList[quizCurrentPage];
            let len = "_".repeat(card.word.length); // 단어 길이만큼 _를 반복
            document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(card.word, `<strong>${len}</strong>`);
            document.getElementById('fillBlankMeaning').innerText = card.meaning;
            document.getElementById('userAnswer').value = ""; // 입력 필드 초기화
            let fillBlankModal = new bootstrap.Modal(document.getElementById('fillBlankModal'));
            fillBlankModal.show();
        }
    })

}