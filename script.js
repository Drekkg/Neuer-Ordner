const STORAGE_KEY = "memorizeit_web_profile_v1";
const CARD_BACK_SYMBOLS = [
  "!",
  "@",
  "#",
  "$",
  "%",
  "&",
  "*",
  "+",
  "=",
  "~",
  "^",
  "?",
  "/",
  "\\",
  "|",
  "<",
  ">",
  "[",
  "]",
  "{}"
];

const state = {
  profile: loadProfile(),
  cards: [],
  firstPickId: null,
  secondPickId: null,
  moves: 0,
  lockBoard: false
};

const nodes = {
  profileView: document.getElementById("profile-view"),
  puzzleView: document.getElementById("puzzle-view"),
  form: document.getElementById("profile-form"),
  name: document.getElementById("name"),
  photoGrade: document.getElementById("photo-grade"),
  photoUpload: document.getElementById("photo-upload"),
  photoPreview: document.getElementById("photo-preview"),
  puzzleTitle: document.getElementById("puzzle-title"),
  puzzleStatus: document.getElementById("puzzle-status"),
  gameGrid: document.getElementById("game-grid"),
  emptyState: document.getElementById("empty-state"),
  winBanner: document.getElementById("win-banner"),
  newRound: document.getElementById("new-round"),
  editProfile: document.getElementById("edit-profile")
};

initialize();

function initialize() {
  hydrateProfileForm();
  renderPhotoPreview(state.profile.photos);

  nodes.form.addEventListener("submit", onProfileSubmit);
  nodes.photoUpload.addEventListener("change", onPhotoUpload);
  nodes.newRound.addEventListener("click", startNewRound);
  nodes.editProfile.addEventListener("click", showProfileView);
}

function hydrateProfileForm() {
  nodes.name.value = state.profile.name || "";
  nodes.photoGrade.value = String(state.profile.photoGrade);
}

function onProfileSubmit(event) {
  event.preventDefault();

  const photoGrade = normalizePhotoGrade(nodes.photoGrade.value);

  if (state.profile.photos.length < photoGrade) {
    window.alert(`Upload at least ${photoGrade} photos to use Grade ${photoGrade}.`);
    return;
  }

  state.profile = {
    name: nodes.name.value.trim(),
    photoGrade,
    photos: [...state.profile.photos]
  };

  saveProfile(state.profile);
  startNewRound();
  showPuzzleView();
}

function onPhotoUpload(event) {
  const files = Array.from(event.target.files || []);
  if (files.length === 0) return;

  Promise.all(files.map(fileToDataUrl))
    .then((encodedImages) => {
      state.profile.photos = [...state.profile.photos, ...encodedImages];
      saveProfile(state.profile);
      renderPhotoPreview(state.profile.photos);
      nodes.photoUpload.value = "";
    })
    .catch(() => {
      window.alert("Some images could not be loaded. Please try again.");
    });
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(new Error("Failed to read image"));
    reader.readAsDataURL(file);
  });
}

function loadProfile() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return emptyProfile();
    const parsed = JSON.parse(raw);
    return {
      name: parsed.name || "",
      photoGrade: normalizePhotoGrade(parsed.photoGrade),
      photos: Array.isArray(parsed.photos) ? parsed.photos.filter(Boolean) : []
    };
  } catch {
    return emptyProfile();
  }
}

function saveProfile(profile) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(profile));
}

function emptyProfile() {
  return {
    name: "",
    photoGrade: 5,
    photos: []
  };
}

function buildSeeds(profile) {
  const seeds = [];
  let id = 0;

  for (const src of profile.photos) {
    seeds.push({ id: id++, type: "image", value: src });
  }

  return seeds;
}

function startNewRound() {
  const seeds = shuffle(buildSeeds(state.profile));
  const selected = seeds.slice(0, Math.min(state.profile.photoGrade, seeds.length));
  const backSymbols = shuffle(CARD_BACK_SYMBOLS).slice(0, selected.length * 2);

  let cardId = 0;
  state.cards = shuffle(
    selected.flatMap((seed) => [
      createCard(seed, cardId++, backSymbols),
      createCard(seed, cardId++, backSymbols)
    ])
  );

  state.firstPickId = null;
  state.secondPickId = null;
  state.moves = 0;
  state.lockBoard = false;

  renderPuzzleHeader();
  renderGrid();
  renderWinBanner(false);
}

