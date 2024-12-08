let quizList;
let quizCurrentPage;

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
    let showAnswerBtn = document.getElementById('showAnswerBtn');
    showAnswerBtn.style.display = 'display: none';
    quizCurrentPage = (quizCurrentPage + 1) % quizList.length;

    // 모달을 유지한 상태에서 빈칸 채우기의 내용을 업데이트
    showQuiz()
}

function showQuiz(){
    let card = quizList[quizCurrentPage];
    let s_word;
    if(card.sentence.indexOf(card.word)!==-1){
        s_word = card.word
    }else{
        s_word = similarWord(card, card.word);
    }
    let len = "_".repeat(s_word.length); // 단어 길이만큼 _를 반복
    document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(s_word, `<strong>${len}</strong>`);
    document.getElementById('fillBlankMeaning').innerText = card.meaning;
    document.getElementById('userAnswer').value = ""; // 입력 필드 초기화
}

function similarWord(card, word){
    let arr = card.sentence.split(" ");
    let similar;
    let min = 999;
    let x = 0;
    for(let i=0; i<arr.length; i++){
        x = getLevenshteinDistance(word, arr[i]);
        if(x<min){
            min = x;
            similar = arr[i];
        }
    }
    return similar;
}

function getLevenshteinDistance(a, b) {
    const table = Array.from({ length: a.length + 1 }, () => Array(b.length + 1).fill(0));
    for (let i = 1; i <= a.length; i++) {
        table[i][0] = i;
    }
    for (let j = 1; j <= b.length; j++) {
        table[0][j] = j;
    }
    for (let i = 1; i <= a.length; i++) {
        for (let j = 1; j <= b.length; j++) {
            const insert = table[i - 1][j] + 1;
            const del = table[i][j - 1] + 1;
            const replace = (a[i - 1] === b[j - 1] ? 0 : 1) + table[i - 1][j - 1];
            table[i][j] = Math.min(insert, del, replace);
        }
    }
    return table[a.length][b.length];
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
            quizCurrentPage = 0;
            showQuiz();
            let fillBlankModal = new bootstrap.Modal(document.getElementById('fillBlankModal'));
            fillBlankModal.show();
        }
    })
}