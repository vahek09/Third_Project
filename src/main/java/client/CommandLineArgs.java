package client;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {

    @Parameter(names = "-t", description = "Type of request (get, set, delete)")
    private String type;

    @Parameter(names = "-k", description = "key of args")
    private String key;

    @Parameter(names = "-v", description = "Value of args")
    private String value;

    @Parameter(names = "-in", description = "Input file containing requests")
    private String inputFile;


    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public CommandLineArgs() {
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getInputFile(){
        return inputFile;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }



    public void setType(String type) {
        this.type = type;
    }
}
