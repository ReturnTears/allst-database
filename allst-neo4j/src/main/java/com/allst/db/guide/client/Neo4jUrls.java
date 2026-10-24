package com.allst.db.guide.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Neo4jUrls {

    private static final Pattern TRANSACTION_ID = Pattern.compile("/tx/(\\d+)(?:/commit)?/?$");

    private Neo4jUrls() {
    }

    public static String transactionId(String commitOrTransactionUrl) {
        if (commitOrTransactionUrl == null || commitOrTransactionUrl.isEmpty()) {
            return null;
        }
        Matcher matcher = TRANSACTION_ID.matcher(commitOrTransactionUrl);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }
}
