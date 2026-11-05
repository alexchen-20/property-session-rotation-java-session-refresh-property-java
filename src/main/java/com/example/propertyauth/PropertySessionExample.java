package com.example.propertyauth;

public final class PropertySessionExample {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            System.err.println("Set INFRAI_API_KEY before the live example.");
            return;
        }
        String refreshToken = args.length == 0 ? "replace-with-a-refresh-token" : args[0];
        InfraiSessionClient client = new InfraiSessionClient(key);
        String envelope = client.refresh(refreshToken);
        System.out.println(envelope);
    }
}
