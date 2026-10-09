// Configuração Visual das 5 Matérias (Imagens com DiceBear / Placeholders dinâmicos)
const STAGES_CONFIG = {
    "Português": {
        bg: "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=1000",
        hero: "https://api.dicebear.com/7.x/bottts/svg?seed=HeroinaPortugues",
        heroClass: "CLASSE: LINGUISTA",
        monsters: [
            "Guardião da Gramática", "Espectro da Ortografia", "Troll dos Verbos",
            "Sombra da Sintaxe", "Mago da Semântica", "Titã da Literatura",
            "Dragão da Crase", "Guerreiro da Pontuação", "Oráculo dos Neologismos",
            "SENHOR DAS PALAVRAS"
        ]
    },
    "Matemática": {
        bg: "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=1000",
        hero: "https://api.dicebear.com/7.x/bottts/svg?seed=HeroiMatematica",
        heroClass: "CLASSE: ALGEBRISTA",
        monsters: [
            "Autômato da Adição", "Golem da Subtração", "Esfinge da Multiplicação",
            "Demônio da Divisão", "Lorde das Frações", "Mago da Álgebra",
            "Guardião da Geometria", "Titã das Equações", "Dragão do Cálculo",
            "O ABSOLUTO INFINITO"
        ]
    },
    "Ciências": {
        bg: "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=1000",
        hero: "https://api.dicebear.com/7.x/bottts/svg?seed=HeroinaCiencias",
        heroClass: "CLASSE: CIENTISTA",
        monsters: [
            "Bactéria Gigante", "Mutante Celular", "Espectro Abissal",
            "Mago Periódico", "Titã da Gravidade", "Golem Ecologista",
            "Dragão Termodinâmico", "Guardião do DNA", "Sombra Genética",
            "ENTROPIA SUPREMA"
        ]
    },
    "Geografia": {
        bg: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000",
        hero: "https://api.dicebear.com/7.x/bottts/svg?seed=HeroiGeografia",
        heroClass: "CLASSE: EXPLORADOR",
        monsters: [
            "Guardião do Globo", "Titã Tectônico", "Espírito dos Climas",
            "Golem de Relevo", "Sombra Demográfica", "Mago Cartógrafo",
            "Dragão Vulcânico", "Espectro Oceânico", "Titã Global",
            "GAIA DESTRUIDORA"
        ]
    },
    "História": {
        bg: "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=1000",
        hero: "https://api.dicebear.com/7.x/bottts/svg?seed=HeroiHistoria",
        heroClass: "CLASSE: HISTORIADOR",
        monsters: [
            "Guardião do Tempo", "Faraó Renascido", "Centurião Espectro",
            "Cavaleiro Feudal", "Pirata Temporal", "Guerreiro Antigo",
            "Sombra da Revolução", "Autômato Industrial", "Titã das Eras",
            "CRONOS SUPREMO"
        ]
    }
};

let currentQuestionData = null;
let timerSeconds = 30;
let timerInterval;
let startTime;

// API Call 1: Busca a pergunta atual no servidor Java
async function fetchCurrentQuestion() {
    try {
        const response = await fetch('/api/questions');
        const questionsList = await response.json();
        
        if (questionsList.length > 0) {
            currentQuestionData = questionsList[0]; // Pega a pergunta atual
            renderStage();
        }
    } catch (err) {
        console.error("Erro ao carregar pergunta via API:", err);
    }
}

// Renderiza o cenário e a pergunta na interface
function renderStage() {
    clearInterval(timerInterval);
    const q = currentQuestionData;
    
    // Identifica configuração da matéria (ou fallback para Geografia)
    const config = STAGES_CONFIG[q.subject] || STAGES_CONFIG["Geografia"];
    const faseIndex = (q.fase ? q.fase : 1) - 1; // Fases de 1 a 10
    const monsterName = config.monsters[faseIndex % 10];

    // 1. Atualiza Cenário e Sprites na Arena
    document.getElementById("arena-bg").style.backgroundImage = `linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.3)), url('${config.bg}')`;
    document.getElementById("hero-img").src = config.hero;
    document.getElementById("class-tag").innerText = config.heroClass;
    
    // Imagem do monstro varia de acordo com o nome/fase
    document.getElementById("monster-img").src = `https://api.dicebear.com/7.x/bottts/svg?seed=${monsterName.replace(/\s+/g, '')}`;
    document.getElementById("monster-tag").innerText = `FASE ${faseIndex + 1}/10`;

    // 2. Atualiza Textos
    document.getElementById("current-stage").innerText = `MATÉRIA: ${q.subject.toUpperCase()} | FASE ${faseIndex + 1}/10`;
    document.getElementById("monster-name").innerText = `${monsterName.toUpperCase()} - FASE ${faseIndex + 1}/10`;
    document.getElementById("subject-tag").innerText = q.subject.toUpperCase();
    document.getElementById("question-text").innerText = q.question;
    document.getElementById("exp-reward").innerText = `+${q.exp} EXP`;

    // 3. Renderiza Botões de Opções
    const container = document.getElementById("options-container");
    container.innerHTML = "";
    const labels = ["A", "B", "C", "D"];

    q.options.forEach((opt, idx) => {
        const btn = document.createElement("button");
        btn.className = "opt-btn";
        btn.innerHTML = `<span style="color:#b8321d; margin-right:8px;">${labels[idx]}</span> ${opt}`;
        btn.onclick = () => answerQuestion(idx);
        container.appendChild(btn);
    });

    startTimer();
}

function startTimer() {
    timerSeconds = 30;
    startTime = Date.now();
    document.getElementById("timer").innerText = `⏱️ 00:${timerSeconds}`;
    
    timerInterval = setInterval(() => {
        timerSeconds--;
        document.getElementById("timer").innerText = `⏱️ 00:${timerSeconds < 10 ? '0' : ''}${timerSeconds}`;
        if (timerSeconds <= 0) {
            clearInterval(timerInterval);
            answerQuestion(-1);
        }
    }, 1000);
}

// API Call 2: Envia a escolha para validação e cálculo no Java
async function answerQuestion(selectedIndex) {
    clearInterval(timerInterval);
    const timeSpent = Math.round((Date.now() - startTime) / 1000);

    const payload = {
        questionId: currentQuestionData.id,
        selectedOption: selectedIndex,
        timeSpent: timeSpent
    };

    try {
        const response = await fetch('/api/answer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (result.correct) {
            triggerDamageAnimation("monster-img"); // Animação no Monstro
            alert(`Excelente! Resposta Correta.\nVocê causou ${result.damageDealt} de dano e ganhou ${result.gainedExp} EXP!`);
        } else {
            triggerDamageAnimation("hero-img"); // Animação no Herói
            alert("Resposta Incorreta! O Chefe atacou você.");
        }

        // Carrega a próxima fase/pergunta
        fetchCurrentQuestion();
    } catch (err) {
        console.error("Erro ao enviar resposta:", err);
    }
}

// Ativa a animação CSS de vibração/dano
function triggerDamageAnimation(elementId) {
    const el = document.getElementById(elementId);
    el.classList.add("take-damage");
    setTimeout(() => {
        el.classList.remove("take-damage");
    }, 400);
}

function useHelper(type) {
    alert(`Ajuda '${type}' utilizada!`);
}

// Inicia o jogo
fetchCurrentQuestion();
