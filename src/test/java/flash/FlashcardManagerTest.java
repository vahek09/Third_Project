package flash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlashcardManagerTest {
    private FlashcardManager flashcardManager;
    private LogManager logManager;

    @BeforeEach
    void setUp() {
        flashcardManager = new FlashcardManager();
        logManager = Mockito.mock(LogManager.class); // Mock LogManager
    }

    @Test
    @DisplayName("Add Term and Definition Test")
    void testAddCard() {
        //given
        Scanner scanner = new Scanner("Term1\nDefinition1");
        //when
        flashcardManager.addCard(scanner, logManager);
        //then
        assertTrue(flashcardManager.flashcards.containsKey("Term1"));
        assertEquals("Definition1",flashcardManager.flashcards.get("Term1").getDefinition());
    }

    @Test
    @DisplayName("Duplicate Term Test")
    void testAddCard_DuplicateTerm() {
        //given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        Scanner scanner = new Scanner("Term1\nDefinition2");

        //when
        flashcardManager.addCard(scanner, logManager);

        //then
        verify(logManager).logAndPrint("The card \"Term1\" already exists.");
        assertEquals(1, flashcardManager.flashcards.size());
        assertEquals("Definition1", flashcardManager.flashcards.get("Term1").getDefinition());
    }

    @Test
    @DisplayName("Duplicate Definition Test")
    void testAddCard_DuplicateDefinition() {
        //given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        Scanner scanner = new Scanner("Term2\nDefinition1");

        //when
        flashcardManager.addCard(scanner, logManager);

        //then
        InOrder inOrder = inOrder(logManager);
        inOrder.verify(logManager).logAndPrint("The card:");
        inOrder.verify(logManager).addToLog("Term2");
        inOrder.verify(logManager).logAndPrint("The definition of the card:");
        inOrder.verify(logManager).addToLog("Definition1");
        inOrder.verify(logManager).logAndPrint("The definition \"Definition1\" already exists.");
        assertEquals(1, flashcardManager.flashcards.size());
    }

    @Test
    @DisplayName("Remove existing Card Test")
    void removeCard() {
        //given
        flashcardManager.flashcards.put("TermToRemove1", new Flashcard("DefinitionToRemove1"));
        flashcardManager.flashcards.put("TermToRemove2", new Flashcard("DefinitionToRemove2"));
        flashcardManager.flashcards.put("TermToRemove3", new Flashcard("DefinitionToRemove3"));
        Scanner scanner = new Scanner("TermToRemove2");

        //when
        flashcardManager.removeCard(scanner, logManager);

        //then
        verify(logManager).logAndPrint("Which card?");
        verify(logManager).addToLog("TermToRemove2");
        verify(logManager).logAndPrint("The card has been removed.");
        assertFalse(flashcardManager.flashcards.containsKey("TermToRemove2"));
        assertEquals(2, flashcardManager.flashcards.size());
    }

    @Test
    @DisplayName("Remove Card that doesn't exist Test")
    void removeCard_NoExistCard() {
        //given
        Scanner scanner = new Scanner("Term");
        //when
        flashcardManager.removeCard(scanner, logManager);
        //then
        verify(logManager).logAndPrint("Which card?");
        verify(logManager).addToLog("Term");
        verify(logManager).logAndPrint("Can't remove \"Term\": there is no such card.");
        assertTrue(flashcardManager.flashcards.isEmpty());
    }

    @Test
    @DisplayName("Resetting Stats Test")
    void testResetStats() {
        //given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        flashcardManager.flashcards.put("Term2", new Flashcard("Definition2"));
        flashcardManager.flashcards.get("Term1").setMistakes(5);
        flashcardManager.flashcards.get("Term2").setMistakes(3);

        //when
        flashcardManager.resetStats(logManager);

        //then
        assertEquals(0, flashcardManager.flashcards.get("Term1").getMistakes());
        assertEquals(0, flashcardManager.flashcards.get("Term2").getMistakes());
        verify(logManager).logAndPrint("Card statistics have been reset.");
    }
    @Test
    @DisplayName("Having Multiple Hardest Cards Test")
    public void testPrintHardestCard() {
        // Given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        flashcardManager.flashcards.put("Term2", new Flashcard("Definition2"));
        flashcardManager.flashcards.put("Term3", new Flashcard("Definition3"));
        flashcardManager.flashcards.get("Term1").setMistakes(5);
        flashcardManager.flashcards.get("Term2").setMistakes(5);
        flashcardManager.flashcards.get("Term3").setMistakes(5);

        // When
        flashcardManager.printHardestCard(logManager);

        // Then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(logManager, times(1)).logAndPrint(argumentCaptor.capture());
        assertEquals("The hardest cards are \"Term1\", \"Term2\", \"Term3\". You have 5 errors answering them.", argumentCaptor.getValue());
    }


}