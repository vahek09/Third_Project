package flash;
import java.util.Scanner;

public class Main {
    private static final FlashcardManager flashcardManager = new FlashcardManager();
    private static final LogManager logManager = new LogManager();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        flashcardManager.checkArgs(args, false, logManager);

        while (true) {
            logManager.logAndPrint("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String action = scanner.nextLine().trim();
            logManager.addToLog(action.toLowerCase());

            switch (action.toLowerCase()) {
                case "add":
                    flashcardManager.addCard(scanner, logManager);
                    break;
                case "remove":
                    flashcardManager.removeCard(scanner, logManager);
                    break;
                case "import":
                    flashcardManager.importCards(scanner, logManager);
                    break;
                case "export":
                    flashcardManager.exportCards(scanner, logManager);
                    break;
                case "ask":
                    flashcardManager.askDefinitions(scanner, logManager);
                    break;
                case "log":
                    logManager.saveLog(scanner);
                    break;
                case "hardest card":
                    flashcardManager.printHardestCard(logManager);
                    break;
                case "reset stats":
                    flashcardManager.resetStats(logManager);
                    break;
                case "exit":
                    logManager.logAndPrint("Bye bye!");
                    flashcardManager.checkArgs(args, true, logManager);
                    return;
                default:
                    logManager.logAndPrint("Unknown action. Please try again.");
            }
        }
    }
}
