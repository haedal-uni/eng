// 학습하기 모달의 단어 표시
function showStudyModal() {
    updateCardDisplay()
    let studyModal = new bootstrap.Modal(document.getElementById('studyModal'));
    studyModal.show();
}

function updateCardDisplay() {
    if(maxPage<currentCard){
        maxPage = currentCard;
        exchange=9;
    }
    let card = cards[currentCard];
    document.getElementById('wordTitle').innerText = card.word;
    document.getElementById('wordMeaning').innerText = card.meaning;
    document.getElementById('exampleSentence').innerHTML = card.sentence;
    document.getElementById('exampleSentence-meaning').innerHTML = card.sentence_meaning;

    // 이전 버튼 숨김/표시 제어
    let beforeButton = document.querySelector('.btn-modal.before');
    let nextButton = document.querySelector('.btn-modal.next');
    if (currentCard === 0) {
        beforeButton.style.display = 'none';  // 첫 페이지에서 이전 버튼 숨기기
    } else {
        beforeButton.style.display = 'inline-block';  // 두 번째 페이지부터 이전 버튼 표시
    }
    if (currentCard === cards.length - 1) {
        nextButton.style.display = 'none';
    } else {
        nextButton.style.display = 'inline-block';
    }
}

// 빈칸 채우기 모달의 단어 표시
function showFillBlankModal() {
    let card = cards[currentCard];
    let len = "_".repeat(card.word.length); // 단어 길이만큼 _를 반복
    document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(card.word, `<strong>${len}</strong>`);
    document.getElementById('fillBlankMeaning').innerText = card.meaning;
    document.getElementById('userAnswer').value = ""; // 입력 필드 초기화
    let fillBlankModal = new bootstrap.Modal(document.getElementById('fillBlankModal'));
    fillBlankModal.show();
}

// 답 확인 기능 (학습하기)
function checkAnswer() {
    let card = cards[currentCard];
    alert(`정답: ${card.word}`);
}

// 답 확인 기능 (빈칸 채우기)
function checkFillBlank() {
    let card = cards[currentCard];
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
    let card = cards[currentCard];
    let answerElement = document.querySelector('#fillBlankSentence strong');
    answerElement.innerText = card.word; // 빈칸에 정답 채우기
}

function beforeCard(){
    if (currentCard > 0) {
        currentCard--;
        updateCardDisplay();
    }

    // 모달을 유지한 상태에서 카드의 내용을 업데이트
    let card = cards[currentCard];
    document.getElementById('wordTitle').innerText = card.word;
    document.getElementById('wordMeaning').innerText = card.meaning;
    document.getElementById('exampleSentence').innerHTML = card.sentence;
    document.getElementById('exampleSentence-meaning').innerHTML = card.sentence_meaning;
}

function nextCard() {
    if (currentCard < cards.length - 1) {
        currentCard++;
        updateCardDisplay();
    }
    // 모달을 유지한 상태에서 카드의 내용을 업데이트
    let card = cards[currentCard];
    document.getElementById('wordTitle').innerText = card.word;
    document.getElementById('wordMeaning').innerText = card.meaning;
    document.getElementById('exampleSentence').innerHTML = card.sentence;
    document.getElementById('exampleSentence-meaning').innerHTML = card.sentence_meaning;
}

// 빈칸 채우기 모달에서 다음 문제로 이동 시에도 같은 처리
function nextFillBlank() {
    currentCard = (currentCard + 1) % cards.length;

    // 모달을 유지한 상태에서 빈칸 채우기의 내용을 업데이트
    let card = cards[currentCard];
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

// 파일 업로드
function uploadFile(input) {
    let file = input.files[0];
    if (!file) return;

    let formData = new FormData();
    formData.append('file', file);

    // AJAX 파일 업로드
    fetch('/upload-excel', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return "upload success";
            }
            throw new Error('File upload failed');
        })
        .then(message => alert(message))
        .catch(error => alert(error.message));
}