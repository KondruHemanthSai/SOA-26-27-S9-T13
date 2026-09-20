package com.studentcentral.ai.dto;

import java.util.List;

public class ChatResponse {

    private boolean success;
    private ChatData data;

    public static class ChatData {
        private String message;
        private List<String> suggestedActions;
        private List<String> toolsUsed;
        private int remainingRateLimit;

        public ChatData() {
        }

        public ChatData(String message, List<String> suggestedActions, List<String> toolsUsed, int remainingRateLimit) {
            this.message = message;
            this.suggestedActions = suggestedActions;
            this.toolsUsed = toolsUsed;
            this.remainingRateLimit = remainingRateLimit;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getSuggestedActions() {
            return suggestedActions;
        }

        public void setSuggestedActions(List<String> suggestedActions) {
            this.suggestedActions = suggestedActions;
        }

        public List<String> getToolsUsed() {
            return toolsUsed;
        }

        public void setToolsUsed(List<String> toolsUsed) {
            this.toolsUsed = toolsUsed;
        }

        public int getRemainingRateLimit() {
            return remainingRateLimit;
        }

        public void setRemainingRateLimit(int remainingRateLimit) {
            this.remainingRateLimit = remainingRateLimit;
        }
    }

    public ChatResponse() {
    }

    public ChatResponse(boolean success, ChatData data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ChatData getData() {
        return data;
    }

    public void setData(ChatData data) {
        this.data = data;
    }
}
