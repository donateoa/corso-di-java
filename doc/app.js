const courses = [
  { id: "corso1", name: "Corso Spring base", level: "intermediate", json: "../json/corso1.json" },
  { id: "actuator", name: "Actuator", level: "basic", json: "../json/actuator.json" },
  { id: "ai", name: "Spring + AI", level: "intermediate", json: "../json/ai.json" },
  { id: "rest", name: "REST", level: "basic", json: "../json/rest.json" },
  { id: "springsecurity", name: "Spring Security", level: "intermediate", json: "../json/springsecurity.json" },
];

const state = {
  current: null,
  questions: [],
  index: 0,
  correct: 0,
  wrong: 0,
  startTime: null,
  timerHandle: null,
  lastAnswered: false,
};

const homeEl = document.getElementById("home");
const quizEl = document.getElementById("quiz");
const courseGridEl = document.getElementById("course-grid");
const courseTitleEl = document.getElementById("course-title");
const progressEl = document.getElementById("progress");
const timerEl = document.getElementById("timer");
const questionTextEl = document.getElementById("question-text");
const questionMetaEl = document.getElementById("question-meta");
const optionsEl = document.getElementById("options");
const submitBtn = document.getElementById("submit-btn");
const nextBtn = document.getElementById("next-btn");
const backBtn = document.getElementById("back-btn");
const feedbackEl = document.getElementById("feedback");

