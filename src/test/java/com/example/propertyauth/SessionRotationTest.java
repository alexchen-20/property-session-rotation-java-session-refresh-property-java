package com.example.propertyauth;

public final class SessionRotationTest {
    public static void main(String[] args) {
        SessionRotation policy = new SessionRotation();
        check(policy.decide(0) == SessionRotation.Decision.ROTATED, "first use rotates");
        check(policy.decide(1) == SessionRotation.Decision.REVOKED, "reuse revokes");
        policy.requireSuccessful(new SessionRotation.RefreshResult("session-7", "next", true));
        System.out.println("session rotation decisions passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
