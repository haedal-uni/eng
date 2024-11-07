let cards=[];
let username = "guest"

function getStudyWords() {
    if(localStorage.getItem(username)){
        cards = localStorage.getItem(JSON.parse(localStorage.getItem(username)))
    }else{
        $.ajax({
            type: "GET",
            url: `/study-words/${username}`,
            headers: {},
            data: {},
            contentType: false,
            processData: false,
            success: function (response) {
                window.localStorage.setItem(username,JSON.stringify(response));
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