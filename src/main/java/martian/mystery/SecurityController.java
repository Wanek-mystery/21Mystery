package martian.mystery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SecurityController {

    int[] arrOffset = {-1,1,-2,0,3,-2,-1,-2,1,-1,2,-2,2,-1,-3,2,2,-1,-1};
    public boolean getQuestion(int number) {
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
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr15).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr16).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr17).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr18).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr19).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr20).split("!")));
            arrQuestions.addAll(Arrays.asList(GetContextClass.getContext().getResources().getString(R.string.awr21).split("!")));
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
            case 15: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr15).split("!"); break;
            case 16: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr16).split("!"); break;
            case 17: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr17).split("!"); break;
            case 18: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr18).split("!"); break;
            case 19: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr19).split("!"); break;
            case 20: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr20).split("!"); break;
            case 21: cipherAnswers = GetContextClass.getContext().getResources().getString(R.string.awr21).split("!"); break;
        }

        String[] answers = new String[cipherAnswers.length];
        for(int i = 0; i < cipherAnswers.length; i++) {
            String[] words = cipherAnswers[i].split(" ");
            String[] charAnswer = new String[cipherAnswers.length];
            for(int j = 0; j < words.length; j++) {
                char[] charOneWord = words[j].toCharArray();
                for (int x = 0; x < charOneWord.length; x++) {
                    charOneWord[x] += arrOffset[x];
                }
                words[j] = String.valueOf(charOneWord).concat(" ");
                charAnswer[i] += words[j];
            }
            answers[i] = charAnswer[i].trim().replaceAll("null","");
        }
        return answers;
    }
}
