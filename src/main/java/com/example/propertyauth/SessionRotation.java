package com.example.propertyauth;

public final class SessionRotation {
    public enum Decision { ROTATED, REVOKED }

    public record RefreshResult(String sessionId, String replacementToken, boolean ok) {}

    public Decision decide(int priorUses) {
        return priorUses == 0 ? Decision.ROTATED : Decision.REVOKED;
    }

    public void requireSuccessful(RefreshResult result) {
        if (!result.ok()) {
            throw new IllegalStateException("session refresh was rejected");
        }
    }
}
