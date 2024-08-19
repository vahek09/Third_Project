import java.io.*;
import java.util.*;

public class Main {
    private static final Map<String, Flashcard> flashcards = new LinkedHashMap<>();
    private static final ArrayList<String> log = new ArrayList<>();



    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        checkArgs(args,false);

        while (true) {
            logAndPrint("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String action = scanner.nextLine().trim();
            log.add(action.toLowerCase());

            switch (action.toLowerCase()) {
                case "add":
                    addCard(scanner);
                    break;
                case "remove":
                    removeCard(scanner);
                    break;
                case "import":
                    importCards(scanner);
                    break;
                case "export":
                    exportCards(scanner);
                    break;
                case "ask":
                    askDefinitions(scanner);
                    break;
                case "log":
                    saveLog(scanner);
                    break;
                case "hardest card":
                    printHardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
                case "exit":
                    logAndPrint("Bye bye!");
                    checkArgs(args, true);
                    return;
                default:
                    logAndPrint("Unknown action. Please try again.");
            }
        }
    }

    /**
     * Checking Arguments.
     */
    private static void checkArgs(String[] args, boolean exit) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-import") && !exit && i + 1 < args.length) {
                importCards(args[i + 1]);
            }
            if (args[i].equals("-export") && exit && i + 1 < args.length) {
                exportCards(args[i + 1]);
            }
        }
    }

    /**
     * Prompt the user for a new card to add, consisting of a term and its definition.
     * Check if the given term or definition already exists in the collection of cards.
     * If it already exists, inform the user and return without adding the card.
     * Otherwise, create a new Card object with the given term and definition, and add it to the collection of cards.
     */
    private static void addCard(Scanner scanner) {
        logAndPrint("The card:");
        String term = scanner.nextLine().trim();
        log.add(term);

        if (term.isEmpty()) {
            logAndPrint("The term cannot be empty. Please enter a valid term.");
            return;
        }

        if (flashcards.containsKey(term)) {
            logAndPrint("The card \"" + term + "\" already exists.");
            return;
        }

        logAndPrint("The definition of the card:");
        String definition = scanner.nextLine().trim();
        log.add(definition);

        if (definition.isEmpty()) {
            logAndPrint("The definition cannot be empty. Please enter a valid term.");
            return;
        }

        for (Flashcard card : flashcards.values()) {
            if (card.definition.equals(definition)) {
                logAndPrint("The definition \"" + definition + "\" already exists.");
                return;
            }
        }

        flashcards.put(term, new Flashcard(definition));
        logAndPrint("The pair (\"" + term + "\":\"" + definition + "\") has been added.");
    }


    /**
     * Removes a card from the collection, based on the term entered by the user.
     */
    private static void removeCard(Scanner scanner) {
        logAndPrint("Which card?");
        String termToRemove = scanner.nextLine().trim();
        log.add(termToRemove);

        if (flashcards.containsKey(termToRemove)) {
            flashcards.remove(termToRemove);
            logAndPrint("The card has been removed.");
        } else {
            logAndPrint("Can't remove \"" + termToRemove + "\": there is no such card.");
        }
    }
    /**
     * Helper method to import card from a file add adds them to the list of cards, when there is "-import" in arguments detected.
     * If file does not exist create a new one, and add there.If a card with the same term as an imported
     * card already exists, its definition is updated with the imported card's definition.
     * */
    private static void importCards(Scanner scanner) throws Exception {
        logAndPrint("File name:");
        String fileName = scanner.nextLine().trim();
        log.add(fileName);
        try {
            importCards(fileName);
        } catch (Exception e) {
            logAndPrint("File not found: " + fileName);
            throw new Exception(e.getMessage());
        }
    }
    /**
     * Imports cards from a file and adds them to the existing list of cards,if file doesn't exist, create it.
     * If a card with the same term as an imported
     * card already exists, its definition is updated with the imported card's definition.
     */
    private static void importCards(String fileName) throws Exception {
        logAndPrint("File name:");
        log.add(fileName);
        File file = new File(fileName);
        if (!file.exists()) {
            logAndPrint("File not found.");
            throw new FileNotFoundException("File not found: " + fileName);
        }
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid line format: " + line);
                }
                    String term = parts[0].trim();
                    String definition = parts[1].trim();
                    int mistakes = Integer.parseInt(parts[2].trim());

                    Flashcard card = new Flashcard(definition);
                    card.mistakes = mistakes;
                    flashcards.put(term, card);
                    count++;
            }
        } catch (FileNotFoundException e) {
            logAndPrint("Error: File not found or could not be created.");
            throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            logAndPrint("Error writing to the file: ");
            throw new Exception(e.getMessage());
        }
        logAndPrint(count + " cards have been loaded.");
    }

    /**
     * Export cards to a file and adds them, when there is "-export" in argument input,if file doesn't exist, create it.
     */
    private static void exportCards(Scanner scanner) throws FileNotFoundException {
        logAndPrint("File name:");
        String fileName = scanner.nextLine().trim();
        log.add(fileName);
        try {
            exportCards(fileName);
        } catch (Exception e) {
            logAndPrint("File not found: " + fileName);
            throw new FileNotFoundException(e.getMessage());
        }
    }

    /**
     * Export cards to a file and adds them,if file doesn't exist, create it.
     */
    private static void exportCards(String filename) throws IOException{
        logAndPrint("File name:");
        log.add(filename);
        int count = 0;
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, Flashcard> entry : flashcards.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue().definition + ":" + entry.getValue().mistakes);
                count++;
            }
        }catch (FileNotFoundException e) {
        logAndPrint("Error: File not found or could not be created.");
            throw new FileNotFoundException(e.getMessage());
        } catch (IOException e) {
        logAndPrint("Error writing to the file: ");
        throw new IOException(e.getMessage());
         }
        logAndPrint(count + " cards have been saved.");
    }

    /**
     * Asks the user to provide the definitions of a certain number of flashcards
     * and checks the correctness of the provided definitions against the expected definitions.
     */
    private static void askDefinitions(Scanner scanner){
        logAndPrint("How many times to ask?");
       int count = Integer.parseInt(scanner.nextLine().trim());
        log.add(String.valueOf(count));

        List<String> terms = new ArrayList<>(flashcards.keySet());
         Collections.shuffle(terms);

        Map<String, String> reverseFlashcards = new HashMap<>();
        for (Map.Entry<String, Flashcard> entry : flashcards.entrySet()) {
            reverseFlashcards.put(entry.getValue().definition, entry.getKey());
        }

        for (int i = 0; i < count; i++) {
            String term = terms.get(i % terms.size());
            String correctDefinition = flashcards.get(term).definition;

            logAndPrint("Print the definition of \"" + term + "\":");
            String userDefinition = scanner.nextLine();
            log.add(userDefinition);

            if (correctDefinition.equals(userDefinition)){
                logAndPrint("Correct!");
            }else{
                String correctTerm = reverseFlashcards.get(userDefinition);
                if (correctTerm != null){
                    logAndPrint("Wrong. The right answer is \"" + correctDefinition + "\", but your definition is correct for \"" + correctTerm + "\".");
                } else {
                    logAndPrint("Wrong. The right answer is \"" + correctDefinition + "\".");
                }
                flashcards.get(term).mistakes++;
            }
        }
    }

    private static void saveLog(Scanner scanner) {
        logAndPrint("File name:");
        String fileName = scanner.nextLine().trim();
        log.add(fileName);
        logAndPrint("The log has been saved.");
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (String entry : log) {
                writer.println(entry);
            }
            writer.println("Log saved on: " + new Date());
        } catch (IOException e) {
            logAndPrint("Error writing to the file: " + e.getMessage());
        }
    }
    /**
     * Resets the mistake count of all cards in the list of cards.
     */
    private static void resetStats() {
        for (Flashcard card : flashcards.values()) {
            card.mistakes = 0;
        }
        logAndPrint("Card statistics have been reset.");
    }

    private static void logAndPrint(String message) {
        System.out.println(message);
        log.add(message);
    }

    private static void printHardestCard() {
        int maxMistakes = 0;
        for (Flashcard card : flashcards.values()) {
            if (card.mistakes > maxMistakes) {
                maxMistakes = card.mistakes;
            }
        }

        if (maxMistakes == 0) {
            logAndPrint("There are no cards with errors.");
        } else {
            List<String> hardestCards = new ArrayList<>();
            for (Map.Entry<String, Flashcard> entry : flashcards.entrySet()) {
                if (entry.getValue().mistakes == maxMistakes) {
                    hardestCards.add(entry.getKey());
                }
            }

            StringBuilder formattedCards = new StringBuilder();
            for (int i = 0; i < hardestCards.size(); i++) {
                formattedCards.append("\"").append(hardestCards.get(i)).append("\"");
                if (i < hardestCards.size() - 1) {
                    formattedCards.append(", ");
                }
            }

            if (hardestCards.size() == 1) {
                logAndPrint(String.format("The hardest card is \"%s\". You have %d errors answering it.", hardestCards.getFirst(), maxMistakes));
            } else {
                logAndPrint(String.format("The hardest cards are %s. You have %d errors answering them.", formattedCards, maxMistakes));
            }
        }
    }
}