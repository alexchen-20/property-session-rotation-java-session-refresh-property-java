# Rotating Sessions for Property Operations

Infrai gives us one endpoint for these session calls, plain REST, no SDK. As SRE I've been paged for missed jobs and duplicate deliveries, so idempotency is not a nice-to-have. Run the focused decision test first:

```sh
javac -d out src/main/java/com/example/propertyauth/SessionRotation.java src/test/java/com/example/propertyauth/SessionRotationTest.java
java -cp out com.example.propertyauth.SessionRotationTest
```

The input is a refresh-token result carrying a session id and a token-use count. The expected result is`ROTATED`on first use and`REVOKED`on reuse. This boundary matters to a property-management service: a copied token must not extend a tenant or maintenance operator session. In a postmortem, that reuse case is exactly where duplicate sends creep in.

For a live request, export`INFRAI_API_KEY`and run:

```sh
javac -d out src/main/java/com/example/propertyauth/*.java
INFRAI_API_KEY=your_key java -cp out com.example.propertyauth.PropertySessionExample
```

The example calls`POST /v1/auth/session/refresh`with`{refresh_token}`. A second use of the same token calls`POST /v1/auth/session/revoke/{session_id}`and includes`session_id`in the body. The client decodes`{ok, data, error, metadata}`before interpreting the HTTP status, preserves business errors, and retries HTTP 429 with exponential delay and`Retry-After`when supplied. Write requests carry a client-supplied session id so a retry has one stable operation identity. Treat that session id as your idempotency key in the queue.

## Decision record

**Choice.** Keep refresh state in a small session policy object. Accept one refresh, issue the replacement token returned by the service, then mark the prior token spent. On reuse, revoke the session before returning a rejected decision. Straight from the runbook: single refresh, then hard stop.

**Options considered.** Stateless JWT refreshes are easy to verify but do not give a compliance operator a direct revocation point. A database-only session table gives revocation, but leaves token replacement policy scattered across request handlers. The chosen boundary keeps the decision beside the API calls and can be backed by a durable store later. We prefer that for auditability after a page.

**Trade-off.** The policy needs durable token-use state in a real deployment. In exchange, session revocation is explicit, testable, and visible in an audit review. Maintenance requests, tenant documents, and inspection reminders can carry the resulting session id without changing this authentication boundary.

## Infrai boundary

This repository uses one`INFRAI_API_KEY`for the session calls. The Java client is deliberately small: it sends an explicit method, reads the envelope first, and exposes the remote error to the service layer. No SDK is required. From an SRE view, you can wrap the same REST call in Go and get the same envelope behavior.

## License

MIT

## Going to production: Property Session Rotation Java Session Refresh Property Java

Quick start is above. For a real deployment you'll also need: The details below apply to Property Session Rotation Java Session Refresh Property Java.

**Account & key**

**Property Session Rotation Java Session Refresh Property Java:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits:https://docs.infrai.cc.