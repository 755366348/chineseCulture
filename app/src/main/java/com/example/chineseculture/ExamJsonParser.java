package com.example.chineseculture;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExamJsonParser {

    private ExamJsonParser() {
    }

    public static ExamData parse(Context context, String assetName) throws Exception {
        String json = readAsset(context, assetName);
        JSONObject root = new JSONObject(json);
        String examDesc = root.optString("examDesc", "").trim();

        List<ExamData.Question> questions = new ArrayList<>();
        JSONArray questionArray = root.optJSONArray("questions");
        if (questionArray != null) {
            for (int i = 0; i < questionArray.length(); i++) {
                JSONObject item = questionArray.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String question = item.optString("question", "").trim();
                JSONArray answersArray = item.optJSONArray("answers");
                List<String> answers = new ArrayList<>();
                if (answersArray != null) {
                    for (int j = 0; j < answersArray.length(); j++) {
                        answers.add(answersArray.optString(j, "").trim());
                    }
                }
                questions.add(new ExamData.Question(question, answers));
            }
        }

        List<ExamData.RuleType> rules = new ArrayList<>();
        JSONArray rulerArray = root.optJSONArray("ruler");
        if (rulerArray != null) {
            for (int i = 0; i < rulerArray.length(); i++) {
                JSONObject ruleObj = rulerArray.optJSONObject(i);
                if (ruleObj == null) {
                    continue;
                }
                String type = ruleObj.optString("type", "").trim();
                JSONArray subArray = ruleObj.optJSONArray("subType");
                List<ExamData.SubType> subTypes = new ArrayList<>();
                if (subArray != null) {
                    for (int j = 0; j < subArray.length(); j++) {
                        JSONObject subObj = subArray.optJSONObject(j);
                        if (subObj == null) {
                            continue;
                        }
                        String name = subObj.optString("name", "").trim();
                        String desc = subObj.optString("desc", "").trim();
                        JSONArray scoreArray = subObj.optJSONArray("score");
                        int minScore = 0;
                        int maxScore = 0;
                        if (scoreArray != null && scoreArray.length() >= 2) {
                            minScore = scoreArray.optInt(0, 0);
                            maxScore = scoreArray.optInt(1, 0);
                        }
                        JSONArray personArray = subObj.optJSONArray("person");
                        List<ExamData.Person> persons = new ArrayList<>();
                        if (personArray != null) {
                            for (int k = 0; k < personArray.length(); k++) {
                                JSONObject personObj = personArray.optJSONObject(k);
                                if (personObj == null) {
                                    continue;
                                }
                                String personName = personObj.optString("name", "").trim();
                                String personDesc = personObj.optString("desc", "").trim();
                                persons.add(new ExamData.Person(personName, personDesc));
                            }
                        }
                        subTypes.add(new ExamData.SubType(name, desc, minScore, maxScore, persons));
                    }
                }
                rules.add(new ExamData.RuleType(type, subTypes));
            }
        }

        return new ExamData(examDesc, questions, rules);
    }

    private static String readAsset(Context context, String assetName) throws Exception {
        InputStream input = context.getAssets().open(assetName);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        input.close();
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
