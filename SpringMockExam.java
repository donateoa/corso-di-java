import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SpringMockExam {

    // Modello domanda
    static class Question {
        String id;
        String text;
        List<String> options;
        Set<Integer> correctIndexes; // 0-based: 0 = A, 1 = B, ecc.
        String explanation;

        Question(String id, String text, List<String> options, Set<Integer> correctIndexes) {
            this(id, text, options, correctIndexes, null);
        }

        Question(String id, String text, List<String> options, Set<Integer> correctIndexes, String explanation) {
            this.id = id;
            this.text = text;
            this.options = options;
            this.correctIndexes = correctIndexes;
            this.explanation = explanation;
        }

        boolean isMultipleChoice() {
            return correctIndexes.size() > 1;
        }
    }

    public static void main(String[] args) {
        List<Question> questions;

        String testName = args.length > 0 ? args[0].trim() : null;
        if (testName != null && !testName.isEmpty()) {
            try {
                questions = loadQuestionsFromFile(testName);
                if (questions.isEmpty()) {
                    System.out.println("Nessuna domanda caricata dal test '" + testName + "'. Uso domande interne.");
                    questions = buildQuestions();
                } else {
                    System.out.println("Caricato test '" + testName + "' (" + questions.size() + " domande).");
                }
            } catch (IOException e) {
                System.out.println("Impossibile caricare il test '" + testName + "': " + e.getMessage());
                System.out.println("Uso le domande interne.");
                questions = buildQuestions();
            }
        } else {
            questions = buildQuestions();
        }

        Scanner scanner = new Scanner(System.in);
        int total = questions.size();
        int correctCount = 0;
        int wrongCount = 0;

        System.out.println("=== Spring Certification Mock Exam ===");
        System.out.println("Domande su Spring Core, Spring Boot 2.7 e 3.x");
        System.out.println("Formato risposta: ");
        System.out.println("  - domanda singola:    A");
        System.out.println("  - risposta multipla:  A,C  oppure AC");
        System.out.println("----------------------------------------\n");

        int index = 1;
        for (Question q : questions) {
            System.out.println("Domanda " + index + "/" + total + "  [" + q.id + "]");
            printQuestion(q);

            Set<Integer> answerIndexes = readAnswer(scanner, q.options.size());

            boolean correct = answerIndexes.equals(q.correctIndexes);
            if (correct) {
                System.out.println("✅ Corretto!" + formatExplanation(q));
                correctCount++;
            } else {
                System.out.println("❌ Sbagliato." + formatExplanation(q));
                System.out.print("   Risposta corretta: ");
                printCorrectAnswers(q);
                wrongCount++;
            }

            int answered = correctCount + wrongCount;
            double percentage = answered == 0 ? 0.0 : (correctCount * 100.0 / answered);
            System.out.printf("   Progresso: %d giuste, %d sbagliate, %.1f%% corrette%n",
                    correctCount, wrongCount, percentage);
            System.out.println("----------------------------------------\n");

            index++;
        }

        System.out.println("=== RISULTATO FINALE ===");
        System.out.println("Totale domande: " + total);
        System.out.println("Corrette:        " + correctCount);
        System.out.println("Sbagliate:       " + wrongCount);
        double finalPercentage = total == 0 ? 0.0 : (correctCount * 100.0 / total);
        System.out.printf("Percentuale:      %.1f%%%n", finalPercentage);
        System.out.println("=========================");
    }

    private static List<Question> buildQuestions() {
        List<Question> list = new ArrayList<>();

        // Q1 - Ciclo di vita bean
        list.add(new Question(
                "Q1",
                "Nel ciclo di vita di un bean Spring, quale affermazione è corretta?",
                List.of(
                        "Ogni bean è sempre di scope prototype.",
                        "I metodi annotati con @PostConstruct vengono eseguiti dopo l’inizializzazione del bean.",
                        "Spring non supporta metodi di distruzione personalizzati.",
                        "@Autowired viene eseguito prima del costruttore."),
                Set.of(1) // B
        ));

        // Q2 - Boot 2.7 vs 3.x
        list.add(new Question(
                "Q2",
                "Passando da Spring Boot 2.7 a 3.x, quale cambiamento è vero?",
                List.of(
                        "È obbligatorio il passaggio a Jakarta EE (pacchetti jakarta.*).",
                        "Spring Boot 3 non supporta Tomcat embedded.",
                        "Non è più possibile usare i profili Spring.",
                        "@Configuration è stata rimossa."),
                Set.of(0) // A
        ));

        // Q3 - @Configuration vs @Component
        list.add(new Question(
                "Q3",
                "Quando è più opportuno usare @Configuration invece di @Component?",
                List.of(
                        "Quando la classe dichiara metodi @Bean che devono essere gestiti con proxy CGLIB.",
                        "Quando non vuoi registrare nessun bean nel container.",
                        "Solo se usi esclusivamente XML.",
                        "Quando devi creare bean sempre di tipo prototype."),
                Set.of(0) // A
        ));

        // Q4 - Sicurezza: mass assignment
        list.add(new Question(
                "Q4",
                "Un controller REST espone un endpoint che fa binding diretto di un DTO senza validazione. Qual è il rischio principale?",
                List.of(
                        "SQL Injection automatica.",
                        "Mass Assignment: un client può popolare campi che non dovrebbero essere modificabili.",
                        "Le password vengono criptate due volte.",
                        "Nessun rischio: Spring filtra automaticamente i campi in eccesso."),
                Set.of(1) // B
        ));

        // Q5 - @Configuration vs @AutoConfiguration (multiple)
        list.add(new Question(
                "Q5",
                "Quali affermazioni su @Configuration e @AutoConfiguration in Spring Boot 3 sono corrette?",
                List.of(
                        "@AutoConfiguration viene elaborata in base al meccanismo di auto-config e può essere condizionata alla presenza/assenza di altri bean.",
                        "@Configuration definisce una configurazione esplicita registrata sempre, se la classe è nel component scan.",
                        "@AutoConfiguration è deprecata e non va più usata.",
                        "@Configuration può essere usata solo nei test."),
                Set.of(0, 1) // A e B
        ));

        // Q6 - Spring Security 6
        list.add(new Question(
                "Q6",
                "In Spring Security 6 (Spring Boot 3.x), qual è il modo raccomandato per configurare HTTP security?",
                List.of(
                        "Estendere WebSecurityConfigurerAdapter.",
                        "Definire un bean SecurityFilterChain usando la lambda DSL.",
                        "Configurare tutto in XML.",
                        "Non si può più personalizzare la sicurezza."),
                Set.of(1) // B
        ));

        // Q7 - Feign Client
        list.add(new Question(
                "Q7",
                "Come si specifica una configurazione personalizzata per un Feign Client con Spring Cloud OpenFeign?",
                List.of(
                        "Usando @FeignClient(configuration = MyFeignConfig.class).",
                        "Usando @FeignClient(feignConfig = MyFeignConfig.class).",
                        "Usando @FeignMapping su ogni metodo.",
                        "Usando @FeignInterceptor a livello di classe."),
                Set.of(0) // A
        ));

        // Q8 - Actuator e sicurezza (multiple)
        list.add(new Question(
                "Q8",
                "In Spring Boot Actuator (2.7/3.x), quali endpoint è consigliato NON esporre pubblicamente in produzione per motivi di sicurezza?",
                List.of(
                        "/info",
                        "/health",
                        "/env",
                        "/beans"),
                Set.of(2, 3) // /env e /beans
        ));

        // Q9 - AOP
        list.add(new Question(
                "Q9",
                "Negli aspetti AOP di Spring, quale tipo ti permette di accedere a informazioni sulla chiamata al metodo target?",
                List.of(
                        "JoinPoint (o ProceedingJoinPoint).",
                        "TargetMethod.",
                        "SpringInvocation.",
                        "ProxyPoint."),
                Set.of(0) // A
        ));

        // Q10 - WebFlux
        list.add(new Question(
                "Q10",
                "Quali sono i tipi reattivi principali usati da Spring WebFlux?",
                List.of(
                        "CompletableFuture e Stream.",
                        "Mono e Flux.",
                        "Observable e Single.",
                        "Task e IObservable."),
                Set.of(1) // B
        ));

        // Q11 - SpringApplication.run
        list.add(new Question(
                "Q11",
                "Cosa fa SpringApplication.run(...) in un'app Spring Boot?",
                List.of(
                        "Avvia il contesto Spring, configura l’environment e, se web, il server embedded.",
                        "Registra solo i bean senza avviare nulla.",
                        "Avvia solo la parte di sicurezza Spring Security.",
                        "Esegue soltanto la scansione dei componenti senza creare i bean."),
                Set.of(0) // A
        ));

        // Q12 - @RestController vs @Controller
        list.add(new Question(
                "Q12",
                "Qual è la differenza principale tra @RestController e @Controller?",
                List.of(
                        "@RestController combina @Controller e @ResponseBody, restituendo di default JSON/XML del body.",
                        "@Controller non può gestire richieste HTTP.",
                        "Sono completamente equivalenti.",
                        "@RestController può essere usato solo con WebFlux."),
                Set.of(0) // A
        ));

        // Q13 - Buco di sicurezza actuator
        list.add(new Question(
                "Q13",
                "Quale di questi è un potenziale \"buco di sicurezza\" se non protetto?",
                List.of(
                        "Esporre l’endpoint /env dell’Actuator su internet senza autenticazione.",
                        "Usare @Value per leggere una property.",
                        "Usare @Transactional su un servizio.",
                        "Registrare un bean con @Component."),
                Set.of(0) // A
        ));

        // Q14 - @Transactional
        list.add(new Question(
                "Q14",
                "Come si gestisce correttamente una transazione che coinvolge più repository?",
                List.of(
                        "Mettere @Transactional sul metodo del service che coordina le operazioni.",
                        "Mettere @Transactional su ogni metodo del repository.",
                        "Mettere @Transactional sui metodi del controller.",
                        "È obbligatoria la configurazione XML."),
                Set.of(0) // A
        ));

        // Q15 - Jackson
        list.add(new Question(
                "Q15",
                "Quale classe di default usa Spring Boot per serializzazione/deserializzazione JSON?",
                List.of(
                        "GsonParser",
                        "Jackson ObjectMapper",
                        "JsonReader",
                        "JsonMarshaller"),
                Set.of(1) // B
        ));

        return list;
    }

    private static void printQuestion(Question q) {
        System.out.println(q.text);
        char letter = 'A';
        for (String opt : q.options) {
            System.out.println("  " + letter + ") " + opt);
            letter++;
        }
        if (q.isMultipleChoice()) {
            System.out.println("  [Risposta multipla: inserisci es. A,C oppure AC]");
        } else {
            System.out.println("  [Risposta singola]");
        }
        System.out.print("La tua risposta: ");
    }

    private static Set<Integer> readAnswer(Scanner scanner, int optionCount) {
        while (true) {
            String line = scanner.nextLine();
            try {
                return parseAnswerLetters(line, optionCount);
            } catch (IllegalArgumentException ex) {
                System.out.print("Risposta non valida (" + ex.getMessage() + "). Riprova (es. A o A,C): ");
            }
        }
    }

    private static void printCorrectAnswers(Question q) {
        List<String> letters = new ArrayList<>();
        for (Integer idx : q.correctIndexes) {
            char c = (char) ('A' + idx);
            letters.add(String.valueOf(c));
        }
        System.out.println(String.join(",", letters));
    }

    private static String formatExplanation(Question q) {
        if (q.explanation == null || q.explanation.isBlank()) {
            return "";
        }
        return " " + q.explanation;
    }

    private static Set<Integer> parseAnswerLetters(String input, int optionCount) {
        if (optionCount <= 0) {
            throw new IllegalArgumentException("nessuna opzione disponibile");
        }
        String normalized = input.trim().toUpperCase(Locale.ITALY);
        normalized = normalized.replace(";", ",").replace(" ", "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("nessuna risposta indicata");
        }

        String[] parts = normalized.contains(",") ? normalized.split(",") : normalized.split("");
        Set<Integer> indexes = new LinkedHashSet<>();
        for (String p : parts) {
            if (p.isEmpty())
                continue;
            char c = p.charAt(0);
            if (c < 'A' || c >= 'A' + optionCount) {
                throw new IllegalArgumentException("lettera fuori range: " + c);
            }
            indexes.add(c - 'A');
        }

        if (indexes.isEmpty()) {
            throw new IllegalArgumentException("nessuna risposta valida indicata");
        }
        return indexes;
    }

    private static List<Question> loadQuestionsFromFile(String testName) throws IOException {
        Path file = Paths.get("tests", testName + ".txt");
        if (!Files.exists(file)) {
            System.out.println("File test non trovato: " + file.toAbsolutePath());
            return Collections.emptyList();
        }

        List<String> lines = Files.readAllLines(file);
        List<Question> result = new ArrayList<>();

        String id = null;
        String text = null;
        String correctRaw = null;
        String explanation = null;
        List<String> options = new ArrayList<>();
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (id != null || text != null || !options.isEmpty() || correctRaw != null || explanation != null) {
                    result.add(buildQuestionFromBlock(id, text, options, correctRaw, explanation, file, lineNumber));
                    id = null;
                    text = null;
                    correctRaw = null;
                    explanation = null;
                    options = new ArrayList<>();
                }
                continue;
            }
            if (trimmed.startsWith("#")) {
                continue;
            }

            int colonPos = trimmed.indexOf(":");
            if (colonPos <= 0) {
                throw new IOException("Formato non valido alla riga " + lineNumber + " del file " + file);
            }

            String key = trimmed.substring(0, colonPos).trim().toLowerCase(Locale.ITALY);
            String value = trimmed.substring(colonPos + 1).trim();

            switch (key) {
                case "id" -> id = value;
                case "text" -> text = value;
                case "option" -> options.add(stripOptionPrefix(value));
                case "options" -> {
                    String[] optParts = value.split("\\|");
                    for (String opt : optParts) {
                        String cleaned = opt.trim();
                        if (!cleaned.isEmpty()) {
                            options.add(stripOptionPrefix(cleaned));
                        }
                    }
                }
                case "correct" -> correctRaw = value;
                case "answer" -> explanation = value;
                default -> {
                    // ignora chiavi sconosciute
                }
            }
        }

        if (id != null || text != null || !options.isEmpty() || correctRaw != null || explanation != null) {
            result.add(buildQuestionFromBlock(id, text, options, correctRaw, explanation, file, lineNumber));
        }

        return result;
    }

    private static Question buildQuestionFromBlock(String id, String text, List<String> options,
                                                   String correctRaw, String explanation,
                                                   Path file, int lineNumber) throws IOException {
        if (id == null || text == null || options.isEmpty() || correctRaw == null) {
            throw new IOException("Blocco incompleto nel file " + file + " vicino alla riga " + lineNumber);
        }
        Set<Integer> correctIndexes;
        try {
            correctIndexes = parseAnswerLetters(correctRaw, options.size());
        } catch (IllegalArgumentException ex) {
            throw new IOException("Risposte corrette non valide per la domanda " + id + ": " + ex.getMessage(), ex);
        }
        return new Question(id, text, List.copyOf(options), correctIndexes, explanation);
    }

    private static String stripOptionPrefix(String value) {
        String cleaned = value.replaceFirst("^[A-Za-z]\\)\\s*", "");
        cleaned = cleaned.replaceFirst("^\\d+\\)\\s*", "");
        return cleaned.trim();
    }
}
