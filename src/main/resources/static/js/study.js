let cards;
let username = "guest"
let maxPage = localStorage.getItem(username+"maxPage")?JSON.parse(localStorage.getItem(username+"maxPage")).value:-1;
let exchange; // maxPage의 값이 변경되었는지 체크

function getStudyWords() {
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
    closeButton.focus(); // 또는 document.body.focus(); 등으로 이동 가능

    const now = new Date();
    const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0); // 다음 날 자정
    const ttl = midnight.getTime() - now.getTime(); // 밀리초 단위 남은 시간
    setTTL(currentName, currentCard, ttl)
    if(exchange===9){
        saveStudy()
    }
});


function saveStudy(){
    const now = new Date();
    const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0); // 다음 날 자정
    const ttl = midnight.getTime() - now.getTime(); // 밀리초 단위 남은 시간
    setTTL(username+"maxPage", maxPage, ttl)
    exchange = 3
    setTTL(username+"exchange",exchange, ttl)
    $.ajax({
        type: "GET",
        url: `/study-words/${maxPage}/${username}`,
        headers: {},
        data: {},
        contentType: false,
        processData: false,
        success: function (response) {
        }
    })
}