function showPuzzleView() {
  nodes.profileView.classList.add("hidden");
  nodes.puzzleView.classList.remove("hidden");

  const person = state.profile.name || "Player";
  nodes.puzzleTitle.textContent = `${person}'s Grade ${state.profile.photoGrade} Memory Puzzle`;
  renderPuzzleHeader();
}

function showProfileView() {
  nodes.puzzleView.classList.add("hidden");
  nodes.profileView.classList.remove("hidden");
  renderPhotoPreview(state.profile.photos);
}

function renderPuzzleHeader() {
  const matchedPairs = state.cards.filter((c) => c.isMatched).length / 2 + 1;
  const totalPairs = state.cards.length / 2;
  nodes.puzzleStatus.textContent = `Moves: ${state.moves} | Matched: ${matchedPairs}/${totalPairs}`;
}

function renderPhotoPreview(photos) {
  nodes.photoPreview.innerHTML = "";
  for (const src of photos) {
    const image = document.createElement("img");
    image.src = src;
    image.alt = "Selected personal photo";
    nodes.photoPreview.appendChild(image);
  }
}

function renderGrid() {
  nodes.gameGrid.innerHTML = "";

  if (state.cards.length === 0) {
    nodes.emptyState.classList.remove("hidden");
    return;
  }

  nodes.emptyState.classList.add("hidden");

  for (const card of state.cards) {
    const button = document.createElement("button");
    button.className = "memory-card";
    button.type = "button";

    if (card.isRevealed || card.isMatched) button.classList.add("revealed");
    if (card.isMatched) button.classList.add("matched");

    button.disabled = card.isMatched || state.lockBoard;
    button.setAttribute("aria-label", "Memory card");
    button.addEventListener("click", () => onCardClick(card.cardId));

    if (card.isRevealed || card.isMatched) {
      if (card.type === "image") {
        const image = document.createElement("img");
        image.src = card.value;
        image.alt = "Personal memory photo";
        button.appendChild(image);
      } else {
        const label = document.createElement("div");
        label.className = "card-label";
        label.textContent = card.value;
        button.appendChild(label);
      }
    } else {
      button.textContent = card.backSymbol;
    }

    nodes.gameGrid.appendChild(button);
  }
}

function onCardClick(cardId) {
  if (state.lockBoard) return;

  const card = state.cards.find((c) => c.cardId === cardId);
  if (!card || card.isMatched || card.isRevealed) return;

  card.isRevealed = true;
  renderGrid();

  if (state.firstPickId === null) {
    state.firstPickId = cardId;
    return;
  }

  state.secondPickId = cardId;
  state.moves += 1;
  state.lockBoard = true;
  renderPuzzleHeader();

  const first = state.cards.find((c) => c.cardId === state.firstPickId);
  const second = state.cards.find((c) => c.cardId === state.secondPickId);
  if (!first || !second) {
    resetSelection();
    return;
  }

  if (first.pairId === second.pairId) {
    first.isMatched = true;
    second.isMatched = true;
    resetSelection();
    renderGrid();
    checkWinState();
  } else {
    window.setTimeout(() => {
      first.isRevealed = false;
      second.isRevealed = false;
      resetSelection();
      renderGrid();
    }, 850);
  }
}

function checkWinState() {
  const won = state.cards.length > 0 && state.cards.every((card) => card.isMatched);
  renderWinBanner(won);
}

function renderWinBanner(show) {
  nodes.winBanner.classList.toggle("hidden", !show);
}

function resetSelection() {
  state.firstPickId = null;
  state.secondPickId = null;
  state.lockBoard = false;
}

function createCard(seed, cardId, backSymbols) {
  return {
    cardId,
    pairId: seed.id,
    type: seed.type,
    value: seed.value,
    backSymbol: backSymbols[cardId],
    isRevealed: false,
    isMatched: false
  };
}

function normalizePhotoGrade(value) {
  const parsed = Number.parseInt(value, 10);
  if (Number.isNaN(parsed)) return 5;
  return Math.min(10, Math.max(5, parsed));
}

function shuffle(items) {
  const copy = [...items];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}
