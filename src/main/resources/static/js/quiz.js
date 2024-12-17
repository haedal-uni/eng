let quizList=[]; // 퀴즈 목록
let quizCurrentPage; // 퀴즈 현재 페이지
let chance = 1; // correct=true로 바꿀 수 있는 기회
let quizId_List= localStorage.getItem("quizId_List")?JSON.parse(localStorage.getItem("quizId_List")):[];

// 답 확인 기능 (학습하기)
function checkAnswer() {
    let card = quizList[quizCurrentPage];
    alert(`정답: ${card.word}`);
}

// 답 확인 기능 (빈칸 채우기)
function checkFillBlank() {
    let card = quizList[quizCurrentPage]["studyResponseDto"]
    let userAnswer = document.getElementById('userAnswer').value.trim();
    let answerElement = document.querySelector('#fillBlankSentence');
    let showAnswerBtn = document.getElementById('showAnswerBtn'); // 정답 버튼

    if (userAnswer.toLowerCase() === card.word.toLowerCase()) {
        answerElement.classList.add('correct');
        answerElement.innerText = card.sentence;
        alert("정답입니다!");
        if(chance===1){ // 한번에 정답을 맞췄을 경우
            if(!quizId_List.includes(quizList[quizCurrentPage]["quizId"])){
                quizId_List.push(quizList[quizCurrentPage]["quizId"]); // 맞춘 quiz_id 저장
                localStorage.setItem("quizId_List",JSON.stringify(quizId_List)); // localstorage 저장
                quizList.splice(quizCurrentPage,1); // quizList에서 정답인 quiz 삭제
                quizCurrentPage-=1;
            }
        }
    } else {
        chance = 0;
        answerElement.classList.add('incorrect');
        alert("틀렸습니다.");
        showAnswerBtn.style.display = 'inline-block'; // 틀렸을 경우 정답 버튼 보이기
    }
}

function fillBlankAnswer(){
    let card = quizList[quizCurrentPage]["studyResponseDto"]
    let answerElement = document.querySelector('#fillBlankSentence');
    answerElement.innerText = card.sentence; // 빈칸에 정답 채우기
}

// 빈칸 채우기 모달에서 다음 문제로 이동 시에도 같은 처리
function nextFillBlank() {
    let showAnswerBtn = document.getElementById('showAnswerBtn');
    showAnswerBtn.style.display = 'display: none';
    quizCurrentPage = (quizCurrentPage + 1) % quizList.length;

    // 모달을 유지한 상태에서 빈칸 채우기의 내용을 업데이트
    showQuiz()
    if(quizList.length<1){ // 1개 미만일 경우 데이터 업데이트
        getRandomQuiz()
    }
}

function showQuiz(){
    chance = 1;
    let card = quizList[quizCurrentPage]["studyResponseDto"]
    let s_word;
    if(card.sentence.indexOf(card.word)!==-1){
        s_word = card.word
    }else{
        s_word = similarWord(card, card.word);
    }
    document.getElementById('fillBlankSentence').innerHTML = card.sentence.replace(s_word, `_____`);
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
            if(response.length===0){ // 객체 빈값 확인
                alert("quiz를 모두 맞췄습니다.")
            }else{
                quizCurrentPage = 0;
                //quizList.push(...response);
                quizList = quizList.concat(response);
                showQuiz();
                let fillBlankModal = new bootstrap.Modal(document.getElementById('fillBlankModal'));
                fillBlankModal.show();
            }
        }
    })
}

function changeCorrect(){
    $.ajax({
        type: "PUT",
        url: `/quiz/${username}`,
        headers: {},
        data: JSON.stringify({quizId_List}),
        contentType: 'application/json',
        processData: false,
        success: function (response) {
            quizId_List=[]; // quiz_id 삭제
            localStorage.removeItem("quizId_List")
        }
    })
}

const quizModal = document.getElementById("fillBlankModal");
quizModal.addEventListener("hidden.bs.modal", () => {
    document.activeElement?.blur();
    if(quizId_List.length>0){
        changeCorrect();
    }
});