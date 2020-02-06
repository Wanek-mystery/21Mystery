package martian.mystery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import martian.mystery.R;

public class SecurityController {

    int offSets[][] = {{-2, 1, -4, -6, 2, -6, -4, -2, 8, 4, -5, 4, -1, -2, 7, -4, 6, -5, -6},
            {4, -3, -1, 8, 6, -6, 4, 8, 6, -4, 7, -3, -1, 4, -5, 4, -2, 7, 5},
            {0, -1, 8, -7, 1, -1, -9, 2, 4, 3, -7, 4, -6, -4, -4, 5, -6, 3, 1},
            {-1, -8, 3, -6, -8, -8, 0, -9, 0, -2, 5, -5, 4, -7, -9, -6, 3, -7, -9},
            {1, -6, 4, -6, 9, 1, 4, -6, 4, 2, -7, -5, 4, 2, -7, 2, 6, -3, -5},
            {5, -6, 2, 0, -8, 1, -9, -5, -7, 3, -9, -1, 2, 3, 9, 8, -4, 4, 3},
            {-5, 2, -8, 9, -3, 6, -1, -9, 8, -8, -1, 7, -3, -4, -2, 9, 2, 1, 8},
            {9, -1, 9, 8, 1, 3, 9, -1, -5, 5, -7, 2, 3, -8, 1, 2, -8, -8, 1},
            {-8, -5, 5, 3, -7, 2, -9, 9, 0, 9, 0, 1, 1, -6, 3, -8, -5, -7, 0},
            {-8, 2, 0, 3, -6, 3, -8, -9, 2, -7, 2, -6, -8, -6, 5, -6, -8, 1, 2},
            {-4, 3, 5, -2, -1, 9, -2, -4, 7, -1, -8, 0, -2, 6, 8, 0, -2, -9, -1},
            {2, 9, 7, -1, -8, -5, 3, 1, 9, -1, 0, 1, -9, -1, 8, 9, 8, -1, -8},
            {1, 3, 0, -8, 6, -4, -6, -5, 3, -7, 3, 5, -3, 5, -6, 3, 4, 8, -1},
            {8, 4, -7, -4, 8, 0, -2, 7, 8, -3, 7, 7, -1, -9, 0, -3, 5, -5, 0}};
    public boolean getQuestion(int number) { // метод против взлома (проверка ответов на одинаковость)
        ArrayList<String> arrQuestions = new ArrayList<>();
        if(number == 21) {
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr1).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr2).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr3).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr4).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr5).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr6).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr7).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr8).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr9).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr10).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr11).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr12).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr13).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr14).split("!")));
        }
        if(arrQuestions.size() == new HashSet<>(arrQuestions).size()) return true;
        else return false;
    }
    public String[] getAnswer(int countAnswer) {
        String[] cipherAnswers = {""};
        switch (countAnswer) {
            case 1: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr1).split("!"); break;
            case 2: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr2).split("!"); break;
            case 3: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr3).split("!"); break;
            case 4: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr4).split("!"); break;
            case 5: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr5).split("!"); break;
            case 6: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr6).split("!"); break;
            case 7: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr7).split("!"); break;
            case 8: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr8).split("!"); break;
            case 9: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr9).split("!"); break;
            case 10: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr10).split("!"); break;
            case 11: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr11).split("!"); break;
            case 12: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr12).split("!"); break;
            case 13: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr13).split("!"); break;
            case 14: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr14).split("!"); break;
        }

        String[] answers = new String[cipherAnswers.length];
        for(int i = 0; i < cipherAnswers.length; i++) { // уровень ответа
            String[] words = cipherAnswers[i].split(" "); // разделяем на слова
            String[] charAnswer = new String[cipherAnswers.length];
            for(int j = 0; j < words.length; j++) { // уровень слова
                char[] charOneWord = words[j].toCharArray();
                for (int x = 0; x < charOneWord.length; x++) { // уровень символа
                    charOneWord[x] -= offSets[countAnswer-1][x];
                }
                words[j] = String.valueOf(charOneWord).concat(" ");
                charAnswer[i] += words[j]; // здесь можно обрезать

            }
            answers[i] = charAnswer[i].trim().replaceAll("null","");
        }
        return answers;
    }
}
