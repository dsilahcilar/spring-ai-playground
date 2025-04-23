package com.example.MCP_Server_Demo;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;


@Service
public class SupplierService {

    private final ChatClient.Builder chatClientBuilder;

    @Value("classpath:system-prompt.txt")
    private Resource systemPrompt;

    public SupplierService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public String basicCall(String document) throws IOException {
        var chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(Charset.defaultCharset())) // Set the system prompt
                //  .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())) // Enable chat memory
                //.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // Enable RAG
                .build();

        var prompt = chatClient.prompt() // Get the user input
                .user(u -> u.text("analyse this document: " + ":\n {context}")
                        .param("context", document)

                );


        var call = prompt.call();
        var content = call.content();
        var response = call.chatResponse();

        return response.toString();
    }

    public Supplier formattedCall(String document) {
        try {
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt.getContentAsString(Charset.defaultCharset())) // Set the system prompt
                    //  .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())) // Enable chat memory
                    //.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // Enable RAG
                    .build();
            var prompt = chatClient.prompt() // Get the user input
                    .user(u -> u.text("analyse this document: " + ":\n {context}")
                            .param("context", document)
                    );

            var call = prompt.call().entity(Supplier.class);

            return call;
        } catch (Exception e) {
          //  e.printStackTrace();
            System.out.println("LLM call parse error!");
            return new Supplier("","","","","");
        }
    }
}