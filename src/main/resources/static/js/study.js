let cards;
let username = "guest";
let maxPage = localStorage.getItem(username + "maxPage") ? JSON.parse(localStorage.getItem(username + "maxPage")).value : -1;
let exchange; // maxPage의 값이 변경되었는지 체크
let startTime;

function showStudyModal() {
  updateCardDisplay();
  let studyModal = new bootstrap.Modal(document.getElementById('studyModal'));
  studyModal.show();
  speakText();
}

function updateCardDisplay() {
  if (maxPage < currentCard) {
    maxPage = currentCard;
    exchange = 9;
  }
  let card = cards[currentCard];
  document.getElementById('wordTitle').innerText = card.word;
  document.getElementById('wordMeaning').innerText = card.meaning;
  document.getElementById('exampleSentence').innerHTML = card.sentence;
  document.getElementById('exampleSentence-meaning').innerHTML = card.sentence_meaning;

  // 이전 버튼 숨김/표시 제어
  let beforeButton = document.querySelector('.btn-modal.before');
  let nextButton = document.querySelector('.btn-modal.next');
  beforeButton.style.display = currentCard === 0 ? 'none' : 'inline-block';
  nextButton.style.display = currentCard === cards.length - 1 ? 'none' : 'inline-block';
}

function beforeCard() {
  if (currentCard > 0) currentCard--;
  tts_stop();
  updateCardDisplay();
  speakText();
}

function nextCard() {
  if (currentCard < cards.length - 1) currentCard++;
  tts_stop();
  updateCardDisplay();
  speakText();
}

function getStudyWords() {
  startTime = Date.now();
  if (localStorage.getItem(username + "exchange") != null) {
    exchange = JSON.parse(localStorage.getItem(username + "exchange")).value;
  } else {
    exchange = 3;
  }

  if (localStorage.getItem(username)) {
    cards = JSON.parse(localStorage.getItem(username)).value;
    showStudyModal();
  } else {
    $.ajax({
      type: "GET",
      url: `/study-words/${username}`,
      contentType: false,
      processData: false,
      success: function (response) {
        const now = new Date();
        const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0);
        const ttl = midnight.getTime() - now.getTime();
        setTTL(username, response, ttl);
        cards = response;
        let temp = `
                    <h5 class="card-title english-text" id="wordTitle">${response[0]["word"]}</h5>
                    <p class="card-text korean-text"><strong>뜻:</strong> <span id="wordMeaning">${response[0]["meaning"]}</span></p>
                    <p class="card-text english-text" id="exampleSentence">${response[0]["sentence"]}</p>
                    <p class="card-text korean-text" id="exampleSentence-meaning">${response[0]["sentence_meaning"]}</p>
                    <button class="btn-modal before" onclick="beforeCard()">이전</button>
                    <button class="btn-modal next" onclick="nextCard()">다음</button>
                `;
        $(".card-body").html(temp);
        showStudyModal();
      }
    });
  }
}

// 만료 시간 설정 (하루에서 남은 시간)
function setTTL(key, value, ttl) {
  const expiry = Date.now() + ttl;
  localStorage.setItem(key, JSON.stringify({value, expiry}));
}

let currentName = username + "NowPage";
let currentCard = localStorage.getItem(currentName) ? JSON.parse(localStorage.getItem(currentName)).value : 0;

const studyModal = document.getElementById("studyModal");
const closeButton = document.querySelector('.btn-close');

// 모달이 닫힐 때 localStorage에 저장하는 EventListener 추가
studyModal.addEventListener("hidden.bs.modal", () => {
  saveTime(startTime, Date.now(), "study");
  tts_stop();
  closeButton.focus();
  if (exchange === 9) saveStudy();
});

function saveStudy() {
  let data = {"page": maxPage, "username": username};
  $.ajax({
    type: "POST",
    url: `/study-words`,
    data: JSON.stringify(data),
    contentType: 'application/json',
    processData: false,
    success: function () {
      const now = new Date();
      const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0);
      const ttl = midnight.getTime() - now.getTime();
      setTTL(username + "maxPage", maxPage, ttl);
      exchange = 3;
      setTTL(username + "exchange", exchange, ttl);
      setTTL(currentName, currentCard, ttl);
    }
  });
}
