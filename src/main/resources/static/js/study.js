let cards;
let username = "guest"
let maxPage = localStorage.getItem(username+"maxPage")?JSON.parse(localStorage.getItem(username+"maxPage")).value:-1;
let exchange; // maxPage의 값이 변경되었는지 체크
let startTime;

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

function getStudyWords() {
    startTime = Date.now();
    if(localStorage.getItem(username+"exchange")!=null){
        exchange = JSON.parse(localStorage.getItem(username+"exchange")).value
    }else{
        exchange = 3;
    }

    if(localStorage.getItem(username)){
        cards = JSON.parse(localStorage.getItem(username)).value
        showStudyModal();
    }else{
        $.ajax({
            type: "GET",
            url: `/study-words/${username}`,
            headers: {},
            data: {},
            contentType: false,
            processData: false,
            success: function (response) {
                const now = new Date();
                const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0); // 다음 날 자정
                const ttl = midnight.getTime() - now.getTime(); // 밀리초 단위 남은 시간
                setTTL(username,response, ttl)
                cards = response;
                let temp = `
            <h5 class="card-title english-text" id="wordTitle">${response[0]["word"]}</h5>
            <p class="card-text korean-text"><strong>뜻:</strong> <span id="wordMeaning">${response[0]["meaning"]}</span></p>
            <p class="card-text english-text" id="exampleSentence">${response[0]["sentence"]}</p>
            <p class="card-text korean-text" id="exampleSentence-meaning">${response[0]["sentence_meaning"]}</p>
            <button class="btn-modal before" onclick="beforeCard()">이전</button>
            <button class="btn-modal next" onclick="nextCard()">다음</button>
            `
                $(".card-body").html(temp)
                showStudyModal();
            }
        })

    }
}

// 만료 시간 설정 (하루에서 남은 시간)
function setTTL(key, value, ttl){
    const expiry = Date.now() + ttl; // 현재 날짜 + TTL(ms)
    const item = {
        value, // 저장할 데이터
        expiry // 만료 시간
    };
    localStorage.setItem(key, JSON.stringify(item));
}

let currentName = username+"NowPage";
let currentCard = localStorage.getItem(currentName)?JSON.parse(localStorage.getItem(currentName)).value:0;
// studyModal 요소 선택
const studyModal = document.getElementById("studyModal");
const closeButton = document.querySelector('.btn-close'); // 모달 닫기 버튼

// 모달이 닫힐 때 localStorage에 저장하는 EventListener 추가
studyModal.addEventListener("hidden.bs.modal", () => {
    saveTime(startTime, Date.now(), "study");
    closeButton.focus(); // 또는 document.body.focus(); 등으로 이동 가능
    if(exchange===9){
        saveStudy()
    }
});

function saveStudy(){
    let data = {"page" : maxPage, "username":username}
    $.ajax({
        type: "POST",
        url: `/study-words`,
        headers: {},
        data: JSON.stringify(data),
        contentType: 'application/json',
        processData: false,
        success: function (response) {
            const now = new Date();
            const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0); // 다음 날 자정
            const ttl = midnight.getTime() - now.getTime(); // 밀리초 단위 남은 시간
            setTTL(username+"maxPage", maxPage, ttl)
            exchange = 3
            setTTL(username+"exchange",exchange, ttl)
            setTTL(currentName, currentCard, ttl)
        }
    })
}