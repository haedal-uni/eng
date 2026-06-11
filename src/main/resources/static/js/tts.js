const synth = window.speechSynthesis;
let voices = [];

function populateVoiceList() {
  voices = synth.getVoices();
}
populateVoiceList();
if (speechSynthesis.onvoiceschanged !== undefined) {
  speechSynthesis.onvoiceschanged = populateVoiceList;
}

function getTtsElements() {
  return {
    pitchInput: document.getElementById("tts-pitch"),
    rateInput:  document.getElementById("tts-rate"),
    pitchValue: document.getElementById("tts-pitch-value"),
    rateValue:  document.getElementById("tts-rate-value"),
    sentence:   document.getElementById('exampleSentence'),
  };
}

document.addEventListener('DOMContentLoaded', function () {
  const studyModal = document.getElementById('studyModal');
  if (!studyModal) return;

  studyModal.addEventListener('shown.bs.modal', function () {
    const { pitchInput, rateInput, pitchValue, rateValue } = getTtsElements();
    if (!pitchInput || !rateInput) return;

    if (pitchInput.dataset.bound) return;
    pitchInput.dataset.bound = 'true';

    pitchInput.addEventListener("input", () => {
      pitchValue.textContent = pitchInput.value;
    });
    rateInput.addEventListener("input", () => {
      rateValue.textContent = rateInput.value;
    });
  });
});

function speakText() {
  const { pitchInput, rateInput, sentence } = getTtsElements();
  if (!sentence || !sentence.textContent.trim()) return;

  // 이미 읽고 있으면 중지 후 재시작
  synth.cancel();

  const utterance = new SpeechSynthesisUtterance(sentence.textContent);
  utterance.lang  = "en-US";
  utterance.pitch = pitchInput ? parseFloat(pitchInput.value) : 1;
  utterance.rate  = rateInput  ? parseFloat(rateInput.value)  : 1;
  synth.speak(utterance);
}

function tts_stop() {
  synth.cancel();
}
