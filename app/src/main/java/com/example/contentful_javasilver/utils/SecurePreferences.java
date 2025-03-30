package com.example.contentful_javasilver.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * 安全な暗号化されたSharedPreferencesを提供するユーティリティクラス
 */
public class SecurePreferences {
    private static final String PREFERENCES_FILE_NAME = "secure_prefs";
    private static final String API_KEY_CONTENTFUL = "contentful_api_key";
    private static final String SPACE_ID_CONTENTFUL = "contentful_space_id";
    private static SharedPreferences securePrefs;

    /**
     * 暗号化されたSharedPreferencesを初期化
     * @param context アプリケーションコンテキスト
     * @return 初期化されたPreferences
     */
    public static SharedPreferences getSecurePreferences(Context context) {
        if (securePrefs == null) {
            try {
                // マスターキーを生成
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build();

                MasterKey masterKey = new MasterKey.Builder(context)
                        .setKeyGenParameterSpec(spec)
                        .build();

                // 暗号化されたSharedPreferencesを作成
                securePrefs = EncryptedSharedPreferences.create(
                        context,
                        PREFERENCES_FILE_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                // フォールバックとして通常のSharedPreferencesを使用
                securePrefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
            }
        }
        return securePrefs;
    }

    /**
     * Contentful APIキーを保存
     */
    public static void saveContentfulApiKey(Context context, String apiKey) {
        getSecurePreferences(context).edit().putString(API_KEY_CONTENTFUL, apiKey).apply();
    }

    /**
     * Contentful APIキーを取得
     */
    public static String getContentfulApiKey(Context context) {
        return getSecurePreferences(context).getString(API_KEY_CONTENTFUL, "");
    }

    /**
     * Contentful SPACE_IDを保存
     */
    public static void saveContentfulSpaceId(Context context, String spaceId) {
        getSecurePreferences(context).edit().putString(SPACE_ID_CONTENTFUL, spaceId).apply();
    }

    /**
     * Contentful SPACE_IDを取得
     */
    public static String getContentfulSpaceId(Context context) {
        return getSecurePreferences(context).getString(SPACE_ID_CONTENTFUL, "");
    }

    /**
     * 初期APIキーを安全に保存する（アプリ初回起動時のみ）
     */
    public static void initializeSecureKeys(Context context, String contentfulApiKey, String contentfulSpaceId) {
        SharedPreferences prefs = getSecurePreferences(context);
        
        // キーがまだ保存されていない場合のみ保存
        if (prefs.getString(API_KEY_CONTENTFUL, "").isEmpty()) {
            prefs.edit().putString(API_KEY_CONTENTFUL, contentfulApiKey).apply();
        }
        
        if (prefs.getString(SPACE_ID_CONTENTFUL, "").isEmpty()) {
            prefs.edit().putString(SPACE_ID_CONTENTFUL, contentfulSpaceId).apply();
        }
    }
} 