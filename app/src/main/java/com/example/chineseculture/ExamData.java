package com.example.chineseculture;

import java.util.List;

public class ExamData {
    public final String examDesc;
    public final List<Question> questions;
    public final List<RuleType> rules;

    public ExamData(String examDesc, List<Question> questions, List<RuleType> rules) {
        this.examDesc = examDesc;
        this.questions = questions;
        this.rules = rules;
    }

    public static class Question {
        public final String question;
        public final List<String> answers;

        public Question(String question, List<String> answers) {
            this.question = question;
            this.answers = answers;
        }
    }

    public static class RuleType {
        public final String type;
        public final List<SubType> subTypes;

        public RuleType(String type, List<SubType> subTypes) {
            this.type = type;
            this.subTypes = subTypes;
        }
    }

    public static class SubType {
        public final String name;
        public final String desc;
        public final int minScore;
        public final int maxScore;
        public final List<Person> persons;

        public SubType(String name, String desc, int minScore, int maxScore, List<Person> persons) {
            this.name = name;
            this.desc = desc;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.persons = persons;
        }
    }

    public static class Person {
        public final String name;
        public final String desc;

        public Person(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }
}
