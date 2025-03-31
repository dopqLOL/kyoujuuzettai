package com.example.contentful_javasilver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.ActivityMainBinding;
import com.example.contentful_javasilver.utils.SecurePreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // ボトムナビゲーションの表示制御とデバッグログ
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            String destLabel = destination.getLabel() != null ? destination.getLabel().toString() : "No Label";
            NavDestination currentDest = controller.getCurrentDestination();
            int currentId = currentDest != null ? currentDest.getId() : -1;
            String currentLabel = (currentDest != null && currentDest.getLabel() != null) ? currentDest.getLabel().toString() : "None";

            Log.d("MainActivity", "Navigating from: " + currentLabel + " (" + currentId + ") to: " + destLabel + " (" + destId + ")");

            if (destId == R.id.startFragment || destId == R.id.loadingFragment) {
                Log.d("MainActivity", "Hiding Bottom Navigation for " + destLabel);
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                Log.d("MainActivity", "Showing Bottom Navigation for " + destLabel);
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });

        // NavigationUI.setupWithNavController の呼び出しを削除し、完全に手動で制御する
        Log.d("MainActivity", "Setting up Bottom Navigation manually");
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            String itemTitle = item.getTitle().toString();
            NavDestination currentDestination = navController.getCurrentDestination();
            int currentId = currentDestination != null ? currentDestination.getId() : -1;

            Log.d("MainActivity", "Manual Listener - Item selected: " + itemTitle + " (" + itemId + "), Current destination ID: " + currentId);

            // 現在表示中の画面と同じアイテムが選択された場合は何もしない (または false を返す)
            if (itemId == currentId) {
                Log.d("MainActivity", "Manual Listener - Same item reselected (" + itemTitle + "). Doing nothing.");
                return false; // falseを返すと選択状態のハイライトが更新されない場合があるので注意、trueでも良いかもしれない
            }

            // NavOptions を設定してバックスタックを管理
            NavOptions.Builder builder = new NavOptions.Builder()
                    .setLaunchSingleTop(true) // 既にスタックにある場合は新しいインスタンスを作らない
                    .setRestoreState(true); // 状態を復元

            // ルートグラフの開始デスティネーションまでポップアップする
            // これにより、タブ切り替え時に前のタブのスタックがクリアされる挙動を模倣
            // 注意: popUpToInclusive=false なので、開始デスティネーション自体は残る
            // NavOptions options = builder.build(); // 一時的にオプションを削除

            try {
                // 選択されたアイテムIDにナビゲート (オプションなしで試す)
                Log.d("MainActivity", "Manual Listener - Attempting navigate(" + itemId + ") without options.");
                navController.navigate(itemId);
                // navigate呼び出し直後の状態を確認 (デバッグ用)
                NavDestination destAfterNavigate = navController.getCurrentDestination();
                int idAfterNavigate = destAfterNavigate != null ? destAfterNavigate.getId() : -1;
                String labelAfterNavigate = (destAfterNavigate != null && destAfterNavigate.getLabel() != null) ? destAfterNavigate.getLabel().toString() : "None";
                Log.d("MainActivity", "Manual Listener - Destination immediately after navigate() call: " + labelAfterNavigate + " (" + idAfterNavigate + ")");

                Log.d("MainActivity", "Manual Listener - navigate(" + itemId + ") called successfully.");
                return true; // ナビゲーション呼び出し成功
            } catch (IllegalArgumentException e) {
                Log.e("MainActivity", "Manual Listener - Navigation failed for item " + itemTitle, e);
                return false; // ナビゲーション失敗
            }
        });
        Log.d("MainActivity", "Manual Bottom Navigation listener set.");


        // BuildConfigからキーを取得し、安全に保存
        String apiKey = BuildConfig.CONTENTFUL_ACCESS_TOKEN;
        String spaceId = BuildConfig.CONTENTFUL_SPACE_ID;
        SecurePreferences.initializeSecureKeys(getApplicationContext(), apiKey, spaceId);

        // カスタムリスナーは削除し、NavigationUI.setupWithNavController の標準動作に任せる (コメントは残しておく)
    }

    // ツールバーのUpボタン（戻る矢印）の処理
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    // 物理的な戻るボタンの処理 (必要に応じてオーバーライド)
    // デフォルトの動作で問題なければ不要
    // @Override
    // public void onBackPressed() {
    //     if (!navController.navigateUp()) {
    //         super.onBackPressed();
    //     }
    // }
}
