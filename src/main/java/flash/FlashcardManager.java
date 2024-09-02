package flash;

import java.io.*;
import java.util.*;

public class FlashcardManager {
    final Map<String, Flashcard> flashcards = new LinkedHashMap<>();

    /**
     * Checking Arguments.
     */
    public void checkArgs(String[] args, boolean exit, LogManager logManager) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-import") && !exit && i + 1 < args.length) {
                importCards(args[i + 1], logManager);
            }
            if (args[i].equals("-export") && exit && i + 1 < args.length) {
                exportCards(args[i + 1], logManager);
            }
        }
    }

    /**
     * Prompt the user for a new card to add, consisting of a term and its definition.
     * Check if the given term or definition already exists in the collection of cards.
     * If it already exists, inform the user and return without adding the card.
     * Otherwise, create a new Card object with the given term and definition, and add it to the collection of cards.
     */
    public void addCard(Scanner scanner, LogManager logManager) {
        logManager.logAndPrint("The card:");
        String term = scanner.nextLine().trim();
        logManager.addToLog(term);

        if (term.isEmpty()) {
            logManager.logAndPrint("The term cannot be empty. Please enter a valid term.");
            return;
        }

        if (flashcards.containsKey(term)) {
            logManager.logAndPrint("The card \"" + term + "\" already exists.");
            return;
        }

        logManager.logAndPrint("The definition of the card:");
        String definition = scanner.nextLine().trim();
        logManager.addToLog(definition);

        if (definition.isEmpty()) {
            logManager.logAndPrint("The definition cannot be empty. Please enter a valid term.");
            return;
        }

        for (Flashcard card : flashcards.values()) {
            if (card.definition.equals(definition)) {
                logManager.logAndPrint("The definition \"" + definition + "\" already exists.");
                return;
            }
        }

        flashcards.put(term, new Flashcard(definition));
        logManager.logAndPrint("The pair (\"" + term + "\":\"" + definition + "\") has been added.");
    }

    /**
     * Removes a card from the collection, based on the term entered by the user.
     */

    public void removeCard(Scanner scanner, LogManager logManager) {
        logManager.logAndPrint("Which card?");
        String termToRemove = scanner.nextLine().trim();
        logManager.addToLog(termToRemove);

        if (flashcards.containsKey(termToRemove)) {
            flashcards.remove(termToRemove);
            logManager.logAndPrint("The card has been removed.");
        } else {
            logManager.logAndPrint("Can't remove \"" + termToRemove + "\": there is no such card.");
        }
    }

    /**
     * Helper method to import card from a file add adds them to the list of cards, when there is "-import" in arguments detected.
     * If file does not exist create a new one, and add there.If a card with the same term as an imported
     * card already exists, its definition is updated with the imported card's definition.
     * */
    public void importCards(Scanner scanner, LogManager logManager) throws Exception {
        logManager.logAndPrint("File name:");
        String fileName = scanner.nextLine().trim();
        logManager.addToLog(fileName);
        try {
            importCards(fileName, logManager);
        } catch (Exception e) {
            logManager.logAndPrint("File not found: " + fileName);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Imports cards from a file and adds them to the existing list of cards,if file doesn't exist, create it.
     * If a card with the same term as an imported
     * card already exists, its definition is updated with the imported card's definition.
     */
    public void importCards(String fileName, LogManager logManager) throws Exception {
        if (logManager != null) {
            logManager.logAndPrint("File name:");
            logManager.addToLog(fileName);
        }

        File file = new File(fileName);
        if (!file.exists()) {
            if (logManager != null) logManager.logAndPrint("File not found.");
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
        } catch (Exception e) {
            if (logManager != null) logManager.logAndPrint("Error writing to the file.");
            throw new Exception(e.getMessage());
        }

        if (logManager != null) logManager.logAndPrint(count + " cards have been loaded.");
    }

    /**
     * Export cards to a file and adds them, when there is "-export" in argument input,if file doesn't exist, create it.
     */
    public void exportCards(Scanner scanner, LogManager logManager) throws FileNotFoundException {
        logManager.logAndPrint("File name:");
        String fileName = scanner.nextLine().trim();
        logManager.addToLog(fileName);

        try {
            exportCards(fileName, logManager);
        } catch (FileNotFoundException e) {
            logManager.logAndPrint("File not found: " + fileName);
            throw e; // Re-throw the exception to propagate it to the caller
        } catch (Exception e) {
            logManager.logAndPrint("An unexpected error occurred: " + e.getMessage());
            throw new RuntimeException(e); // Handle other unexpected exceptions
        }
    }

    /**
     * Export cards to a file and adds them,if file doesn't exist, create it.
     */
    public void exportCards(String filename, LogManager logManager) throws IOException {
        if (logManager != null) {
            logManager.logAndPrint("File name:");
            logManager.addToLog(filename);
        }

        int count = 0;
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, Flashcard> entry : flashcards.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue().definition + ":" + entry.getValue().mistakes);
                count++;
            }
        } catch (IOException e) {
            if (logManager != null) logManager.logAndPrint("Error writing to the file.");
            throw new IOException(e.getMessage());
        }

        if (logManager != null) logManager.logAndPrint(count + " cards have been saved.");
    }


    /**
     * Asks the user to provide the definitions of a certain number of flashcards
     * and checks the correctness of the provided definitions against the expected definitions.
     */
    public void askDefinitions(Scanner scanner, LogManager logManager) {
        logManager.logAndPrint("How many times to ask?");
        String input = scanner.nextLine().trim();

        int count;
        try {
            count = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            logManager.logAndPrint("Invalid number format: " + input);
            return;
        }

        logManager.addToLog(String.valueOf(count));

        List<String> terms = new ArrayList<>(flashcards.keySet());
        Collections.shuffle(terms);

        Map<String, String> reverseFlashcards = new HashMap<>();
        for (Map.Entry<String, Flashcard> entry : flashcards.entrySet()) {
            reverseFlashcards.put(entry.getValue().definition, entry.getKey());
        }

        for (int i = 0; i < count; i++) {
            String term = terms.get(i % terms.size());
            String correctDefinition = flashcards.get(term).definition;

            logManager.logAndPrint("Print the definition of \"" + term + "\":");
            String userDefinition = scanner.nextLine();
            logManager.addToLog(userDefinition);

            if (correctDefinition.equals(userDefinition)) {
                logManager.logAndPrint("Correct!");
            } else {
                String correctTerm = reverseFlashcards.get(userDefinition);
                if (correctTerm != null) {
                    logManager.logAndPrint("Wrong. The right answer is \"" + correctDefinition + "\", but your definition is correct for \"" + correctTerm + "\".");
                } else {
                    logManager.logAndPrint("Wrong. The right answer is \"" + correctDefinition + "\".");
                }
                flashcards.get(term).mistakes++;
            }
        }
    }


    public void printHardestCard(LogManager logManager) {
        int maxMistakes = 0;
        for (Flashcard card : flashcards.values()) {
            if (card.mistakes > maxMistakes) {
                maxMistakes = card.mistakes;
            }
        }

        if (maxMistakes == 0) {
            logManager.logAndPrint("There are no cards with errors.");
        } else {
            List<String> hardestCards = new ArrayList<>();
            for (Map.Entry<String, Flashcard> entry : flashcards.entrySet()) {
                if (entry.getValue().mistakes == maxMistakes) {
                    hardestCards.add(entry.getKey());
                }
            }

            if (hardestCards.size() == 1) {
                logManager.logAndPrint("The hardest card is \"" + hardestCards.get(0) + "\". You have " + maxMistakes + " errors answering it.");
            } else {
                logManager.logAndPrint("The hardest cards are \"" + String.join("\", \"", hardestCards) + "\". You have " + maxMistakes + " errors answering them.");
            }
        }
    }

    /**
     * Resets the mistake count of all cards in the list of cards.
     */
    public void resetStats(LogManager logManager) {
        for (Flashcard card : flashcards.values()) {
            card.mistakes = 0;
        }
        logManager.logAndPrint("Card statistics have been reset.");
    }
}
