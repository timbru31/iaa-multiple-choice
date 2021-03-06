package de.nordakademie.iaa_multiple_choice.web;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.nordakademie.iaa_multiple_choice.domain.Exam;
import de.nordakademie.iaa_multiple_choice.domain.Student;
import de.nordakademie.iaa_multiple_choice.domain.ExamResult;
import de.nordakademie.iaa_multiple_choice.domain.exceptions.StudentNotEnrolledException;
import de.nordakademie.iaa_multiple_choice.service.ExamService;
import de.nordakademie.iaa_multiple_choice.service.ExamResultService;
import de.nordakademie.iaa_multiple_choice.service.UserService;
import de.nordakademie.iaa_multiple_choice.web.util.LoginRequired;
import de.nordakademie.iaa_multiple_choice.web.util.StudentRequired;
import lombok.Getter;
import lombok.Setter;

/**
 * Action for enrolling to an exam.
 * 
 * @author Tim Brust
 */
@LoginRequired
@StudentRequired
public class EnrollAction extends BaseSessionAction {
    private static final long serialVersionUID = -2887663909719799155L;
    private static final Logger logger = LogManager.getLogger(EnrollAction.class.getName());
    @Autowired
    private ExamService examService;
    @Autowired
    private ExamResultService examResultService;
    @Autowired
    private UserService userService;
    @Getter
    @Setter
    private Long examId;
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private Exam exam;
    @Getter
    @Setter
    private Student student;

    public String enrollStudent() {
        final LocalDateTime now = LocalDateTime.now();
        final ExamResult examResult = new ExamResult();
        examResult.setExam(exam);
        examResult.setStudent(student);
        examResult.setStartTime(now);
        examResultService.createExamResult(examResult);
        exam.addExamResult(examResult);
        student.addExamResult(examResult);
        examService.updateExam(exam);
        userService.updateUser(student);
        return SUCCESS;
    }

    @Override
    public void validate() {
        exam = examService.find(examId);
        student = (Student) getUser();
        if (!exam.hasParticipant(student)) {
            logger.warn("The student {} tried to enroll to the exam {}, but he is not enlisted for this exam!",
                    student.getEmail(), exam.getName());
            throw new StudentNotEnrolledException();
        }
        if (!exam.hasQuestions()) {
            addFieldError("examInvalid", getText("validation.examHasNoQuestions"));
            return;
        }
        if (token == null || token.isEmpty() || !exam.getToken(student).equals(token)) {
            addFieldError("token", getText("validation.tokenInvalid"));
        }
        if (student.hasTakenExam(exam)) {
            addFieldError("token", getText("validation.tokenUsed"));
        }
    }
}
