// 예시
const cards = [
    {
        word: "food",
        meaning: "음식",
        sentence: "I love Italian food.",
        answer: "food"
    },
    {
        word: "gift",
        meaning: "선물",
        sentence: "The watch was a gift from my mother.",
        answer: "gift"
    }
];

let currentCard = 0;

// 학습하기 모달의 단어 표시
function showStudyModal() {
    const card = cards[currentCard];
    document.getElementById('wordTitle').innerText = card.word;
    document.getElementById('wordMeaning').innerText = card.meaning;
    document.getElementById('exampleSentence').innerHTML = card.sentence;
    const studyModal = new bootstrap.Modal(document.getElementById('studyModal'));
    studyModal.show();
}

// 빈칸 채우기 모달의 단어 표시
function showFillBlankModal() {
    const card = cards[currentCard];
    const len = "_".repeat(card.answer.length); // 단어 길이만큼 _를 반복
    document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(card.answer, `<strong>${len}</strong>`);

    document.getElementById('fillBlankMeaning').innerText = card.meaning;
    document.getElementById('userAnswer').value = ""; // 입력 필드 초기화
    const fillBlankModal = new bootstrap.Modal(document.getElementById('fillBlankModal'));
    fillBlankModal.show();
}

// 답 확인 기능 (학습하기)
function checkAnswer() {
    const card = cards[currentCard];
    alert(`정답: ${card.answer}`);
}

// 답 확인 기능 (빈칸 채우기)
function checkFillBlank() {
    const card = cards[currentCard];
    const userAnswer = document.getElementById('userAnswer').value.trim();
    const answerElement = document.querySelector('#fillBlankSentence strong');
    const showAnswerBtn = document.getElementById('showAnswerBtn'); // 정답 버튼

    if (userAnswer.toLowerCase() === card.answer.toLowerCase()) {
        answerElement.classList.add('correct');
        answerElement.innerText = card.answer;
        alert("정답입니다!");
    } else {
        answerElement.classList.add('incorrect');
        alert("틀렸습니다.");
        showAnswerBtn.style.display = 'inline-block'; // 틀렸을 경우 정답 버튼 보이기
    }
}

function fillBlankAnswer(){
    const card = cards[currentCard];
    const answerElement = document.querySelector('#fillBlankSentence strong');
    answerElement.innerText = card.answer; // 빈칸에 정답 채우기
}

function nextCard() {
    currentCard = (currentCard + 1) % cards.length;

    // 모달을 유지한 상태에서 카드의 내용을 업데이트
    const card = cards[currentCard];
    document.getElementById('wordTitle').innerText = card.word;
    document.getElementById('wordMeaning').innerText = card.meaning;
    document.getElementById('exampleSentence').innerHTML = card.sentence.replace(card.answer, '<strong>__</strong>');
}

// 빈칸 채우기 모달에서 다음 문제로 이동 시에도 같은 처리
function nextFillBlank() {
    currentCard = (currentCard + 1) % cards.length;

    // 모달을 유지한 상태에서 빈칸 채우기의 내용을 업데이트
    const card = cards[currentCard];
    document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(card.answer, '<strong>__</strong>');
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
