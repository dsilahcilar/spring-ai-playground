package com.example.MCP_Server_Demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;

import java.util.Scanner;


@SpringBootApplication
public class McpServerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerDemoApplication.class, args);
    }


    //@Bean
//    public ToolCallbackProvider weatherTools(MCPService mcpService) {
//        return MethodToolCallbackProvider.builder().toolObjects(mcpService).build();
//    }


  //  @Bean
    public CommandLineRunner cli(@Value("classpath:sup1.pdf") Resource suppDocs,
                                 ChatClient.Builder chatClientBuilder) {

        return args -> {

            // 1. Load the hurricane documents in vector store
            //      vectorStore.add(new TokenTextSplitter().split(new PagePdfDocumentReader(hurricaneDocs).read()));

            var pdfStr = new PagePdfDocumentReader(suppDocs).read();

            // 2. Create the ChatClient with chat memory and RAG support
            var chatClient = chatClientBuilder
                    .defaultSystem("You are useful assistant, expert in suppliers.") // Set the system prompt
                    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory())) // Enable chat memory
                    //.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // Enable RAG
                    .build();

            // 3. Start the chat loop
            System.out.println("\nI am your supplier assistant.\n");
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("\nUSER: ");
                    System.out.println("\nASSISTANT: " +
                            chatClient.prompt() // Get the user input
                                    .user(u -> u.text(scanner.nextLine() + ":\n {context}")
                                            .param("context", pdfStr)
                                    )
                                    .call()
                                    .content())
                    ;
                }
            }
        };
    }
}


