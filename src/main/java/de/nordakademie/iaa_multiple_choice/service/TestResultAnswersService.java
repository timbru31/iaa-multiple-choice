package de.nordakademie.iaa_multiple_choice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.nordakademie.iaa_multiple_choice.domain.TestResultAnswers;
import de.nordakademie.iaa_multiple_choice.domain.TestResultAnswersRepository;

/**
 * Service for answers of test.
 * 
 * @author Jens Gottwald
 */
@Service
public class TestResultAnswersService {
    @Autowired
    private TestResultAnswersRepository testResultAnswersRepository;

    @Transactional
    public void createTestResultAnswers(final TestResultAnswers testResultAnswers) {
        testResultAnswersRepository.createTestResultAnswers(testResultAnswers);
    }
}
