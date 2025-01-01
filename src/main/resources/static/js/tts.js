const synth = window.speechSynthesis;
const pitchInput = document.getElementById("tts-pitch");
const rateInput = document.getElementById("tts-rate");
const pitchValue = document.getElementById("tts-pitch-value");
const rateValue = document.getElementById("tts-rate-value");
const sentence = document.getElementById('exampleSentence');

let voices = [];
// 음성 목록 업데이트
function populateVoiceList() {
    voices = synth.getVoices(); // 지원되는 음성 목록 가져오기
}

populateVoiceList();
if (speechSynthesis.onvoiceschanged !== undefined) {
    speechSynthesis.onvoiceschanged = populateVoiceList;
}

// 실시간 값 업데이트
pitchInput.addEventListener("input", () => {
    pitchValue.textContent = pitchInput.value;
});
rateInput.addEventListener("input", () => {
    rateValue.textContent = rateInput.value;
});

// 텍스트 읽기
function speakText() {
    const lang = "en-US";
    const utterance = new SpeechSynthesisUtterance(sentence.textContent);
    utterance.lang = lang;

    // pitch와 rate 설정
    utterance.pitch = parseFloat(pitchInput.value);
    utterance.rate = parseFloat(rateInput.value);

    synth.speak(utterance);
}

function tts_stop(){
    synth.cancel();
}