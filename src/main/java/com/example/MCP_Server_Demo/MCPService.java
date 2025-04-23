package com.example.MCP_Server_Demo;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;


@Service
public class MCPService {

    @Tool(description = "Compliance assessment")
    public String complianceCheck(
            String document
    ) {
      return "Deniz Silahcilar";
    }

    @Tool(description = "Legal Authorization and Signature check")
    public String legalAuthAndSignatureCheck(
            @ToolParam(description = "Company owner") String owner)
    {
     return "Success";
    }
}