package com.example.MCP_Server_Demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@SpringBootTest
class McpServerDemoApplicationTests {
    @Autowired
    private SupplierService supplierService;
    @Value("classpath:sup1.pdf")
    private Resource sup1;

    @Value("classpath:sup2.pdf")
    private Resource sup2;

    @Value("classpath:sup3.pdf")
    private Resource sup3;

    @Value("classpath:sup4.pdf")
    private Resource sup4;


    @Value("classpath:sup5.pdf")
    private Resource sup5;


    @Value("classpath:sup5-missing.pdf")
    private Resource sup5Missing;

    @Value("classpath:system-prompt.txt")
    private Resource systemPrompt;

    @Autowired
    private AIEvaluationService evaluationService;

    @Autowired
    SupplierExtractionEvaluator supplierExtractionEvaluator;

    @Test
    void basicCall() throws IOException {
        List<Resource> resources = List.of(sup1, sup2, sup3, sup4, sup5, sup5Missing);

        for (Resource resource : resources) {
            var doc = new PagePdfDocumentReader(resource).read();
            System.out.println("Extracting data for : " + resource.getFilename());

            var response = supplierService.basicCall(doc.getFirst().toString());
            //System.out.println(response);

            var evalRequest = new EvaluationRequest(
                    "All the extracted fields are present in the input document.",
                    doc,
                    response
            );
            var evalResponse = evaluationService.eval(evalRequest);
            System.out.println("Evaluation response for : " + resource.getFilename());
            System.out.println(evalResponse);
        }


    }

    @Test
    void formatted() {
        List<Resource> resources = List.of(sup1, sup2, sup3, sup4, sup5, sup5Missing);

        for (Resource resource : resources) {
            var doc = new PagePdfDocumentReader(resource).read();
            System.out.println("Extracting data for : " + resource.getFilename());
            var response = supplierService.formattedCall(doc.getFirst().toString());
            //System.out.println(response);

            var evalRequest = new EvaluationRequest(
                    "All the extracted fields are present in the input document.",
                    doc,
                    response.toString()
            );
            var evalResponse = evaluationService.eval(evalRequest);
            System.out.println("Evaluation response for : " + resource.getFilename());
            System.out.println(evalResponse.isPass());
        }
    }

    @Test
    void relevancyTest() throws IOException {
        var doc = new PagePdfDocumentReader(sup1).read();
        var response = supplierService.basicCall(doc.getFirst().toString());
        var evalRequest = new EvaluationRequest(
                "All the extracted fields are present in the input document.",
                doc,
                response
        );
        var evalResponse = evaluationService.eval(evalRequest);
    }

    @Test
    void customEvaluatorTest() throws IOException {
        List<Resource> resources = List.of(sup1, sup2, sup3, sup4, sup5, sup5Missing);
        HashMap<String, String> files = new HashMap<>();

        for (Resource resource : resources) {
            var doc = new PagePdfDocumentReader(resource).read();
            files.put(resource.getFilename(), doc.getFirst().toString());
        }
        System.out.println("Documents checking... ");
        for (String fileName : files.keySet()) {
            System.out.println("fileName = " + fileName);
            String document = files.get(fileName);
            var response = supplierService.formattedCall(document);
            System.out.println("Supplier information is extracted ");
            System.out.println(response);
            System.out.println("File is being transferred to the Kunefe Validator for hallucination check");

            var evalResponse = supplierExtractionEvaluator.evaluate(
                    response,
                    systemPrompt.getContentAsString(Charset.defaultCharset()),
                    document
            );

            System.out.println("Evaluation Score:" + evalResponse.getScore());
            if(evalResponse.isPass()) {
                System.out.println("Evaluation is successful ");
            } else {
                System.out.println("Evaluation failed ");
                System.out.println("Feedback:");
                System.out.println(evalResponse.getFeedback());
            }
            System.out.println("-------------------------------");
        }

    }

}
