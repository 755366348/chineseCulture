package com.example.chineseculture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestDocxParser {

    private TestDocxParser() {
    }

    public static String extractText(Context context, String assetName) throws Exception {
        byte[] bytes = readAsset(context, assetName);
        String xml = readZipEntryText(bytes, "word/document.xml");
        if (xml == null) {
            return "";
        }
        String text = xml.replaceAll("<[^>]+>", "");
        text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        text = text.replace("\t", " ").replace("\r", " ").replace("\n", " ");
        return text.replaceAll("\\s+", " ").trim();
    }

    public static List<TestQuestion> parseQuestions(String text) {
        List<TestQuestion> questions = new ArrayList<>();
        Pattern blockPattern = Pattern.compile("(\\d{1,2})\\.\\s*(.*?)(?=\\d{1,2}\\.\\s|$)");
        Matcher matcher = blockPattern.matcher(text);
        while (matcher.find()) {
            String block = matcher.group(2).trim();
            if (!block.contains("A.") || !block.contains("B.") || !block.contains("C.")
                    || !block.contains("D.") || !block.contains("E.")) {
                continue;
            }
            String stem;
            int aIndex = block.indexOf("A.");
            if (aIndex <= 0) {
                continue;
            }
            stem = block.substring(0, aIndex).trim();
            Map<String, String> options = parseOptions(block.substring(aIndex));
            int number = Integer.parseInt(matcher.group(1));
            questions.add(new TestQuestion(
                    number,
                    stem,
                    options.get("A"),
                    options.get("B"),
                    options.get("C"),
                    options.get("D"),
                    options.get("E")
            ));
            if (questions.size() >= 35) {
                break;
            }
        }
        return questions;
    }

    private static Map<String, String> parseOptions(String text) {
        Map<String, String> map = new HashMap<>();
        Pattern optionPattern = Pattern.compile("([A-E])\\.\\s*(.*?)(?=[A-E]\\.\\s|$)");
        Matcher matcher = optionPattern.matcher(text);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2).trim());
        }
        return map;
    }

    public static Map<String, String> parseTypeDescriptions(String text, List<String> typeNames) {
        Map<String, Integer> positions = new HashMap<>();
        for (String type : typeNames) {
            int index = text.indexOf(type);
            if (index >= 0) {
                positions.put(type, index);
            }
        }
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(positions.entrySet());
        Collections.sort(sorted, Comparator.comparingInt(Map.Entry::getValue));

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            String type = sorted.get(i).getKey();
            int start = sorted.get(i).getValue() + type.length();
            int end = (i + 1 < sorted.size()) ? sorted.get(i + 1).getValue() : text.length();
            if (start >= 0 && end > start) {
                String desc = text.substring(start, end).trim();
                result.put(type, desc);
            }
        }
        return result;
    }

    public static Map<String, Bitmap> parseTypeImages(Context context, String assetName, List<String> typeNames)
            throws Exception {
        byte[] bytes = readAsset(context, assetName);
        String documentXml = readZipEntryText(bytes, "word/document.xml");
        String relsXml = readZipEntryText(bytes, "word/_rels/document.xml.rels");
        if (documentXml == null || relsXml == null) {
            return Collections.emptyMap();
        }

        Map<String, String> ridToTarget = parseRelationships(relsXml);
        Map<String, String> typeToRid = mapTypeToRid(documentXml, typeNames);
        Map<String, Bitmap> result = new HashMap<>();
        for (Map.Entry<String, String> entry : typeToRid.entrySet()) {
            String target = ridToTarget.get(entry.getValue());
            if (target == null) {
                continue;
            }
            String path = "word/" + target;
            byte[] imageBytes = readZipEntryBytes(bytes, path);
            if (imageBytes == null) {
                continue;
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                result.put(entry.getKey(), bitmap);
            }
        }
        return result;
    }

    private static Map<String, String> mapTypeToRid(String xml, List<String> typeNames) throws Exception {
        Map<String, String> map = new HashMap<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));

        String currentType = null;
        StringBuilder recent = new StringBuilder();
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if ("t".equals(name)) {
                    String text = parser.nextText();
                    if (text != null) {
                        recent.append(text.trim());
                        if (recent.length() > 30) {
                            recent.delete(0, recent.length() - 30);
                        }
                        for (String type : typeNames) {
                            if (recent.toString().contains(type)) {
                                currentType = type;
                                break;
                            }
                        }
                    }
                } else if ("blip".equals(name)) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attr = parser.getAttributeName(i);
                        if ("embed".equals(attr)) {
                            String rid = parser.getAttributeValue(i);
                            if (currentType != null && !map.containsKey(currentType)) {
                                map.put(currentType, rid);
                            }
                        }
                    }
                }
            }
            event = parser.next();
        }
        return map;
    }

    private static Map<String, String> parseRelationships(String xml) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("Id=\"(rId\\d+)\"[^>]*Target=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    private static byte[] readAsset(Context context, String assetName) throws Exception {
        InputStream input = context.getAssets().open(assetName);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
        input.close();
        return output.toByteArray();
    }

    private static String readZipEntryText(byte[] zipBytes, String entryName) throws Exception {
        byte[] data = readZipEntryBytes(zipBytes, entryName);
        if (data == null) {
            return null;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] readZipEntryBytes(byte[] zipBytes, String entryName) throws Exception {
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entryName.equals(entry.getName())) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                zis.close();
                return output.toByteArray();
            }
        }
        zis.close();
        return null;
    }
}
