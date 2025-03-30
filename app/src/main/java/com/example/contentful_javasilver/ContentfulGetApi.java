package com.example.contentful_javasilver;

import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;


public class ContentfulGetApi {
    private static final String ACCESS_TOKEN = BuildConfig.CONTENTFUL_ACCESS_TOKEN;
    private static final String SPACE_ID = BuildConfig.CONTENTFUL_SPACE_ID;
    private CDAClient client;
    // コンストラクタでスペースIDとアクセストークンを受け取る
    public ContentfulGetApi(String spaceId, String accessToken) {
        this.client = CDAClient.builder()
                .setSpace(spaceId)
                .setToken(accessToken)
                .build();
    }
    // 特定のコンテンツタイプの全エントリを取得
    public CDAArray fetchEntries(String contentType) {
        return client
                .fetch(CDAEntry.class)
                .withContentType(contentType)
                .limit(1000)
                .all();
    }
    // 特定のエントリを取得するメソッド
    public CDAEntry fetchEntryById(String entryId) {
        return client.fetch(CDAEntry.class).one(entryId);
    }
    
    // CDAClientインスタンスを取得するメソッド
    public CDAClient getClient() {
        return this.client;
    }
}
