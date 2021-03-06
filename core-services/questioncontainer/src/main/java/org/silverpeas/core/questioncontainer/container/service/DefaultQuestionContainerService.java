/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.questioncontainer.container.service;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.questioncontainer.answer.dao.AnswerDAO;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.answer.service.AnswerService;
import org.silverpeas.core.questioncontainer.container.dao.QuestionContainerDAO;
import org.silverpeas.core.questioncontainer.container.model.Comment;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerRuntimeException;
import org.silverpeas.core.questioncontainer.question.dao.QuestionDAO;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.questioncontainer.question.service.QuestionService;
import org.silverpeas.core.questioncontainer.result.dao.QuestionResultDAO;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.result.service.QuestionResultService;
import org.silverpeas.core.questioncontainer.score.dao.ScoreDAO;
import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.questioncontainer.score.model.ScorePK;
import org.silverpeas.core.questioncontainer.score.service.ScoreService;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * Stateless service to manage question container.
 * @author neysseri
 */
@Singleton
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class DefaultQuestionContainerService
    implements QuestionContainerService, ComponentInstanceDeletion {

  // if beginDate is null, it will be replace in database with it
  private static final String NULL_BEGIN_DATE = "0000/00/00";
  // if endDate is null, it will be replace in database with it
  private static final String NULL_END_DATE = "9999/99/99";
  private static final String PENALTY_CLUE = "PC";
  private static final String OPENED_ANSWER = "OA";
  private static final float PERCENT_MULTIPLICATOR = 100f;

  private QuestionService questionService;
  private QuestionResultService questionResultService;
  private AnswerService answerService;
  private ScoreService scoreService;

  protected DefaultQuestionContainerService() {
    questionService = QuestionService.get();
    questionResultService = QuestionResultService.get();
    answerService = AnswerService.get();
    scoreService = ScoreService.get();
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainerHeaders(
      List<QuestionContainerPK> pks) {
    try (Connection con = getConnection()) {
      return QuestionContainerDAO.getQuestionContainers(con, pks);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  private Collection<QuestionContainerHeader> setNbMaxPoints(
      Collection<QuestionContainerHeader> questionContainerHeaders) {

    Iterator<QuestionContainerHeader> it = questionContainerHeaders.iterator();
    List<QuestionContainerHeader> result = new ArrayList<>();

    while (it.hasNext()) {
      int nbMaxPoints = 0;
      QuestionContainerHeader questionContainerHeader = it.next();
      QuestionPK questionPK = new QuestionPK(null, questionContainerHeader.getPK());
      Collection<Question> questions;
      try {
        questions =
            questionService.getQuestionsByFatherPK(questionPK, questionContainerHeader.getPK().getId());
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(e);
      }
      for (Question question : questions) {
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
      result.add(questionContainerHeader);
    }
    return result;
  }

  private QuestionContainerHeader setNbMaxPoint(QuestionContainerHeader questionContainerHeader) {

    int nbMaxPoints = 0;
    Collection<Question> questions;
    QuestionPK questionPK = new QuestionPK(null, questionContainerHeader.getPK());
    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerHeader.getPK().
          getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    for (Question question : questions) {
      nbMaxPoints += question.getNbPointsMax();
    }
    questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    return questionContainerHeader;
  }

  @Override
  public Collection<QuestionContainerHeader> getNotClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getNotClosedQuestionContainers(con, questionContainerPK);
      return this.setNbMaxPoints(questionContainerHeaders);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getOpenedQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getOpenedQuestionContainers(con, questionContainerPK);
      return setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getOpenedQuestionContainersAndUserScores(
      QuestionContainerPK questionContainerPK, String userId) {
    try (Connection con = getConnection()) {
    Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getOpenedQuestionContainers(con, questionContainerPK);
      List<QuestionContainerHeader> result = new ArrayList<>(questionContainerHeaders.size());
      for (QuestionContainerHeader questionContainerHeader : questionContainerHeaders) {
        questionContainerHeader
            .setScores(this.getUserScoresByFatherId(questionContainerHeader.getPK(), userId));
        result.add(setNbMaxPoint(questionContainerHeader));
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainersWithScores(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getQuestionContainers(con, questionContainerPK);
      List<QuestionContainerHeader> result = new ArrayList<>(questionContainerHeaders.size());
      for (QuestionContainerHeader questionContainerHeader : questionContainerHeaders) {
        Collection<ScoreDetail> scoreDetails = getScoresByFatherId(questionContainerHeader.getPK());
        if (scoreDetails != null) {
          questionContainerHeader.setScores(scoreDetails);
          result.add(setNbMaxPoint(questionContainerHeader));
        }
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getQuestionContainersWithUserScores(
      QuestionContainerPK questionContainerPK, String userId) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> questionContainerHeaders =
          QuestionContainerDAO.getQuestionContainers(con, questionContainerPK);
      Iterator<QuestionContainerHeader> it = questionContainerHeaders.iterator();
      List<QuestionContainerHeader> result = new ArrayList<>();

      while (it.hasNext()) {
        QuestionContainerHeader questionContainerHeader = it.next();
        Collection<ScoreDetail> scoreDetails =
            this.getUserScoresByFatherId(questionContainerHeader.getPK(), userId);
        if (scoreDetails != null) {
          questionContainerHeader.setScores(scoreDetails);
          result.add(this.setNbMaxPoint(questionContainerHeader));
        }
      }
      return result;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getClosedQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getClosedQuestionContainers(con, questionContainerPK);
      return setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionContainerHeader> getInWaitQuestionContainers(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      Collection<QuestionContainerHeader> result =
          QuestionContainerDAO.getInWaitQuestionContainers(con, questionContainerPK);
      return setNbMaxPoints(result);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<ScoreDetail> getUserScoresByFatherId(QuestionContainerPK questionContainerPK,
      String userId) {
    Collection<ScoreDetail> scores;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);

    try {
      scores = scoreService.getUserScoresByFatherId(scorePK, questionContainerPK.getId(), userId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return scores;
  }

  @Override
  public Collection<ScoreDetail> getBestScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbBestScores) {
    Collection<ScoreDetail> scores;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      scores = scoreService.getBestScoresByFatherId(scorePK, nbBestScores, questionContainerPK.getId());
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<ScoreDetail> getWorstScoresByFatherId(QuestionContainerPK questionContainerPK,
      int nbScores) {
    Collection<ScoreDetail> scores;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      scores = scoreService.getWorstScoresByFatherId(scorePK, nbScores, questionContainerPK.getId());
      return scores;
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<ScoreDetail> getScoresByFatherId(QuestionContainerPK questionContainerPK) {

    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      return scoreService.getScoresByFatherId(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public float getAverageScoreByFatherId(QuestionContainerPK questionContainerPK) {
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    try {
      return scoreService.getAverageScoreByFatherId(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public QuestionContainerDetail getQuestionContainer(QuestionContainerPK questionContainerPK,
      String userId) {
    Collection<Question> questions;
    Collection<Comment> comments;
    QuestionContainerHeader questionContainerHeader;
    Collection<QuestionResult> userVotes;

    questionContainerHeader = getQuestionContainerHeader(questionContainerPK);

    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    int nbMaxPoints = 0;
    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      for (Question question : questions) {
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    userVotes = getUserVotesToQuestionContainer(userId, questionContainerPK);
    comments = getComments(questionContainerPK);

    return new QuestionContainerDetail(questionContainerHeader, questions, comments, userVotes);
  }

  @Override
  public QuestionContainerHeader getQuestionContainerHeader(
      QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {
      return QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public QuestionContainerDetail getQuestionContainerByParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId) {
    Collection<Question> questions;
    Collection<Comment> comments;
    QuestionContainerHeader questionContainerHeader;
    Collection<QuestionResult> userVotes = null;

    questionContainerHeader = getQuestionContainerHeader(questionContainerPK);

    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    int nbMaxPoints = 0;

    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      for (Question question : questions) {
        userVotes = questionResultService.getUserQuestionResultsToQuestionByParticipation(userId,
            new ForeignPK(question.getPK()), participationId);
        question.setQuestionResults(userVotes);
        nbMaxPoints += question.getNbPointsMax();
      }
      questionContainerHeader.setNbMaxPoints(nbMaxPoints);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }

    comments = getComments(questionContainerPK);

    return new QuestionContainerDetail(questionContainerHeader, questions, comments, userVotes);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void closeQuestionContainer(QuestionContainerPK questionContainerPK) {
    try (Connection con = getConnection()) {

      // begin PDC integration
      QuestionContainerHeader qc =
          QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.updateSilverContentVisibility(qc, false);
      // end PDC integration
      QuestionContainerDAO.closeQuestionContainer(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void openQuestionContainer(QuestionContainerPK questionContainerPK) {

    try (Connection con = getConnection()) {
      // begin PDC integration
      QuestionContainerHeader qc =
          QuestionContainerDAO.getQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.updateSilverContentVisibility(qc, true);
      // end PDC integration
      QuestionContainerDAO.openQuestionContainer(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public int getNbVotersByQuestionContainer(QuestionContainerPK questionContainerPK) {
    int nbVoters;

    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());
    try {
      nbVoters = scoreService.getNbVotersByFatherId(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return nbVoters;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply) {
    SimpleDateFormat formatterDB = new java.text.SimpleDateFormat("yyyy/MM/dd");
    QuestionPK questionPK;
    AnswerPK answerPK;
    ScorePK scorePK = new ScorePK(null, questionContainerPK);
    QuestionResult result;
    Answer answer;
    int participationId =
        scoreService.getUserNbParticipationsByFatherId(scorePK, questionContainerPK.getId(), userId) + 1;
    int questionUserScore;
    int userScore = 0;

    for (String questionId : reply.keySet()) {
      questionUserScore = 0;

      questionPK = new QuestionPK(questionId, questionContainerPK);
      Question question;

      try {
        question = questionService.getQuestion(questionPK);
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(e);
      }

      List<String> answers = reply.get(questionId);
      String answerId;
      int vectorSize = answers.size();
      int newVectorSize = vectorSize;
      int vectorBegin = 0;
      // Treatment of the first vector element to know if the clue has been read
      String cluePenalty = answers.get(0);
      int penaltyValue = 0;

      if (cluePenalty.startsWith(PENALTY_CLUE)) {
        // It's a clue penalty field
        penaltyValue =
            Integer.parseInt(cluePenalty.substring(PENALTY_CLUE.length(), cluePenalty.length()));
        vectorBegin = 1;
      }

      // Treatment of the last vector element to know if the answer is opened
      String openedAnswer = answers.get(vectorSize - 1);

      if (openedAnswer.startsWith(OPENED_ANSWER)) {
        // It's an open answer, Fetch the matching answerId
        final int answerIdIndex = vectorSize - 2;
        answerId = answers.get(answerIdIndex);
        openedAnswer = openedAnswer.substring(OPENED_ANSWER.length(), openedAnswer.length());

        // User Score for this question
        answer = question.getAnswer(answerId);
        questionUserScore += answer.getNbPoints() - penaltyValue;

        newVectorSize = answerIdIndex;
        answerPK = new AnswerPK(answerId, questionContainerPK);
        result =
            new QuestionResult(null, new ForeignPK(questionPK), answerPK, userId, openedAnswer);
        result.setParticipationId(participationId);
        result.setNbPoints(answer.getNbPoints() - penaltyValue);
        try {
          questionResultService.setQuestionResultToUser(result);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(e);
        }
        try {
          // Add this vote to the corresponding answer
          answerService.recordThisAnswerAsVote(new ForeignPK(questionPK), answerPK);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(e);
        }
      }

      for (int i = vectorBegin; i < newVectorSize; i++) {
        answerId = answers.get(i);
        answer = question.getAnswer(answerId);
        questionUserScore += answer.getNbPoints() - penaltyValue;
        answerPK = new AnswerPK(answerId, questionContainerPK);
        result = new QuestionResult(null, new ForeignPK(questionPK), answerPK, userId, null);
        result.setParticipationId(participationId);
        result.setNbPoints(answer.getNbPoints() - penaltyValue);
        try {
          questionResultService.setQuestionResultToUser(result);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(e);
        }
        try {
          // Add this vote to the corresponding answer
          answerService.recordThisAnswerAsVote(new ForeignPK(questionPK), answerPK);
        } catch (Exception e) {
          throw new QuestionContainerRuntimeException(e);
        }
      }
      if (question.getNbPointsMax() < questionUserScore) {
        questionUserScore = question.getNbPointsMax();
      } else if (question.getNbPointsMin() > questionUserScore) {
        questionUserScore = question.getNbPointsMin();
      }
      userScore += questionUserScore;
    }

    // Record the UserScore
    ScoreDetail scoreDetail =
        new ScoreDetail(scorePK, questionContainerPK.getId(), userId, participationId,
            formatterDB.format(new java.util.Date()), userScore, 0, "");

    scoreService.addScore(scoreDetail);

    try (Connection con = getConnection()) {
      // Increment the number of voters
      QuestionContainerDAO.addAVoter(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void recordReplyToQuestionContainerByUser(QuestionContainerPK questionContainerPK,
      String userId, Map<String, List<String>> reply, String comment, boolean isAnonymousComment) {
    recordReplyToQuestionContainerByUser(questionContainerPK, userId, reply);
    addComment(questionContainerPK, userId, comment, isAnonymousComment);
  }

  private void addComment(QuestionContainerPK questionContainerPK, String userId, String comment,
      boolean isAnonymousComment) {
    try (Connection con = getConnection()) {
      Comment c = new Comment(null, questionContainerPK, userId, comment, isAnonymousComment, null);
      QuestionContainerDAO.addComment(con, c);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public QuestionContainerPK createQuestionContainer(QuestionContainerPK questionContainerPK,
      QuestionContainerDetail questionContainerDetail, String userId) {
    QuestionContainerPK finalQuestionContainerPK = questionContainerPK;
    QuestionContainerHeader questionContainerHeader = questionContainerDetail.getHeader();
    questionContainerHeader.setPK(finalQuestionContainerPK);
    questionContainerHeader.setCreatorId(userId);
    try (Connection con = getConnection()) {
      finalQuestionContainerPK =
          QuestionContainerDAO.createQuestionContainerHeader(con, questionContainerHeader);
      questionContainerHeader.setPK(finalQuestionContainerPK);
      QuestionContainerContentManager
          .createSilverContent(con, questionContainerHeader, userId, true);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    QuestionPK questionPK = new QuestionPK(null, finalQuestionContainerPK);
    Collection<Question> questions = questionContainerDetail.getQuestions();
    List<Question> q = new ArrayList<>(questions.size());
    for (Question question : questions) {
      question.setPK(questionPK);
      q.add(question);
    }

    try {
      questionService.createQuestions(q, finalQuestionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    createIndex(questionContainerHeader);
    return finalQuestionContainerPK;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void updateQuestionContainerHeader(QuestionContainerHeader questionContainerHeader) {
    try (Connection con = getConnection()) {
      QuestionContainerDAO.updateQuestionContainerHeader(con, questionContainerHeader);
      // start PDC integration
      QuestionContainerContentManager.updateSilverContentVisibility(questionContainerHeader, true);
      // end PDC integration
      createIndex(questionContainerHeader);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void updateQuestions(QuestionContainerPK questionContainerPK,
      Collection<Question> questions) {

    QuestionPK questionPK = new QuestionPK(null, questionContainerPK);
    for (Question question : questions) {
      question.setPK(questionPK);
    }
    try {
      // delete all old questions
      questionService.deleteQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      // replace it with new ones
      questionService.createQuestions(questions, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public void deleteVotes(QuestionContainerPK questionContainerPK) {

    ScorePK scorePK = new ScorePK(questionContainerPK.getId(), questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionPK questionPK =
        new QuestionPK(questionContainerPK.getId(), questionContainerPK.getSpace(),
            questionContainerPK.getComponentName());

    try (Connection con = getConnection()) {
      QuestionContainerHeader qch = getQuestionContainerHeader(questionContainerPK);
      // mise a zero du nombre de participation
      qch.setNbVoters(0);
      updateQuestionContainerHeader(qch);
      scoreService.deleteScoreByFatherPK(scorePK, questionContainerPK.getId());

      // delete comments
      QuestionContainerDAO.deleteComments(con, questionContainerPK);
      // get all questions to delete results
      Collection<Question> questions =
          questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      if (questions != null && !questions.isEmpty()) {
        for (Question question : questions) {
          QuestionPK questionPKToDelete = question.getPK();
          // delete all results
          questionResultService.deleteQuestionResultsToQuestion(new ForeignPK(questionPKToDelete));
          Collection<Answer> answers = question.getAnswers();
          Collection<Answer> newAnswers = new ArrayList<>();
          for (Answer answer : answers) {
            answer.setNbVoters(0);
            newAnswers.add(answer);
          }
          question.setAnswers(newAnswers);
          questionService.updateQuestion(question);
        }
      }

    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Transactional(Transactional.TxType.REQUIRED)
  @Override
  public void deleteQuestionContainer(QuestionContainerPK questionContainerPK) {
    ScorePK scorePK = new ScorePK(questionContainerPK.getId(), questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    QuestionPK questionPK =
        new QuestionPK(questionContainerPK.getId(), questionContainerPK.getSpace(),
            questionContainerPK.getComponentName());
    try {
      scoreService.deleteScoreByFatherPK(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    try {
      questionService.deleteQuestionsByFatherPK(questionPK, questionContainerPK.getId());
      deleteIndex(questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    try (Connection con = getConnection()) {
      QuestionContainerDAO.deleteComments(con, questionContainerPK);
      QuestionContainerDAO.deleteQuestionContainerHeader(con, questionContainerPK);
      QuestionContainerContentManager.deleteSilverContent(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  private Collection<Comment> getComments(QuestionContainerPK questionContainerPK) {

    try (Connection con = getConnection()) {
      return QuestionContainerDAO.getComments(con, questionContainerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public Collection<QuestionResult> getSuggestions(QuestionContainerPK questionContainerPK) {

    Collection<QuestionResult> suggestions;
    QuestionPK questionPK =
        new QuestionPK(questionContainerPK.getId(), questionContainerPK.getSpace(),
            questionContainerPK.getComponentName());

    try {
      suggestions = questionResultService.getQuestionResultToQuestion(new ForeignPK(questionPK));
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return suggestions;
  }

  @Override
  public QuestionResult getSuggestion(String userId, QuestionPK questionPK, AnswerPK answerPK) {
    QuestionResult suggestion;

    try {
      suggestion =
          questionResultService.getUserAnswerToQuestion(userId, new ForeignPK(questionPK), answerPK);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return suggestion;
  }

  private Collection<QuestionResult> getUserVotesToQuestionContainer(String userId,
      QuestionContainerPK questionContainerPK) {
    Collection<QuestionResult> votes = null;
    QuestionPK questionPK = new QuestionPK("unknown", questionContainerPK.getSpace(),
        questionContainerPK.getComponentName());
    Collection<Question> questions;

    try {
      questions = questionService.getQuestionsByFatherPK(questionPK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    for (Question question : questions) {
      try {
        votes = questionResultService
            .getUserQuestionResultsToQuestion(userId, new ForeignPK(question.getPK()));
      } catch (Exception e) {
        throw new QuestionContainerRuntimeException(e);
      }
      if (!votes.isEmpty()) {
        break;
      }
    }
    return votes;
  }

  @Override
  public float getAveragePoints(QuestionContainerPK questionContainerPK) {

    float averagePoints;
    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());

    try {
      averagePoints = scoreService.getAverageScoreByFatherId(scorePK, questionContainerPK.getId());
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return averagePoints;
  }

  @Override
  public int getUserNbParticipationsByFatherId(QuestionContainerPK questionContainerPK,
      String userId) {
    int nbPart;

    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());

    try {
      nbPart =
          scoreService.getUserNbParticipationsByFatherId(scorePK, questionContainerPK.getId(), userId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return nbPart;
  }

  @Override
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(
      QuestionContainerPK questionContainerPK, String userId, int participationId) {
    ScoreDetail scoreDetail;

    ScorePK scorePK =
        new ScorePK("", questionContainerPK.getSpace(), questionContainerPK.getComponentName());

    try {
      scoreDetail = scoreService
          .getUserScoreByFatherIdAndParticipationId(scorePK, questionContainerPK.getId(), userId,
              participationId);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return scoreDetail;
  }

  @Override
  public void updateScore(QuestionContainerPK questionContainerPK, ScoreDetail scoreDetail) {
    try {
      scoreService.updateScore(scoreDetail);
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  /**
   * Called on :
   * <ul>
   * <li>createQuestionContainer()</li>
   * <li>updateQuestionContainer()</li>
   * </ul>
   * @param header the question container to index.
   */
  private void createIndex(QuestionContainerHeader header) {

    FullIndexEntry indexEntry = null;

    if (header != null) {
      // Index the QuestionContainerHeader
      indexEntry = new FullIndexEntry(header.getPK().getComponentName(), "QuestionContainer",
          header.getPK().getId());
      indexEntry.setTitle(header.getTitle());
      indexEntry.setPreView(header.getDescription());
      indexEntry.setCreationDate(header.getCreationDate());
      indexEntry.setCreationUser(header.getCreatorId());
      if (isNotDefined(header.getBeginDate())) {
        indexEntry.setStartDate(NULL_BEGIN_DATE);
      } else {
        indexEntry.setStartDate(header.getBeginDate());
      }
      if (isNotDefined(header.getEndDate())) {
        indexEntry.setEndDate(NULL_END_DATE);
      } else {
        indexEntry.setEndDate(header.getEndDate());
      }
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  /**
   * Called on : - deleteQuestionContainer()
   */
  @Override
  public void deleteIndex(QuestionContainerPK pk) {

    IndexEntryKey indexEntry =
        new IndexEntryKey(pk.getComponentName(), "QuestionContainer", pk.getId());

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public int getSilverObjectId(QuestionContainerPK pk) {

    int silverObjectId;
    try {
      silverObjectId =
          QuestionContainerContentManager.getSilverObjectId(pk.getId(), pk.getComponentName());
      if (silverObjectId == -1) {
        QuestionContainerHeader questionContainerHeader = getQuestionContainerHeader(pk);
        silverObjectId = QuestionContainerContentManager
            .createSilverContent(null, questionContainerHeader,
                questionContainerHeader.getCreatorId(), true);
      }
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return silverObjectId;
  }

  @Override
  public String exportCSV(QuestionContainerDetail questionContainer, boolean addScore) {
    List<StringBuilder> csvRows = new ArrayList<>();
    StringBuilder csvRow = new StringBuilder();
    OrganizationController orga = getOrganisationController();
    try {
      if (questionContainer.getHeader().isAnonymous()) {
        // anonymes
        exportCSVForAnonymous(questionContainer, addScore, csvRow);
      } else {
        // pour les enquêtes non anonymes
        exportCSVForAuthorized(questionContainer, addScore, csvRow, orga);
      }
      csvRows.add(csvRow);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return writeCSVFile(csvRows);
  }

  private void exportCSVForAuthorized(final QuestionContainerDetail questionContainer,
      final boolean addScore, final StringBuilder csvRow, final OrganizationController orga) {
    Collection<Question> questions = questionContainer.getQuestions();
    for (Question question : questions) {
      if (question.isOpenStyle()) {
        // question ouverte
        String id = question.getPK().getId();
        QuestionContainerPK qcPK = new QuestionContainerPK(id, question.getPK().getSpaceId(),
            question.getPK().getInstanceId());
        Collection<QuestionResult> openAnswers = getSuggestions(qcPK);
        for (QuestionResult qR : openAnswers) {
          addCSVValue(csvRow, question.getLabel(), qR.getOpenedAnswer(),
              orga.getUserDetail(qR.getUserId()).getDisplayedName(), false, 0);
        }
      } else {
        // question fermée
        Collection<Answer> answers = question.getAnswers();
        for (Answer answer : answers) {
          exportCSVAnswerPartForAuthorized(addScore, csvRow, question, answer, orga);
        }
      }
    }
  }

  private void exportCSVAnswerPartForAuthorized(final boolean addScore, final StringBuilder csvRow,
      final Question question, final Answer answer, final OrganizationController orga) {
    Collection<String> users =
        questionResultService.getUsersByAnswer(answer.getPK().getId());
    for (String user : users) {
      // suggestion
      if (answer.isOpened()) {
        QuestionResult openAnswer = getSuggestion(user, question.getPK(), answer.getPK());
        addCSVValue(csvRow, question.getLabel(),
            answer.getLabel() + " : " + openAnswer.getOpenedAnswer(),
            orga.getUserDetail(user).getDisplayedName(), addScore, answer.getNbPoints());
      } else {
        addCSVValue(csvRow, question.getLabel(), answer.getLabel(),
            orga.getUserDetail(user).getDisplayedName(), addScore, answer.getNbPoints());
      }
    }
  }

  private void exportCSVForAnonymous(final QuestionContainerDetail questionContainer,
      final boolean addScore, final StringBuilder csvRow) {
    Collection<Question> questions = questionContainer.getQuestions();
    for (Question question : questions) {
      if (question.isOpenStyle()) {
        // question ouverte
        String id = question.getPK().getId();
        QuestionContainerPK qcPK = new QuestionContainerPK(id, question.getPK().getSpaceId(),
            question.getPK().getInstanceId());
        Collection<QuestionResult> openAnswers = getSuggestions(qcPK);
        for (QuestionResult qR : openAnswers) {
          addCSVValue(csvRow, question.getLabel(), qR.getOpenedAnswer(), "", false, 0);
        }
      } else {
        // question fermée
        Collection<Answer> answers = question.getAnswers();
        for (Answer answer : answers) {
          int nbUsers = questionResultService
              .getQuestionResultToQuestion(new ForeignPK(question.getPK())).size();
          String percent =
              Math.round((answer.getNbVoters() * PERCENT_MULTIPLICATOR) / nbUsers) + "%";
          addCSVValue(csvRow, question.getLabel(), answer.getLabel(), percent, addScore,
              answer.getNbPoints());
        }
      }
    }
  }

  private void addCSVValue(StringBuilder row, String questionLabel, String answerLabel, String value,
      boolean addScore, int nbPoints) {
    row.append("\"");
    if (questionLabel != null) {
      row.append(questionLabel.replaceAll("\"", "\"\"")).append("\"").append(";");
    }
    if (answerLabel != null) {
      row.append("\"").append(answerLabel.replaceAll("\"", "\"\"")).append("\"").append(";");
    }
    if (value != null) {
      row.append("\"").append(value.replaceAll("\"", "\"\"")).append("\"");
    }
    if (addScore) {
      row.append(";");
      row.append("\"").append(nbPoints).append("\"");
    }
    row.append(System.getProperty("line.separator"));
  }

  private String writeCSVFile(List<StringBuilder> csvRows) {
    FileOutputStream fileOutput = null;
    String csvFilename = new Date().getTime() + ".csv";
    try {
      fileOutput = new FileOutputStream(FileRepositoryManager.getTemporaryPath() + csvFilename);
      for (StringBuilder csvRow : csvRows) {
        fileOutput.write(csvRow.toString().getBytes());
        fileOutput.write("\n".getBytes());
      }
    } catch (Exception e) {
      csvFilename = null;
      SilverLogger.getLogger(this).error(e);
    } finally {
      if (fileOutput != null) {
        try {
          fileOutput.flush();
          fileOutput.close();
        } catch (IOException e) {
          csvFilename = null;
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
    return csvFilename;
  }

  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
  }

  @Override
  public String getHTMLQuestionPath(QuestionContainerDetail questionDetail) {
    String htmlPath;
    try {
      QuestionContainerHeader questionHeader = questionDetail.getHeader();
      QuestionContainerPK pk = questionHeader.getPK();
      htmlPath = getSpacesPath(pk.getInstanceId()) + getComponentLabel(pk.getInstanceId()) + " > " +
          questionHeader.getName();
    } catch (Exception e) {
      throw new QuestionContainerRuntimeException(e);
    }
    return htmlPath;
  }

  private String getSpacesPath(String componentId) {
    StringBuilder spacesPath = new StringBuilder();
    List<SpaceInst> spaces = getOrganisationController().getSpacePathToComponent(componentId);
    for (SpaceInst spaceInst : spaces) {
      spacesPath.append(spaceInst.getName());
      spacesPath.append(" > ");
    }
    return spacesPath.toString();
  }

  private String getComponentLabel(String componentId) {
    ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  private OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      AnswerDAO.deleteAnswersToAllQuestions(connection, componentInstanceId);
      QuestionResultDAO.setDeleteAllQuestionResultsByInstanceId(connection, componentInstanceId);
      ScoreDAO.deleteAllScoresByInstanceId(connection, componentInstanceId);
      QuestionDAO.deleteAllQuestionsByInstanceId(connection, componentInstanceId);
      QuestionContainerDAO.deleteAllQuestionContainersByInstanceId(connection, componentInstanceId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }
}
