public class Flashcard {
    String definition;
    int mistakes;

    Flashcard(String definition) {
        this.definition = definition;
        this.mistakes = 0;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }
}