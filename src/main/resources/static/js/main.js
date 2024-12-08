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