package de.nordakademie.iaa_multiple_choice.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import de.nordakademie.iaa_multiple_choice.domain.Answer;
import de.nordakademie.iaa_multiple_choice.domain.Exam;
import de.nordakademie.iaa_multiple_choice.domain.Question;
import de.nordakademie.iaa_multiple_choice.domain.QuestionType;
import de.nordakademie.iaa_multiple_choice.domain.exceptions.ExamNotFoundException;
import de.nordakademie.iaa_multiple_choice.service.AnswerService;
import de.nordakademie.iaa_multiple_choice.service.ExamService;
import de.nordakademie.iaa_multiple_choice.service.QuestionService;
import de.nordakademie.iaa_multiple_choice.web.util.LecturerRequired;
import de.nordakademie.iaa_multiple_choice.web.util.LoginRequired;
import lombok.Getter;
import lombok.Setter;

@LoginRequired
@LecturerRequired
public class QuestionAction extends BaseAction {
    private static final long serialVersionUID = 6954059649443931989L;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ExamService examService;
    @Autowired
    private AnswerService answerService;

    @Getter
    @Setter
    private Question question;

    @Getter
    @Setter
    private Long examId;

    @Getter
    @Setter
    private Long questionId;

    @Getter
    @Setter
    private String rawAnswerTextsSc[]; // SC
    // private String/boolean selected;
    @Getter
    @Setter
    private String rawAnswerTextsMc[]; // MC
    // private String/boolean selected;
    @Getter
    @Setter
    private Integer sc;
    @Getter
    @Setter
    private ArrayList<Integer> mc = new ArrayList<>();
    @Getter
    @Setter
    private String fitb;

    @Getter
    @Setter
    private String questionType;

    public String createQuestion() {
        final Exam exam = examService.find(examId);
        if (exam.getId() == null) {
            throw new ExamNotFoundException();
        }
        return SUCCESS;
    }

    public String deleteQuestion() {
        questionService.deleteQuestion(question.getId());
        return SUCCESS;
    }

    public String editQuestion() {
        question = questionService.find(questionId);
        return SUCCESS;
    }

    public String saveQuestion() {
        questionService.createQuestion(question);
        question.setAnswers(new HashSet<>());
        final Exam exam = examService.find(examId);
        if (questionType.equals("sc")) {
            question.setType(QuestionType.SINGLE_CHOICE);
            for (int i = 0; i < rawAnswerTextsSc.length; i++) {
                final String rawAnswerText = rawAnswerTextsSc[i];
                final Answer answer = new Answer(rawAnswerText, i == sc);
                question.addAnswer(answer);
                answerService.createAnswer(answer);
            }
        } else if (questionType.equals("mc")) {
            question.setType(QuestionType.MULTIPLE_CHOICE);
            for (int i = 0; i < rawAnswerTextsMc.length; i++) {
                final String rawAnswerText = rawAnswerTextsMc[i];
                final Answer answer = new Answer(rawAnswerText, mc.contains(i));
                question.addAnswer(answer);
                answerService.createAnswer(answer);
            }
        } else if (questionType == null) {
            final Pattern p = Pattern.compile("\\[(.*?)\\]");
            final Matcher m = p.matcher(question.getText());
            question.setType(QuestionType.FILL_IN_THE_BLANK);
            while (m.find()) {
                final String rawAnswerText = m.group(1);
                final Answer answer = new Answer(rawAnswerText, true);
                question.addAnswer(answer);
                answerService.createAnswer(answer);
            }
            question.setText(question.getText().replaceAll("\\[(.*?)\\]", "[]"));
        }

        exam.addQuestion(question);
        examService.updateExam(exam);
        return SUCCESS;

    }

    public String updateQuestion() {
        final Exam exam = examService.find(examId);
        question = questionService.find(questionId);
        if (question.getType() == QuestionType.SINGLE_CHOICE) {
            int i = 0;
            for (Answer answer : question.getAnswers()) {
                try {
                    final String rawAnswerText = rawAnswerTextsSc[i];
                    answer.setText(rawAnswerText);
                    answer.setRightAnswer(i == sc);
                    i++;
                    answerService.updateAnswer(answer);
                } catch (IndexOutOfBoundsException e) {
                    answerService.deleteAnswer(answer.getId());
                }
            }
        } else if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
            int i = 0;
            for (Answer answer : question.getAnswers()) {
                try {
                    final String rawAnswerText = rawAnswerTextsMc[i];
                    answer.setText(rawAnswerText);
                    answer.setRightAnswer(mc.contains(i));
                    i++;
                    answerService.updateAnswer(answer);
                } catch (IndexOutOfBoundsException e) {
                    answerService.deleteAnswer(answer.getId());
                }
            }
        } else if (questionType == null) {
            // delete old answers?
            final Pattern p = Pattern.compile("\\[(.*?)\\]");
            final Matcher m = p.matcher(question.getText());
            while (m.find()) {
                final String rawAnswerText = m.group(1);
                final Answer answer = new Answer(rawAnswerText, true);
                question.addAnswer(answer);
                answerService.createAnswer(answer);
            }
            question.setText(question.getText().replaceAll("\\[(.*?)\\]", "[]"));
        }
        // question.setId(questionId);
        // question.setAnswers(question.getAnswers());
        // question.setType(question.getType());
        questionService.updateQuestion(question);
        return SUCCESS;

    }

    public void validateSaveQuestion() {
        if (question.getText() == "") {
            addFieldError("question.text", getText("validation.questionMissing"));
        }
        if (question.getPoints() == null) {
            addFieldError("question.points", getText("validation.pointsMissing"));
        }
    }
}
