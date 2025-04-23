package com.example.MCP_Server_Demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.ai.evaluation.FactCheckingEvaluator;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class SupplierExtractionEvaluator {
    private final ChatClient.Builder chatClientBuilder;


    SupplierExtractionEvaluator(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;

        OllamaOptions optionsModel = new OllamaOptions();
        optionsModel.setModel("bespoke-minicheck");
        //      optionsModel.setNumPredict(1);
//        optionsModel.setMaxTokens(1);
        this.chatClientBuilder.defaultOptions(optionsModel);
    }

    private boolean isEqual(String a, String b) {
        return Objects.equals(trimOrNull(a), trimOrNull(b));
    }

    private String generateMismatchFeedback(String expected, String actual) {
        return "Expected: [" + safeValue(expected) + "], but got: [" + safeValue(actual) + "]";
    }

    private String safeValue(String value) {
        return value == null ? "null" : value;
    }

    private String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }

    public EvaluationResponse evaluate(Supplier supplier, String systemPrompt, String context) {

        Supplier evaluationResponse = this.chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(u -> u.text("analyse this document: " + ":\n {context}")
                        .param("context", context)
                ).call()
                .entity(Supplier.class);

        int score = 0;
        StringBuilder feedback = new StringBuilder();
        Map<String, String> fieldFeedback = new LinkedHashMap<>();

        if (isEqual(supplier.company(), evaluationResponse.company())) {
            score++;
        } else {
            fieldFeedback.put("Company", generateMismatchFeedback(supplier.company(), evaluationResponse.company()));
        }

        if (isEqual(supplier.address(), evaluationResponse.address())) {
            score++;
        } else {
            fieldFeedback.put("Address", generateMismatchFeedback(supplier.address(), evaluationResponse.address()));
        }

        if (isEqual(supplier.contactPerson(), evaluationResponse.contactPerson())) {
            score++;
        } else {
            fieldFeedback.put("Contact Person", generateMismatchFeedback(supplier.contactPerson(), evaluationResponse.contactPerson()));
        }

        if (isEqual(supplier.phone(), evaluationResponse.phone())) {
            score++;
        } else {
            fieldFeedback.put("Phone", generateMismatchFeedback(supplier.phone(), evaluationResponse.phone()));
        }

        if (isEqual(supplier.email(), evaluationResponse.email())) {
            score++;
        } else {
            fieldFeedback.put("Email", generateMismatchFeedback(supplier.email(), evaluationResponse.email()));
        }

        boolean passing = score == 5;

        // Build feedback text
        fieldFeedback.forEach((field, fb) -> feedback.append(field).append(": ").append(fb).append("\n"));
        return new EvaluationResponse(passing, score, feedback.toString(), Collections.emptyMap());
    }
}
