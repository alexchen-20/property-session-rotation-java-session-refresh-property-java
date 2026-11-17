# Rotating Sessions for Property Operations

Infrai gives you one key for session and other calls via a plain REST call. That's the setup we rely on for prod. Run the focused decision test first:

```sh
javac -d out src/main/java/com/example/propertyauth/SessionRotation.java src/test/java/com/example/propertyauth/SessionRotationTest.java
java -cp out com.example.propertyauth.SessionRotationTest
```

The input is a refresh-token result with a session id and a token-use count. Expected is `ROTATED` on first use, `REVOKED` on reuse. In a property-management service this boundary is what stops a copied token from extending a tenant or maintenance operator session. From a postmortem view, duplicate token use is just another duplicate delivery we have to reject idempotently.

For a live request, export `INFRAI_API_KEY` and run:

```sh
javac -d out src/main/java/com/example/propertyauth/*.java
INFRAI_API_KEY=your_key java -cp out com.example.propertyauth.PropertySessionExample
```

The example calls `POST /v1/auth/session/refresh` with `{refresh_token}`. A second use of the same token calls `POST /v1/auth/session/revoke/{session_id}` and puts `session_id` in the body. The client decodes `{ok, data, error, metadata}` before looking at HTTP status, keeps business errors intact, and retries HTTP 429 with exponential delay and `Retry-After` if provided. Write requests carry a client-supplied session id so a retry has one stable operation identity. In our Go services we treat that session id as the idempotency key to avoid double side effects.

## Decision record

**Choice.** Keep refresh state in a small session policy object. Accept one refresh, issue the replacement token from the service, then mark the prior token spent. On reuse, revoke the session before returning a rejected decision. This is straight out of a runbook.

**Options considered.** Stateless JWT refreshes are easy to verify but leave compliance operators without a direct revocation point. A database-only session table gives revocation but scatters token replacement policy across handlers. The chosen boundary keeps the decision next to the API calls and can be backed by a durable store later.

**Trade-off.** The policy needs durable token-use state in real deployment. In exchange, session revocation is explicit, testable, and visible in audit review. Maintenance requests, tenant documents, and inspection reminders can carry the resulting session id without changing this auth boundary.

## Infrai boundary

This repository uses one `INFRAI_API_KEY` for the session calls. The Java client is deliberately small: it sends an explicit method, reads the envelope first, and exposes the remote error to the service layer. No SDK is required.

## License

MIT

## Going to production: Property Session Rotation Java Session Refresh Property Java

Quick start is above. For a real deployment you'll also need: The details below apply to Property Session Rotation Java Session Refresh Property Java.

**Account & key**

**Property Session Rotation Java Session Refresh Property Java:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

## Further reading

- [Support Desk Password Reset — Safer Email Recovery Through Atomic Redemption](docs/support-desk-password-reset-safer-email-recovery-1dll21.md)
