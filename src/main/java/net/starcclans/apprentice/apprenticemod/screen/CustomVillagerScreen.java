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
    private List<String> botResponses = new ArrayList<>();
    private boolean endConversation = false;
    private int moodPercentage = 100;
    private String userName;

    private List<List<String>> allVerbVectors = new ArrayList<>();

    public CustomVillagerScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        // Additional initialization code for your custom UI
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
            moodPercentage = manageMood(userInput, moodPercentage);
            List<List<String>> sentences = convertToSentences(userInput);
            List<Integer> result = compareAndOutput(sentences, allVerbVectors);
            List<List<Integer>> formattedOutput = formatOutput(result);
            List<Integer> arrPattern = formattedOutput.stream().map(CustomVillagerScreen::comparePattern).toList();

            for (int pattern : arrPattern) {
                botResponses.addAll(botResponse(pattern, userName, sentences));
            }

            if (userInput.equalsIgnoreCase("bye")) {
                endConversation = true;
            }

            userInput = "";
        }
    }

    private static int manageMood(String userInput, int currentMood) {
        int decreaseMultiplier = 20; // 20% decrease for each word in all caps
        int maxMood = 100;

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
            }
            currentSentence.add(word);
        }

        if (!currentSentence.isEmpty()) {
            stringArrays.add(new ArrayList<>(currentSentence));
        }

        return stringArrays;
    }

    private static List<String> botResponse(int pattern, String userName, List<List<String>> sentences) {
        List<String> response = new ArrayList<>();

        for (List<String> sentence : sentences) {
            List<String> rearrangedSentence = new ArrayList<>(Collections.nCopies(sentence.size(), ""));
            StringBuilder rearrangedStr = new StringBuilder();

            switch (pattern) {
                case 0:
                    response.add("Sorry to tell you man, but I don't understand what you typed");
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    rearrangedSentence.set(0, sentence.get(1));
                    rearrangedSentence.set(1, sentence.get(0));
                    for (int word = 2; word < sentence.size(); ++word) {
                        rearrangedSentence.set(word, sentence.get(word));
                    }
                    rearrangedSentence.forEach(word -> rearrangedStr.append(word).append(" "));
                    response.add(rearrangedStr.toString().trim());
                    break;
                default:
                    response.add("I'm not sure how to respond to that.");
                    break;
            }
        }

        return response;
    }

    private static List<Integer> compareAndOutput(List<List<String>> sentences, List<List<String>> allVerbVectors) {
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
                    result.add(sentenceIndex + 1);
                    result.add(j + 1);
                    result.add(0);
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
}
