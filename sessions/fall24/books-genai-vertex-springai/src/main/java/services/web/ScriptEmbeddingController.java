/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.actuator.StartupCheck;
import services.ai.VertexAIClient;
import services.domain.BooksService;
import services.domain.CloudStorageService;
import services.utility.PromptUtility;
import services.utility.RequestValidationUtility;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling document embedding requests, generating document summaries
 * and persistence to Vector and SQL databases.
 */
@RestController
@RequestMapping("/script")
public class ScriptEmbeddingController {

  private static final Logger logger = LoggerFactory.getLogger(ScriptEmbeddingController.class);

  BooksService booksService;
  CloudStorageService cloudStorageService;

  VertexAIClient vertexAIClient;

  @Value("${prompts.promptTransformTF}")
  private String promptTransformTF;

  @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
  private String model;

  @Value("classpath:/bashscripts/provision-cloud-infra.sh")
  private Resource bashscript;

  public ScriptEmbeddingController(BooksService booksService,
                                   CloudStorageService cloudStorageService, VertexAIClient vertexAIClient) {
    this.booksService = booksService;
    this.cloudStorageService = cloudStorageService;
    this.vertexAIClient = vertexAIClient;
  }

  @PostConstruct
  public void init() {
      logger.info("BookImagesApplication: DocumentEmbeddingController Post Construct Initializer {}", new SimpleDateFormat("HH:mm:ss.SSS").format(
              new java.util.Date(System.currentTimeMillis())));
    logger.info(
        "BookImagesApplication: DocumentEmbeddingController Post Construct - StartupCheck can be enabled");

    StartupCheck.up();
  }

  @GetMapping("start")
  String start() {
    logger.info("BookImagesApplication: DocumentEmbeddingController - Executed start endpoint request {}", new SimpleDateFormat("HH:mm:ss.SSS").format(
              new java.util.Date(System.currentTimeMillis())));
    return "DocumentEmbeddingController started";
  }

  @RequestMapping(value = "/terraform", method = RequestMethod.POST)
  public ResponseEntity<String> receiveMessageTransform(
          @RequestBody Map<String, Object> body, @RequestHeader Map<String, String> headers) {
    // validate  headers
    // request received as a CLoudEvent
    String errorMsg = RequestValidationUtility.validateRequest(body,headers);
    if (!errorMsg.isBlank()) {
      logger.error("Document Embedding Request failed: {}", errorMsg);
      return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
    }

    // get document name and bucket
    String fileName = (String) body.get("name");
    String bucketName = (String) body.get("bucket");

    logger.info("New script to transform:{}", fileName);

    // read file from Cloud Storage
    String script = cloudStorageService.readFileAsString(bucketName, fileName);
    String response = tfTransform(script);

    // success
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @CrossOrigin
  @RequestMapping(value = "/bash/to-terraform", method = RequestMethod.POST)
  public ResponseEntity<String> tfTransformTransform(
          @RequestBody Map<String, Object> body) {

    String script = (String) body.get("script");
    String response = tfTransform(script);

    // success
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  public String tfTransform(String script) {
    logger.info("tf transform flow - Model: {}", model);
    long start = System.currentTimeMillis();
    List<Map<String, Object>> responseDoc = booksService.prompt("Find paragraphs mentioning Terraform best practices for general style, structure, and dependency management", 6000);
    String transformScriptPrompt =  PromptUtility.formatPromptTF(responseDoc, promptTransformTF, script);
    logger.info("TF transform: prompt LLM: {}ms", System.currentTimeMillis() - start);
    String response = vertexAIClient.promptModel(transformScriptPrompt, model);
    logger.info("TF transform flow: {}ms", System.currentTimeMillis() - start);
    return response;
  }
}