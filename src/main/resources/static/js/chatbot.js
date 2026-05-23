const chatbotModal = document.getElementById("chatbotModal");
const chatbotOpenBtn = document.getElementById("chatbotOpen");
const closeChatbot = document.getElementById("closeChatbot");
const chatMessages = document.getElementById("chatMessages");
const sendBtn = document.getElementById("sendBtn");
const chatInput = document.getElementById("chatInput");
const fixedButtons = document.getElementById("fixedButtons");
const prevButton = document.getElementById("prevButton");

let selectedGroup = ""; // 선택된 그룹: synonyms, examples 등 (옵션 선택)
let selectedPOS = ""; // 선택된 품사 (lemmatizer 전용)
let socket;

// "이전으로 돌아가기" 버튼 동작
prevButton.addEventListener("click", () => {
  addOptionsButtons("main");
  fixedButtons.classList.add("hidden");
});

function toggleFixedButtons(visible) {
  if (visible) fixedButtons.classList.remove("hidden");
  else fixedButtons.classList.add("hidden");
}

chatbotOpenBtn.addEventListener("click", () => {
  chatbotModal.classList.remove("hidden");
  chatbotModal.style.display = "flex";
  openChatBot();
});

// 닫기 버튼 클릭 시 모달 숨기기
closeChatbot.addEventListener("click", () => {
  chatbotModal.style.display = "none";
});

function openChatBot() {
  selectedGroup = "";
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
  messageDiv.innerHTML = message;
  chatMessages.appendChild(messageDiv);
  chatMessages.scrollTop = chatMessages.scrollHeight;

  if (buttonsType === "afterResponse") toggleFixedButtons(true);
  if (includeButtons) addOptionsButtons(buttonsType);
}

function addOptionsButtons(type) {
  const messageDiv = document.createElement("div");
  messageDiv.classList.add("message", "bot-message");
  messageDiv.innerHTML = `<p>옵션을 선택하세요:</p>`;

  const optionsDiv = document.createElement("div");
  optionsDiv.classList.add("options");

  let options = type === "main"
    ? ["synonyms", "examples", "definition", "pos", "lemmatizer"]
    : ["n", "v", "a", "s", "r"];

  options.forEach(option => {
    const btn = document.createElement("button");
    btn.classList.add("option-button");
    btn.textContent = getButton(option);
    btn.setAttribute("data-command", option);
    btn.addEventListener("click", () => handleOptionClick(option, type));
    optionsDiv.appendChild(btn);
  });

  messageDiv.appendChild(optionsDiv);
  chatMessages.appendChild(messageDiv);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function my_messageResponse(socket) {
  socket.on("reply", function (data) {
    let msg = "";
    try {
      if (!data.response || JSON.parse(data.response).length === 0 || data.response[0] === "") {
        addMessageToChat("bot", "결과를 찾을 수 없습니다. <br>다른 단어를 입력해주세요.", false, "afterResponse");
      } else {
        let option = data.option;
        const pos_list = { "n": "명사", "v": "동사", "r": "부사", "a": "형용사", "s": "adjective satellite" };
        if (option === "pos") {
          msg = JSON.parse(data.response).map(p => pos_list[p]).join(", ");
        } else {
          const res = JSON.parse(data.response);
          if (res.length > 1) {
            res.forEach((r, i) => { msg += `${i + 1}. ${r}<br><br>`; });
          } else {
            msg = res[0];
          }
        }
        addMessageToChat("bot", msg, false, "afterResponse");
      }
    } catch (error) {
      console.error("Error parsing response:", error);
      addMessageToChat("bot", "결과를 처리할 수 없습니다.", false);
    }
  });
}

function clearChatMessages() {
  chatMessages.innerHTML = "";
}

sendBtn.addEventListener("click", () => {
  const message = chatInput.value.trim();
  if (!message) return;

  if (selectedGroup === "lemmatizer") {
    if (!selectedPOS) {
      addMessageToChat("bot", "품사를 선택하고 메시지를 입력하세요.", false);
      return;
    }
    socket.emit("my_message", { command: selectedGroup, word: message, pos: selectedPOS });
  } else if (selectedGroup) {
    socket.emit("my_message", { command: selectedGroup, word: message });
  } else {
    addMessageToChat("bot", "옵션을 선택하고 메시지를 입력하세요.", false);
    return;
  }
  addMessageToChat("user", message);
  chatInput.value = "";
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
            addMessageToChat("bot", "품사를 선택해주세요:", true, "lemmatizer-pos");
        } else {
            addMessageToChat("bot", `${command}를 선택했습니다. <br>단어를 입력하세요!`, false);
        }
    } else if (type === "lemmatizer-pos") {
        selectedPOS = command;
        addMessageToChat("bot", `품사(${command})를 선택했습니다. <br>단어를 입력하세요!`, false);
    }
}
