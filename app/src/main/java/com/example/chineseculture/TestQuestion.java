package com.example.chineseculture;

public class TestQuestion {
    private final int number;
    private final String text;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final String optionD;
    private final String optionE;

    public TestQuestion(int number, String text,
                        String optionA, String optionB, String optionC, String optionD, String optionE) {
        this.number = number;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.optionE = optionE;
    }

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getOptionE() {
        return optionE;
    }
}
