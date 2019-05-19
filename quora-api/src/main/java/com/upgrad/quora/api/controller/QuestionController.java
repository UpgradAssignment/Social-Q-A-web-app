package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.UserAdminService;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;

import com.upgrad.quora.service.exception.QuestionNotFoundException;
import com.upgrad.quora.service.exception.UserNotFoundException;
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
public class QuestionController {

    @Autowired
    UserAdminService userAdminService;

    @Autowired
    QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest, @RequestHeader final String authorization) throws AuthorizationFailedException {
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.CREATE_QUESTION);
        UserEntity user = authorizedUser.getUser();
        Question question = new Question();
        question.setUser(authorizedUser.getUser());
        question.setUuid(UUID.randomUUID().toString());
        question.setContent(questionRequest.getContent());
        final ZonedDateTime now = ZonedDateTime.now();
        question.setDate(now);
        Question createdQuestion = questionService.createQuestion(question);
        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestion.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDetailsResponse> getAllQuestions(@RequestHeader final String authorization) throws AuthorizationFailedException, QuestionNotFoundException {
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.ALL_QUESTION);
        //Since the user is authorized, go for extracting questions for all users
        List<Question> questionList = questionService.getAllQuestions();
        StringBuilder builder = new StringBuilder();
        getContentsString(questionList, builder);
        StringBuilder uuIdBuilder = new StringBuilder();
        getUuIdString(questionList, uuIdBuilder);
        QuestionDetailsResponse questionResponse = new QuestionDetailsResponse()
                .id(uuIdBuilder.toString())
                .content(builder.toString());
        return new ResponseEntity<QuestionDetailsResponse>(questionResponse, HttpStatus.OK);
    }



    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(QuestionEditRequest questionEditRequest, @PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.EDIT_QUESTION);
        //Check if the user himself is the owner and trying to edit it and return the question object
        Question question = questionService.isUserQuestionOwner(questionId, authorizedUser, ActionType.EDIT_QUESTION);
        question.setContent(questionEditRequest.getContent());
        questionService.editQuestion(question);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(question.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> userDelete(@PathVariable("questionId") final String questionUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.DELETE_QUESTION);
        //Check if the user himself is the owner and trying to edit it and return the question object
        Question question = questionService.isUserQuestionOwner(questionUuId, authorizedUser, ActionType.DELETE_QUESTION);
        questionService.deleteQuestion(question);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse()
                .id(question.getUuid())
                .status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDetailsResponse> getAllQuestionsByUser(@PathVariable("userId") final String uuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, QuestionNotFoundException, UserNotFoundException {
        UserAuthEntity authorizedUser = userAdminService.getUserByAccessToken(authorization, ActionType.ALL_QUESTION_FOR_USER);
        //Get the list of questions for the user
        List<Question> questionList = questionService.getQuestionsForUser(uuId);
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder uuIdBuilder = new StringBuilder();
        getContentsString(questionList, contentBuilder);
        getUuIdString(questionList, uuIdBuilder);
        QuestionDetailsResponse questionResponse = new QuestionDetailsResponse()
                .id(uuIdBuilder.toString())
                .content(contentBuilder.toString());
        return new ResponseEntity<QuestionDetailsResponse>(questionResponse, HttpStatus.OK);
    }


    public static final StringBuilder getUuIdString(List<Question> questionList, StringBuilder uuIdBuilder) {

        for (Question questionObject : questionList) {
            uuIdBuilder.append(questionObject.getUuid()).append(",");
        }
        return uuIdBuilder;
    }

    public static final StringBuilder getContentsString(List<Question> questionList, StringBuilder builder) {
        for (Question questionObject : questionList) {
            builder.append(questionObject.getContent()).append(",");
        }
        return builder;
    }
}
