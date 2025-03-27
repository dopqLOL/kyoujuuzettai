package com.example.contentful_javasilver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * サブアクティビティクラス
 * - MainActivityから遷移してくる画面
 * - ホーム画面のレイアウトを表示
 */
public class HomeActivity extends AppCompatActivity {

    /**
     * アクティビティの作成時に呼ばれるライフサイクルメソッド
     * @param savedInstanceState 以前の状態を保存したBundle。アクティビティが再作成される場合に使用
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 親クラスのonCreateメソッドを呼び出し
        super.onCreate(savedInstanceState);
        
        // activity_home.xmlをこのアクティビティのレイアウトとして設定
        setContentView(R.layout.activity_home);
        final Button randomCardbtn = findViewById(R.id.randomButton);
        randomCardbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
                startActivity(intent);
            }
        });

        // 必要に応じて、MainActivityから渡されたデータを取得
        // Intent intent = getIntent();
        // String data = intent.getStringExtra("key");
    }

    /**
     * アクティビティが開始されるときに呼ばれるライフサイクルメソッド
     * - ユーザーに見えるようになる直前に呼ばれる
     */
    @Override
    protected void onStart() {
        super.onStart();
        // 画面表示前の初期化処理をここに記述
    }

    /**
     * アクティビティが再開されるときに呼ばれるライフサイクルメソッド
     * - ユーザーとの対話を開始する直前に呼ばれる
     */
    @Override
    protected void onResume() {
        super.onResume();
        // アクティビティがフォアグラウンドに来たときの処理
    }

    /**
     * アクティビティが一時停止するときに呼ばれるライフサイクルメソッド
     * - 別の画面が前面に出たときなどに呼ばれる
     */
    @Override
    protected void onPause() {
        super.onPause();
        // バックグラウンドに移行する際のデータ保存などの処理
    }

    /**
     * アクティビティが停止するときに呼ばれるライフサイクルメソッド
     * - 画面が完全に見えなくなったときに呼ばれる
     */
    @Override
    protected void onStop() {
        super.onStop();
        // アクティビティが非表示になったときの処理
    }

    /**
     * アクティビティが破棄されるときに呼ばれるライフサイクルメソッド
     * - アクティビティが完全に終了するときに呼ばれる
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // リソースの解放など、終了時の処理
    }

    /**
     * 戻るボタンが押されたときの処理をカスタマイズする場合はこのメソッドをオーバーライド
     */
    @Override
    public void onBackPressed() {
        // 必要に応じて戻るボタンの動作をカスタマイズ
        super.onBackPressed();
    }
}
