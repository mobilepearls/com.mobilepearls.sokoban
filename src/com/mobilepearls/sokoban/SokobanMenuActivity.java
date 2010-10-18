package com.mobilepearls.sokoban;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SokobanMenuActivity extends Activity {

	public static final boolean HAPTIC_FEEDBACK_DEFAULT_VALUE = false;
	public static final String HAPTIC_FEEDBACK_PREFS_NAME = "haptic_feedback";
	public static final String SHARED_PREFS_NAME = "game_prefs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR });
		setContentView(R.layout.menu);

		final SokobanMenuActivity self = this;

		Button playButton = (Button) findViewById(R.id.startbutton);
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SokobanMenuActivity.this, SokobanLevelMenuActivity.class));
			}
		});

		Button settingsButton = (Button) findViewById(R.id.settingsbutton);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final SharedPreferences prefs = self.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
				boolean hapticFeedback = prefs.getBoolean(HAPTIC_FEEDBACK_PREFS_NAME, HAPTIC_FEEDBACK_DEFAULT_VALUE);

				AlertDialog.Builder builder = new AlertDialog.Builder(self);
				builder.setTitle("Settings");
				builder.setMultiChoiceItems(new String[] { "Vibrate when moving" }, new boolean[] { hapticFeedback },
						new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						Editor editor = prefs.edit();
						editor.putBoolean(HAPTIC_FEEDBACK_PREFS_NAME, isChecked);
						editor.commit();
					}
				});
				builder.setPositiveButton("Done", null);
				builder.create().show();
			}
		});

		Button aboutButton = (Button) findViewById(R.id.aboutbutton);
		aboutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(self, SokobanAboutActivity.class);
				startActivity(intent);
			}
		});

		Button exitButton = (Button) findViewById(R.id.exitbutton);
		exitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
