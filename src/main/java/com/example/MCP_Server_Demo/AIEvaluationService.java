package com.example.MCP_Server_Demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.stereotype.Component;

@Component
public class AIEvaluationService {
    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallingManager toolCallingManager;
    private FactCheckingEvaluator evaluator;

    AIEvaluationService(ChatClient.Builder chatClientBuilder, ToolCallingManager toolCallingManager) {
        this.chatClientBuilder = chatClientBuilder;
        this.toolCallingManager = toolCallingManager;
        OllamaApi ollamaApi = new OllamaApi();

//        OllamaOptions optionsModel = new OllamaOptions();
//        optionsModel.setModel("bespoke-minicheck");
//        optionsModel.setNumPredict(1);
//        optionsModel.setMaxTokens(1);
//        chatClientBuilder.defaultOptions(optionsModel);
//

     //   evaluator = new RelevancyEvaluator(chatClientBuilder);
        evaluator = FactCheckingEvaluator.forBespokeMinicheck(chatClientBuilder);
    }

    public EvaluationResponse eval(EvaluationRequest evaluationRequest) {
        var response = evaluator.evaluate2(evaluationRequest);
        return response;
    }
}
