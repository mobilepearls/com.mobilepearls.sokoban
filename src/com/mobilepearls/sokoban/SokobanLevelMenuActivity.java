package com.mobilepearls.sokoban;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SokobanLevelMenuActivity extends Activity {

	private static final String MAX_LEVEL_NAME = "max_level";
	public static final String SHARED_PREFS_NAME = "game_prefs";

	public static String getMaxLevelPrefName(int levelSetIndex) {
		// historical compat: first level == no suffix
		return MAX_LEVEL_NAME + (levelSetIndex == 0 ? "" : ("_" + levelSetIndex));
	}

	public void onButtonClicked(View view) {
		int index = -1;
		switch (view.getId()) {
		case R.id.levelsOriginalButton:
			index = 0;
			break;
		case R.id.levelsMasSasquatchButton:
			index = 1;
			break;
		case R.id.levelsSasIIIButton:
			index = 2;
			break;
		case R.id.levelsMicrobanEasyButton:
			index = 3;
			break;
		case R.id.levelsSasIVButton:
			index = 4;
			break;
		}

		final int levelSetIndex = index;

		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		final String maxLevelNamePref = getMaxLevelPrefName(levelSetIndex);
		final int maxLevel = Math.min(prefs.getInt(maxLevelNamePref, 1),
				SokobanLevels.levelMaps.get(levelSetIndex).length);

		if (maxLevel == 1) {
			Intent intent = new Intent();
			intent.putExtra(SokobanGameActivity.GAME_LEVEL_INTENT_EXTRA, 0);
			intent.putExtra(SokobanGameActivity.GAME_LEVEL_SET_EXTRA, levelSetIndex);
			intent.putExtra(SokobanGameActivity.SHOW_HELP_INTENT_EXTRA, true);
			intent.setClass(this, SokobanGameActivity.class);
			startActivity(intent);
		} else {
			List<String> levelList = new ArrayList<String>(maxLevel);
			for (int i = maxLevel; i > 0; i--) {
				levelList.add("Level " + i);
			}
			final String[] items = levelList.toArray(new String[maxLevel]);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Choose level");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Intent intent = new Intent();
					int levelClicked = maxLevel - item - 1;
					intent.putExtra(SokobanGameActivity.GAME_LEVEL_SET_EXTRA, levelSetIndex);
					intent.putExtra(SokobanGameActivity.GAME_LEVEL_INTENT_EXTRA, levelClicked);
					intent.setClass(SokobanLevelMenuActivity.this, SokobanGameActivity.class);
					startActivity(intent);
				}
			});
			builder.create().show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.levelsets);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setButtonText(R.id.levelsOriginalButton, 0);
		setButtonText(R.id.levelsMasSasquatchButton, 1);
		setButtonText(R.id.levelsSasIIIButton, 2);
		setButtonText(R.id.levelsMicrobanEasyButton, 3);
		setButtonText(R.id.levelsSasIVButton, 4);
	}

	private void setButtonText(int buttonId, int levelSetIndex) {
		Button button = (Button) findViewById(buttonId);
		String buttonText = button.getText().toString();
		if (buttonText.contains("-")) {
			buttonText = buttonText.split("-")[0].trim();
		}

		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		final String maxLevelNamePref = getMaxLevelPrefName(levelSetIndex);
		final int maxLevel = Math.min(prefs.getInt(maxLevelNamePref, 1), SokobanLevels.levelMaps.get(levelSetIndex).length);
		int availableLevels = SokobanLevels.levelMaps.get(levelSetIndex).length;
		button.setText(buttonText + " - " + maxLevel + "/" + availableLevels);
	}

}
