package br.fortline.mestres;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

public class Server {

    // Modelo interno da pergunta (a resposta correta 'correct' fica protegida no servidor Java)
    static class Question {
        int id;
        String subject;
        int fase;
        String monster;
        String question;
        String[] options;
        int correct;
        int exp;

        public Question(int id, String subject, int fase, String monster, String question, String[] options, int correct, int exp) {
            this.id = id;
            this.subject = subject;
            this.fase = fase;
            this.monster = monster;
            this.question = question;
            this.options = options;
            this.correct = correct;
            this.exp = exp;
        }
    }

    // "Banco de dados" em memória para as perguntas
    private static final Map<Integer, Question> questionsDb = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // Inicializa o catálogo de perguntas das matérias
        initQuestions();

        // Obtém a porta definida pelo ambiente de hospedagem (Render) ou usa 8085 como padrão
        String envPort = System.getenv("PORT");
        int port = (envPort != null) ? Integer.parseInt(envPort) : 8085;

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        
        // Rotas do servidor
        server.createContext("/", new StaticHandler());
        server.createContext("/api/questions", new QuestionsHandler());
        server.createContext("/api/answer", new AnswerHandler());
        
        server.setExecutor(null);
        System.out.println("Servidor MESTRES DO CONHECIMENTO ativo na porta: " + port);
        server.start();
    }

    private static void initQuestions() {
        // Exemplos de perguntas configuradas por Matéria e Fase
        questionsDb.put(1, new Question(
            1, "Geografia", 1, "🌐 GUARDIÃO DO GLOBO", 
            "Qual é o maior rio do mundo em extensão?", 
            new String[]{"Rio Amazonas", "Rio Nilo", "Rio Mississípi", "Rio Yangtzé"}, 
            0, 58
        ));

        questionsDb.put(2, new Question(
            2, "História", 1, "📜 GUARDIÃO DO TEMPO", 
            "Em que ano ocorreu a Proclamação da República no Brasil?", 
            new String[]{"1500", "1822", "1889", "1945"}, 
            2, 65
        ));

        questionsDb.put(3, new Question(
            3, "Português", 1, "📖 GUARDIÃO DA GRAMÁTICA", 
            "Qual das palavras abaixo é um substantivo próprio?", 
            new String[]{"Cachorro", "Brasil", "Cidade", "Livro"}, 
            1, 50
        ));

        questionsDb.put(4, new Question(
            4, "Matemática", 1, "🔢 AUTÔMATO DA ADIÇÃO", 
            "Quanto é 7 x 8?", 
            new String[]{"48", "54", "56", "64"}, 
            2, 50
        ));

        questionsDb.put(5, new Question(
            5, "Ciências", 1, "🦠 BACTÉRIA GIGANTE", 
            "Qual órgão é responsável por bombear o sangue no corpo humano?", 
            new String[]{"Pulmão", "Cérebro", "Fígado", "Coração"}, 
            3, 50
        ));
    }

    // Manipulador para servir os arquivos estáticos da web (HTML, CSS, JS)
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws java.io.IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            // Procura o arquivo na pasta frontend/ ou na raiz do projeto
            File file = new File("frontend" + path);
            if (!file.exists()) {
                file = new File("." + path);
            }

            if (!file.exists() || file.isDirectory()) {
                String response = "404 - Arquivo Nao Encontrado";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            // Define o Content-Type apropriado para o navegador
            if (path.endsWith(".html")) {
                t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else if (path.endsWith(".css")) {
                t.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            } else if (path.endsWith(".js")) {
                t.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
            } else if (path.endsWith(".png")) {
                t.getResponseHeaders().set("Content-Type", "image/png");
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                t.getResponseHeaders().set("Content-Type", "image/jpeg");
            }

            t.sendResponseHeaders(200, file.length());
            OutputStream os = t.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }
    }

    // GET /api/questions -> Retorna as perguntas em formato JSON SEM revelar a opção correta
    static class QuestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws java.io.IOException {
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                StringBuilder json = new StringBuilder("[");
                int count = 0;
                for (Question q : questionsDb.values()) {
                    if (count > 0) json.append(",");
                    json.append("{")
                        .append("\"id\":").append(q.id).append(",")
                        .append("\"subject\":\"").append(q.subject).append("\",")
                        .append("\"fase\":").append(q.fase).append(",")
                        .append("\"monster\":\"").append(q.monster).append("\",")
                        .append("\"question\":\"").append(q.question).append("\",")
                        .append("\"exp\":").append(q.exp).append(",")
                        .append("\"options\":[");
                    for (int i = 0; i < q.options.length; i++) {
                        json.append("\"").append(q.options[i]).append("\"");
                        if (i < q.options.length - 1) json.append(",");
                    }
                    json.append("]}");
                    count++;
                }
                json.append("]");

                t.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }

    // POST /api/answer -> Processa e valida a resposta enviada pelo jogador no backend
    static class AnswerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws java.io.IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                InputStream is = t.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Parsing simples do JSON enviado pelo JavaScript
                int questionId = parseJsonInt(body, "questionId");
                int selectedOption = parseJsonInt(body, "selectedOption");
                int timeSpent = parseJsonInt(body, "timeSpent");

                Question q = questionsDb.get(questionId);
                boolean isCorrect = false;
                int gainedExp = 0;
                int damageDealt = 0;

                if (q != null && selectedOption == q.correct) {
                    isCorrect = true;
                    // Bônus de EXP para respostas rápidas (menos de 10 segundos)
                    gainedExp = (timeSpent > 0 && timeSpent < 10) ? q.exp + 15 : q.exp;
                    damageDealt = 35; // Dano provocado no chefe
                }

                String response = String.format(
                    "{\"correct\":%b, \"gainedExp\":%d, \"damageDealt\":%d}",
                    isCorrect, gainedExp, damageDealt
                );

                t.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }

        private int parseJsonInt(String json, String key) {
            try {
                String target = "\"" + key + "\":";
                int start = json.indexOf(target);
                if (start == -1) return -1;
                start += target.length();
                int end = json.indexOf(",", start);
                if (end == -1) end = json.indexOf("}", start);
                return Integer.parseInt(json.substring(start, end).trim());
            } catch (Exception e) {
                return -1;
            }
        }
    }
}