function formatDuration(ms) {
  const totalSec = Math.max(0, Math.floor(ms / 1000));
  const minutes = Math.floor(totalSec / 60);
  const seconds = totalSec % 60;
  if (minutes >= 60) {
    const hours = Math.floor(minutes / 60);
    const remMin = minutes % 60;
    return `${String(hours).padStart(2, "0")}:${String(remMin).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
  }
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

function shuffle(array) {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
}

async function loadCounts() {
  const fragments = [];
  for (const course of courses) {
    const card = document.createElement("div");
    card.className = "course";
    card.innerHTML = `
      <div style="display:flex; align-items:center; justify-content:space-between; gap:8px;">
        <div style="font-weight:700;">${course.name}</div>
        <span class="pill">Livello: ${course.level}</span>
      </div>
      <div class="muted" id="count-${course.id}">Caricamento domande...</div>
      <div style="margin-top:10px; display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
        <button data-course="${course.id}">Avvia quiz</button>
      </div>
    `;
    fragments.push(card);
    fetch(course.json)
      .then((res) => res.json())
      .then((data) => {
        const countEl = document.getElementById(`count-${course.id}`);
        const n = Array.isArray(data.questions) ? data.questions.length : 0;
        countEl.textContent = `Domande: ${n}`;
      })
      .catch(() => {
        const countEl = document.getElementById(`count-${course.id}`);
        if (countEl) countEl.textContent = "Domande: n/d";
      });
  }
  fragments.forEach((f) => courseGridEl.appendChild(f));
  courseGridEl.addEventListener("click", (e) => {
    const btn = e.target.closest("button[data-course]");
    if (btn) {
      startCourse(btn.getAttribute("data-course"));
    }
  });
}

async function startCourse(courseId) {
  const course = courses.find((c) => c.id === courseId);
  if (!course) return;
  try {
    const res = await fetch(course.json);
    const data = await res.json();
    const qs = Array.isArray(data.questions) ? data.questions.slice() : [];
    if (!qs.length) {
      alert("Nessuna domanda trovata per questo corso.");
      return;
    }
    shuffle(qs);
    state.current = course;
    state.questions = qs;
    state.index = 0;
    state.correct = 0;
    state.wrong = 0;
    state.startTime = Date.now();
    state.lastAnswered = false;
    if (state.timerHandle) clearInterval(state.timerHandle);
    state.timerHandle = setInterval(() => {
      timerEl.textContent = formatDuration(Date.now() - state.startTime);
    }, 1000);
    timerEl.textContent = "00:00";
    homeEl.style.display = "none";
    quizEl.style.display = "block";
    courseTitleEl.textContent = `${course.name} (${course.level})`;
    feedbackEl.style.display = "none";
    nextBtn.style.display = "none";
    renderQuestion();
  } catch (err) {
    console.error(err);
    alert("Errore nel caricamento del corso.");
  }
}

function renderQuestion() {
  const q = state.questions[state.index];
  if (!q) return;
  const total = state.questions.length;
  progressEl.textContent = `Domanda ${state.index + 1} / ${total} — Corrette: ${state.correct} — Sbagliate: ${state.wrong}`;
  questionTextEl.textContent = q.text;
  const isMultiple = Array.isArray(q.correct) && q.correct.length > 1;
  questionMetaEl.textContent = isMultiple ? "Risposta multipla: seleziona tutte le opzioni corrette." : "Risposta singola: seleziona una sola opzione.";
  optionsEl.innerHTML = "";
  const type = isMultiple ? "checkbox" : "radio";
  q.options.forEach((opt, idx) => {
    const li = document.createElement("li");
    const id = `opt-${state.index}-${idx}`;
    li.innerHTML = `
      <input type="${type}" name="opt-${state.index}" id="${id}" value="${idx}" />
      <label for="${id}"><span class="badge">${String.fromCharCode(65 + idx)}</span> ${opt}</label>
    `;
    optionsEl.appendChild(li);
  });
  feedbackEl.style.display = "none";
  submitBtn.disabled = false;
  submitBtn.style.display = "inline-block";
  nextBtn.style.display = "none";
  state.lastAnswered = false;
}

function getUserAnswers() {
  const inputs = optionsEl.querySelectorAll("input");
  const selected = [];
  inputs.forEach((inp) => {
    if (inp.checked) selected.push(Number(inp.value));
  });
  return selected;
}

function toLetterSet(indexes) {
  return new Set(indexes.map((i) => String.fromCharCode(65 + i)));
}

function checkAnswer() {
  const q = state.questions[state.index];
  const selected = getUserAnswers();
  if (!selected.length) {
    alert("Seleziona almeno una risposta.");
    return;
  }
  const selectedLetters = toLetterSet(selected);
  const correctLetters = new Set((q.correct || []).map((c) => c.toUpperCase()));
  const isCorrect = selectedLetters.size === correctLetters.size && [...selectedLetters].every((c) => correctLetters.has(c));
  if (isCorrect) {
    state.correct += 1;
    feedbackEl.className = "feedback ok";
    feedbackEl.textContent = "✅ Corretto!";
  } else {
    state.wrong += 1;
    const correctList = [...correctLetters].join(", ");
    feedbackEl.className = "feedback ko";
    feedbackEl.textContent = `❌ Sbagliato. Risposta corretta: ${correctList}`;
  }
  if (q.answer) {
    feedbackEl.textContent += ` ${q.answer}`;
  }
  feedbackEl.style.display = "block";
  state.lastAnswered = true;
  submitBtn.disabled = true;
  submitBtn.style.display = "none";
  nextBtn.style.display = "inline-block";
  updateProgress();
}

function updateProgress() {
  const total = state.questions.length;
  progressEl.textContent = `Domanda ${state.index + 1} / ${total} — Corrette: ${state.correct} — Sbagliate: ${state.wrong}`;
}

function nextQuestion() {
  const total = state.questions.length;
  if (!state.lastAnswered) return;
  if (state.index + 1 >= total) {
    finishQuiz();
    return;
  }
  state.index += 1;
  renderQuestion();
}

function finishQuiz() {
  const total = state.questions.length;
  const pct = total ? ((state.correct * 100) / total).toFixed(1) : "0.0";
  const elapsed = state.startTime ? formatDuration(Date.now() - state.startTime) : "00:00";
  alert(`Quiz completato!\\nCorrette: ${state.correct}\\nSbagliate: ${state.wrong}\\nPercentuale: ${pct}%\\nTempo: ${elapsed}`);
  goHome();
}

function goHome() {
  quizEl.style.display = "none";
  homeEl.style.display = "block";
  if (state.timerHandle) clearInterval(state.timerHandle);
}

submitBtn.addEventListener("click", checkAnswer);
nextBtn.addEventListener("click", nextQuestion);
backBtn.addEventListener("click", goHome);

loadCounts();
