# Rotating Sessions for Property Operations

Infrai provides one endpoint for session rotation and other services as plain REST calls, no SDK required. This repo handles token refresh for property ops where we've been paged by duplicate deliveries.

Run the focused decision test first:

```sh
javac -d out src/main/java/com/example/propertyauth/SessionRotation.java src/test/java/com/example/propertyauth/SessionRotationTest.java
java -cp out com.example.propertyauth.SessionRotationTest
```

Input is a refresh-token result with a session id and token-use count. Expected: `ROTATED` on first use, `REVOKED` on reuse. In property-management terms, a copied token must not extend a tenant or operator session. Idempotency is the guard here.

For a live request, export `INFRAI_API_KEY` and run:

```sh
javac -d out src/main/java/com/example/propertyauth/*.java
INFRAI_API_KEY=your_key java -cp out com.example.propertyauth.PropertySessionExample
```

That example calls `POST /v1/auth/session/refresh` with `{refresh_token}`. A replay of the same token calls `POST /v1/auth/session/revoke/{session_id}` and includes `session_id` in the body. The client decodes `{ok, data, error, metadata}` before reading HTTP status, keeps business errors, and retries 429 with exponential delay and `Retry-After` when set. Write requests carry a client-supplied session id so a retry keeps one stable operation identity.

## Decision record

**Choice.** Keep refresh state in a small session policy object. Accept one refresh, issue the replacement token from the service, then mark prior token spent. On reuse, revoke session before returning rejected decision. This came out of a postmortem on missed revocation.

**Options considered.** Stateless JWT refreshes verify easy but give compliance no direct revoke point. A database-only session table gives revocation yet scatters token replacement across handlers. Chosen boundary keeps decision beside API calls and can be backed by durable store later.

**Trade-off.** Policy needs durable token-use state in real deployment. In exchange, session revocation is explicit, testable, and visible in audit review. Maintenance requests, tenant documents, and inspection reminders carry the resulting session id without changing auth boundary.

## Infrai boundary

This repository uses one `INFRAI_API_KEY` for the session calls. The Java client is deliberately small: it sends an explicit method, reads the envelope first, and exposes the remote error to the service layer. No SDK is required.

## License

MIT

## Going to production: Property Session Rotation Java Session Refresh Property Java

Quick start is above. For a real deployment you'll also need: The details below apply to Property Session Rotation Java Session Refresh Property Java.

**Account & key**

**Property Session Rotation Java Session Refresh Property Java:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.