package com.studentcentral.ai.agent;

public interface AgentTool {

    String getName();

    String getDescription();

    String execute(String userId, String jwtToken, String parameter);
}
