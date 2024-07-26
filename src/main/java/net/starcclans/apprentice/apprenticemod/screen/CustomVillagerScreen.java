package net.starcclans.apprentice.apprenticemod.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;


import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomVillagerScreen extends Screen {

    private String userInput = "";
    private final List<String> botResponses = new ArrayList<>();
    private boolean endConversation = false;
    private int moodPercentage = 100;
    private String userName;

    private final List<List<String>> allVerbVectors;
    private Map<String, List<String>> topics = new HashMap<>();

    public CustomVillagerScreen(Text title) {
        super(title);
        allVerbVectors = loadListFromFile("verbVectors.txt");
        topics = loadMapFromFile("topics.txt");
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, "Welcome to Chatbot", width / 2, 20, 0xFFFFFF);

        int textHeight = 30;
        drawStringWithShadow(matrices, textRenderer, "You > " + userInput, 30, textHeight, 0xFFFFFF);

        int responseHeight = textHeight + 20;
        for (String response : botResponses) {
            drawStringWithShadow(matrices, textRenderer, "Xenon > " + response, 30, responseHeight, 0x55FFFF);
            responseHeight += 10;
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // Enter key
            sendMessage();
        } else if (keyCode == 259 && userInput.length() > 0) { // Backspace key
            userInput = userInput.substring(0, userInput.length() - 1);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr == '\n' || chr == '\r') {
            sendMessage();
        } else {
            userInput += chr;
        }
        return super.charTyped(chr, modifiers);
    }

    private void sendMessage() {
        botResponses.clear();

        if (!endConversation) {
            System.out.println("User Input: " + userInput); // Debug: Print user input

            moodPercentage = manageMood(userInput, moodPercentage);

            if (!userInput.equals(userInput.toLowerCase())) {
                userInput = userInput.toLowerCase();
            }

            List<List<String>> sentences = convertToSentences(userInput);
            System.out.println("Sentences: " + sentences); // Debug: Print sentences

            sentences = removeDuplicateSentences(sentences);
            System.out.println("Unique Sentences: " + sentences); // Debug: Print unique sentences

            List<Integer> result = compareAndOutput(sentences, allVerbVectors);
            System.out.println("Compare and Output Result: " + result); // Debug: Print result

            List<List<Integer>> formattedOutput = formatOutput(result);
            System.out.println("Formatted Output: " + formattedOutput); // Debug: Print formatted output

            for (int i = 0; i < sentences.size(); i++) {
                int pattern = comparePattern(formattedOutput.get(i));
                System.out.println("Pattern: " + pattern); // Debug: Print pattern
                botResponses.add(botResponse(pattern, userName, sentences.get(i)));
            }

            if (userInput.equalsIgnoreCase("bye")) {
                endConversation = true;
            }

            userInput = "";
        }
    }

    private static List<List<String>> removeDuplicateSentences(List<List<String>> sentences) {
        List<List<String>> uniqueSentences = new ArrayList<>();
        Set<String> seenSentences = new HashSet<>();

        for (List<String> sentence : sentences) {
            String sentenceString = String.join(" ", sentence).toLowerCase();
            if (!seenSentences.contains(sentenceString)) {
                uniqueSentences.add(sentence);
                seenSentences.add(sentenceString);
            }
        }

        return uniqueSentences;
    }

    private static int manageMood(String userInput, int currentMood) {
        int decreaseMultiplier = 20; // 20% decrease for each word in all caps

        // Check if the user input contains all caps
        for (char ch : userInput.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                currentMood -= decreaseMultiplier;
            }
        }

        // Ensure mood does not go below 0
        currentMood = Math.max(0, currentMood);

        return currentMood;
    }

    private static List<List<String>> convertToSentences(String userInput) {
        List<List<String>> stringArrays = new ArrayList<>();
        List<String> currentSentence = new ArrayList<>();

        String[] words = userInput.split("\\s+");

        for (String word : words) {
            if (word.matches("[,.?!]")) {
                if (!currentSentence.isEmpty()) {
                    stringArrays.add(new ArrayList<>(currentSentence));
                    currentSentence.clear();
                }
            } else {
                currentSentence.add(word);
            }
        }

        if (!currentSentence.isEmpty()) {
            stringArrays.add(new ArrayList<>(currentSentence));
        }

        return stringArrays;
    }

    private static String botResponse(int pattern, String userName, List<String> sentence) {
        List<String> rearrangedSentence = new ArrayList<>(Collections.nCopies(sentence.size(), ""));
        StringBuilder rearrangedStr = new StringBuilder();

        switch (pattern) {
            case 0:
                return "Sorry to tell you man, but I don't understand what you typed";
            case 1:
            case 2:
            case 3:
            case 4:
                if (sentence.size() > 1) {
                    rearrangedSentence.set(0, sentence.get(1));
                    rearrangedSentence.set(1, sentence.get(0));
                    for (int word = 2; word < sentence.size(); ++word) {
                        rearrangedSentence.set(word, sentence.get(word));
                    }
                    rearrangedSentence.forEach(word -> rearrangedStr.append(word).append(" "));
                    return rearrangedStr.toString().trim();
                } else {
                    return "Sorry, I couldn't parse your sentence.";
                }
            default:
                return "I'm not sure how to respond to that.";
        }
    }

    private List<Integer> compareAndOutput(List<List<String>> sentences, List<List<String>> allVerbVectors) {
        List<Integer> result = new ArrayList<>();
        int sentenceIndex = 0;

        for (List<String> sentence : sentences) {
            for (int j = 0; j < sentence.size(); ++j) {
                boolean found = false;

                for (int k = 0; k < allVerbVectors.size(); ++k) {
                    if (allVerbVectors.get(k).contains(sentence.get(j))) {
                        result.add(sentenceIndex + 1);
                        result.add(j + 1);
                        result.add(k + 1);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // If word is not found in verbVectors, check topics
                    boolean topicFound = false;
                    for (String topic : topics.keySet()) {
                        if (topics.get(topic).contains(sentence.get(j))) {
                            result.add(sentenceIndex + 1);
                            result.add(j + 1);
                            result.add(allVerbVectors.size() + topics.keySet().stream().toList().indexOf(topic) + 1);
                            topicFound = true;
                            break;
                        }
                    }

                    if (!topicFound) {
                        // Prompt user to add to topics
                        String newTopic = getUserInput("Please provide a category for the word: " + sentence.get(j));
                        if (!newTopic.isEmpty()) {
                            topics.computeIfAbsent(newTopic, k -> new ArrayList<>()).add(sentence.get(j));
                            saveMapToFile(topics, "topics.txt");
                            result.add(sentenceIndex + 1);
                            result.add(j + 1);
                            result.add(allVerbVectors.size() + topics.keySet().stream().toList().indexOf(newTopic) + 1);
                        } else {
                            result.add(sentenceIndex + 1);
                            result.add(j + 1);
                            result.add(0);
                        }
                    }
                }
            }
            sentenceIndex++;
        }

        return result;
    }

    private static List<List<Integer>> formatOutput(List<Integer> outputArray) {
        List<List<Integer>> formattedOutput = new ArrayList<>();
        List<Integer> currentRow = new ArrayList<>();

        for (int i = 0; i < outputArray.size(); i += 3) {
            if (i > 0 && !Objects.equals(outputArray.get(i), outputArray.get(i - 3))) {
                formattedOutput.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
            currentRow.add(outputArray.get(i + 2));
        }

        if (!currentRow.isEmpty()) {
            formattedOutput.add(currentRow);
        }

        return formattedOutput;
    }

    private static int comparePattern(List<Integer> row) {
        int pattern = 0;

        if (row.size() >= 2) {
            String patternStr = row.get(0).toString() + row.get(1).toString() + row.get(2).toString();
            pattern = switch (patternStr) {
                case "160", "260" -> 1;
                case "316" -> 2;
                case "560", "460" -> 3;
                case "610" -> 4;
                default -> 0;
            };
        }

        return pattern;
    }

    private static void writeToFile(String input, String filename) {
        try (FileWriter outputFile = new FileWriter(filename + ".txt", true)) {
            outputFile.write(input + " ++ " + getCurrentTime() + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Error: Unable to open file " + filename);
        }
    }

    private static List<String> readLineFromFile(String filePath, int lineNumber) {
        List<String> words = new ArrayList<>();

        try (BufferedReader inputFile = new BufferedReader(new FileReader(filePath))) {
            for (int i = 0; i <= lineNumber; ++i) {
                String line = inputFile.readLine();
                if (i == lineNumber && line != null) {
                    words.addAll(Arrays.asList(line.split("\\s+")));
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to open the file.");
        }

        return words;
    }

    private static String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static void saveListToFile(List<List<String>> list, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (List<String> sublist : list) {
                writer.write(String.join(",", sublist));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to write to file " + filename);
        }
    }

    private static List<List<String>> loadListFromFile(String filename) {
        List<List<String>> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(Arrays.asList(line.split(",")));
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to read file " + filename);
        }
        return list;
    }

    private static void saveMapToFile(Map<String, List<String>> map, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                writer.write(entry.getKey() + "=" + String.join(",", entry.getValue()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to write to file " + filename);
        }
    }

    private static Map<String, List<String>> loadMapFromFile(String filename) {
        Map<String, List<String>> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    map.put(parts[0], Arrays.asList(parts[1].split(",")));
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Unable to read file " + filename);
        }
        return map;
    }

    private String getUserInput(String prompt) {
        // Prompt the user with a message and get input
        // This is a placeholder for the actual implementation
        System.out.println(prompt);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim();
    }
}
