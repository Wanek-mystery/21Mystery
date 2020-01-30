package martian.mystery.controller;

import martian.mystery.R;
import martian.mystery.exceptions.ErrorOnServerException;
import martian.mystery.exceptions.NoInternetException;

public class QuestionAnswerController {

    public boolean checkAnswer(String answer) throws NoInternetException, ErrorOnServerException {
        answer = answer.trim().toLowerCase();
        String[] keysAnswers = getAnswers();
        for(int i = 0; i < keysAnswers.length; i++) {
            if(answer.equals(keysAnswers[i])) {
                return true;
            }
        }
        return false;
    }
    public String getQuestion() {
            switch (Progress.getInstance().getLevel()) {
                case 1: return GetContextClass.getContext().getResources().getString(R.string.qst1);
                case 2: return GetContextClass.getContext().getResources().getString(R.string.qst2);
                case 3: return GetContextClass.getContext().getResources().getString(R.string.qst3);
                case 4: return GetContextClass.getContext().getResources().getString(R.string.qst4);
                case 5: return GetContextClass.getContext().getResources().getString(R.string.qst5);
                case 6: return GetContextClass.getContext().getResources().getString(R.string.qst6);
                case 7: return GetContextClass.getContext().getResources().getString(R.string.qst7);
                case 8: return GetContextClass.getContext().getResources().getString(R.string.qst8);
                case 9: return GetContextClass.getContext().getResources().getString(R.string.qst9);
                case 10: return GetContextClass.getContext().getResources().getString(R.string.qst10);
                case 11: return GetContextClass.getContext().getResources().getString(R.string.qst11);
                case 12: return GetContextClass.getContext().getResources().getString(R.string.qst12);
                case 13: return GetContextClass.getContext().getResources().getString(R.string.qst13);
                case 14: return GetContextClass.getContext().getResources().getString(R.string.qst14);
                case 15: return GetContextClass.getContext().getResources().getString(R.string.qst15);
                case 16: return GetContextClass.getContext().getResources().getString(R.string.qst16);
                case 17: return GetContextClass.getContext().getResources().getString(R.string.qst17);
                case 18: return GetContextClass.getContext().getResources().getString(R.string.qst18);
                case 19: return GetContextClass.getContext().getResources().getString(R.string.qst19);
                case 20: return GetContextClass.getContext().getResources().getString(R.string.qst20);
                case 21: return GetContextClass.getContext().getResources().getString(R.string.qst21);
            }
        return "Question";
    }
    private String[] getAnswers() {
        SecurityController securityController = new SecurityController();
        String[] answers = null;
        answers = securityController.getAnswer(Progress.getInstance().getLevel());
        return answers;
    }
}
