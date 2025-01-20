const chatbotModal = document.getElementById("chatbotModal");
const chatbotOpenBtn = document.getElementById("chatbotOpen");
const closeChatbot = document.getElementById("closeChatbot");
const chatMessages = document.getElementById("chatMessages");
const sendBtn = document.getElementById("sendBtn");
const chatInput = document.getElementById("chatInput");

let currentStep = "main"; // 현재 단계: "main", "group", "detail", "lemmatizer"
let selectedGroup = ""; // 선택된 그룹: synonyms, examples, 등
let selectedCommand = ""; // 선택된 세부 명령
let selectedPOS = ""; // 선택된 품사 (lemmatizer 전용)
let socket;


const fixedButtons = document.getElementById("fixedButtons");
const prevButton = document.getElementById("prevButton");

// "이전으로 돌아가기" 버튼 동작
prevButton.addEventListener("click", () => {
    addOptionsButtons("main");
    fixedButtons.classList.add("hidden");
});

function toggleFixedButtons(visible) {
    if (visible) {
        fixedButtons.classList.remove("hidden");
    } else {
        fixedButtons.classList.add("hidden");
    }
}

chatbotOpenBtn.addEventListener("click", () => {
    chatbotModal.classList.remove("hidden");
    chatbotModal.style.display = "flex";
    openChatBot();
});

// 닫기 버튼 클릭 시 모달 숨기기
closeChatbot.addEventListener("click", () => {
    chatbotModal.style.display = "none";
    if (socket) {
        console.log("Disconnected");
    }
});

function openChatBot() {
    currentStep = "main"; // 초기화
    selectedGroup = "";
    selectedCommand = "";
    selectedPOS = "";
    clearChatMessages();
    if (!socket) {
        socket = io.connect("http://localhost:5000");
        my_messageResponse(socket);
    }
    addMessageToChat("bot", "환영합니다! 아래 옵션을 선택해주세요:", true);
}

function addMessageToChat(sender, message, includeButtons = false, buttonsType = "main") {
    const messageDiv = document.createElement("div");
    messageDiv.classList.add("message", `${sender}-message`);
    messageDiv.innerHTML = `${message}`;
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    console.log("button : " + buttonsType)
    if(buttonsType === "afterResponse"){
        toggleFixedButtons(true);
    }
    if (includeButtons) {
        addOptionsButtons(buttonsType);
    }
}

function addOptionsButtons(type) {
    const messageDiv = document.createElement("div");
    messageDiv.classList.add("message", "bot-message");
    messageDiv.innerHTML = `<p>옵션을 선택하세요:</p>`;

    const optionsDiv = document.createElement("div");
    optionsDiv.classList.add("options");

    let options = [];
    if (type === "main") {
        // 메인 옵션: 전체 기능 그룹
        options = ["synonyms", "examples", "definition", "pos", "lemmatizer"];
    } else if (type === "lemmatizer-pos") {
        // lemmatizer 품사 옵션
        options = ["n", "v", "a", "s", "r"];
    }

    options.forEach(option => {
        const optionButton = document.createElement("button");
        optionButton.classList.add("option-button");
        optionButton.textContent = getButton(option);
        optionButton.setAttribute("data-command", option);

        optionButton.addEventListener("click", () => handleOptionClick(option, type));
        optionsDiv.appendChild(optionButton);
    });
    messageDiv.appendChild(optionsDiv);
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// 서버 응답 처리
function my_messageResponse(socket) {
    socket.on("reply", function (data) {
        console.log(data)
        let msg = "";
        try{
            if (!data.response || JSON.parse(data.response).length === 0 || data.response[0] === "") {
                addMessageToChat("bot", "결과를 찾을 수 없습니다. <br>다른 단어를 입력해주세요.", false, "afterResponse");
            } else {
                let option = data.option
                const pos_list = {"n":"명사", "v":"동사", "r" : "부사", "a":"형용사", "s": "adjective satellite"}
                if(option==="pos"){
                    for(let i = 0; i<JSON.parse(data.response).length; i++){
                        msg+=pos_list[JSON.parse(data.response)[i]];
                        if(i!==JSON.parse(data.response).length-1){
                            msg+=", "
                        }
                    }
                }else{
                    if(JSON.parse(data.response).length>1){
                        let i = 1;
                        JSON.parse(data.response).forEach((response) => {
                            console.log(response)
                            msg+=(i + ". ")
                            msg+=response;
                            msg+=`<br><br>`;
                            i++;
                        });
                    }else{
                        msg+=JSON.parse(data.response);
                    }
                }
                addMessageToChat("bot", msg, false, "afterResponse");
            }
        }catch (error){
            console.error("Error parsing response:", error);
            addMessageToChat("bot", "결과를 처리할 수 없습니다.", false);
        }
    });
}


function clearChatMessages() {
    chatMessages.innerHTML = "";
}

// 메시지 전송
sendBtn.addEventListener("click", () => {
    const message = chatInput.value.trim();

    if (message) {
        if (selectedGroup === "lemmatizer") {
            if (!selectedPOS) {
                addMessageToChat("bot", "품사를 선택하고 메시지를 입력하세요.", false);
                return; // 동작 중단
            }
            socket.emit("my_message", {command: selectedGroup, word: message, pos: selectedPOS});
        } else if (selectedGroup) {
            socket.emit("my_message", {command: selectedGroup, word: message});
        } else {
            addMessageToChat("bot", "옵션을 선택하고 메시지를 입력하세요.", false);
            return;
        }
        addMessageToChat("user", message);
        chatInput.value = "";
    }
});

// 'Enter' 키를 누르면 send 버튼 클릭처럼 동작하도록 처리
chatInput.addEventListener("keydown", function (e) {
    if (e.key === "Enter" && chatInput.value.trim() !== "") {
        sendBtn.click(); // Send 버튼 클릭
        e.preventDefault(); // 엔터키 기본 동작 (줄바꿈)을 막기
    }
})

// 버튼 이름 설정
function getButton(option) {
    const labels = {
        synonyms: "동의어",
        examples: "예문",
        definition: "정의",
        pos: "품사",
        lemmatizer: "품사 변환 (lemmatizer)",
        // "이전으로 돌아가기": "이전으로 돌아가기",
        n: "명사 (n)",
        v: "동사 (v)",
        a: "형용사 (a)",
        s: "adjective satellite (s)",
        r: "부사 (r)",
    };
    return labels[option] || option;
}

// 옵션 클릭 처리
function handleOptionClick(command, type) {
    if (type === "main") {
        selectedGroup = command;
        if (command === "lemmatizer") {
            currentStep = "lemmatizer-pos";
            addMessageToChat("bot", "품사를 선택해주세요:", true, "lemmatizer-pos");
        } else {
            currentStep = "detail";
            addMessageToChat("bot", `${command}를 선택했습니다. <br>단어를 입력하세요!`, false);
        }
    } else if (type === "lemmatizer-pos") {
        selectedPOS = command;
        addMessageToChat("bot", `품사(${command})를 선택했습니다. <br>단어를 입력하세요!`, false);
    }
}