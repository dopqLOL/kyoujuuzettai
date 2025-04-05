package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class SettingsFragment extends Fragment {

    private TextView appVersionText;
    private SwitchCompat notificationSwitch;
    private Button privacyPolicyButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appVersionText = view.findViewById(R.id.appVersionText);
        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        privacyPolicyButton = view.findViewById(R.id.privacyPolicyButton);

        // Set App Version (Example - replace with actual version retrieval)
        try {
            String versionName = requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0).versionName;
            appVersionText.setText(getString(R.string.version_format, versionName));
        } catch (Exception e) {
            appVersionText.setText(getString(R.string.version_format, "N/A"));
        }

        // TODO: Implement notification switch logic (save state, handle changes)
        // notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> { ... });

        // Set OnClickListener for Privacy Policy Button
        privacyPolicyButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(SettingsFragment.this);
            // Navigate to PrivacyPolicyFragment (Action ID needs to be defined in nav_graph.xml)
            navController.navigate(R.id.action_settingsFragment_to_privacyPolicyFragment);
        });

        // Add listeners or logic for other settings items here
    }
}
