package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserAdminService;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    AnswerService answerService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserAdminService userAdminService;


    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        //Authorize
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.EDIT_ANSWER);

        //Edit response
        Answer answer = answerService.isUserAnswerOwner(answerUuId, authorizedUser, ActionType.EDIT_ANSWER);
        answer.setAnswer(answerEditRequest.getContent());
        answer.setDate(ZonedDateTime.now());
        Answer editedAnswer = answerService.editAnswer(answer);// persist changes
        AnswerEditResponse answerEditResponse = new AnswerEditResponse()
                .id(answerUuId)
                .status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {

        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.DELETE_ANSWER);
        //Delete validation
        Answer answer = answerService.isUserAnswerOwner(answerUuId, authorizedUser, ActionType.DELETE_ANSWER);
        answerService.deleteAnswer(answer);
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse()
                .id(answerUuId)
                .status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDetailsResponse> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException, AnswerNotFoundException {

        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.GET_ALL_ANSWER_TO_QUESTION);
        List<Answer> answerList = answerService.getAnswersForQuestion(questionId);
        StringBuilder contentBuilder = new StringBuilder();
        getContentsString(answerList, contentBuilder);
        StringBuilder uuIdBuilder = new StringBuilder();
        String questionContentValue = getUuIdStringAndQuestionContent(answerList, uuIdBuilder);
        AnswerDetailsResponse response = new AnswerDetailsResponse()
                .id(uuIdBuilder.toString())
                .answerContent(contentBuilder.toString())
                .questionContent(questionContentValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(final AnswerRequest answerRequest, @PathVariable("questionId") final String questionUuId, @RequestHeader final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        //Authorize the user
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.CREATE_QUESTION);
        //get the question object from database
        Question question = questionService.getQuestionForUuId(questionUuId);
        //Create answer object
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswer(answerRequest.getAnswer());
        answer.setUuid(UUID.randomUUID().toString());
        answer.setUser(authorizedUser.getUser());
        ZonedDateTime now = ZonedDateTime.now();
        answer.setDate(now);
        Answer createdAnswer = answerService.createAnswer(answer);

        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    public static final String getUuIdStringAndQuestionContent(List<Answer> answerList, StringBuilder uuIdBuilder) {
        String questionContent = new String();
        for (Answer answerObject : answerList) {
            uuIdBuilder.append(answerObject.getUuid()).append(",");
            questionContent = answerObject.getQuestion().getContent();
        }
        return questionContent;
    }


    public static final StringBuilder getContentsString(List<Answer> answerList, StringBuilder builder) {
        for (Answer answerObject : answerList) {
            builder.append(answerObject.getAnswer()).append(",");
        }
        return builder;
    }

}
