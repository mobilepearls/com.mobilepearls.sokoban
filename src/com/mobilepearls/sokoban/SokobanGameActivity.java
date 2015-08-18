package com.mobilepearls.sokoban;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class SokobanGameActivity extends Activity {

	/** Key under which the {@link SokobanGameState} is stored in the saved instance state bundle. */
	private static final String GAME_KEY = "GAME";
	/** Key under which the level to launch is set as an extra Intent attribute. */
	public static final String GAME_LEVEL_INTENT_EXTRA = "GAME_LEVEL";
	public static final String GAME_LEVEL_SET_EXTRA = "GAME_LEVEL_SET";
	public static int IMAGE_SIZE;
	/** Key under which the image size is stored. */
	public static final String IMAGE_SIZE_PREFS_KEY = "image_size";
	/** If the help should be shown (when max level is one). */
	public static final String SHOW_HELP_INTENT_EXTRA = "SHOW_HELP";
	public SokobanGameState gameState;

	SokobanGameView view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			gameState = (SokobanGameState) savedInstanceState.getSerializable(GAME_KEY);
		}
		if (gameState == null) {
			int level = 0;
			Intent intent = getIntent();
			int levelSet = 0;
			if (intent != null && intent.getExtras() != null) {
				level = intent.getExtras().getInt(GAME_LEVEL_INTENT_EXTRA, 0);
				if (intent.getExtras().getBoolean(SHOW_HELP_INTENT_EXTRA, false))
					showHelp(); // show when starting first level from menu
				levelSet = intent.getExtras().getInt(GAME_LEVEL_SET_EXTRA, 0);
			}
			gameState = new SokobanGameState(level, levelSet);
		}
		setContentView(R.layout.main);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int defaultImageSize = Math.min(size.x, size.y) / 11; // 11 = tile size of first level
		if (defaultImageSize % 2 != 0)
			defaultImageSize--;
		IMAGE_SIZE = getSharedPreferences(SokobanPrefs.SHARED_PREFS_NAME, MODE_PRIVATE).getInt(IMAGE_SIZE_PREFS_KEY,
				defaultImageSize);

		view = (SokobanGameView) findViewById(R.id.android_memoryview);

		View undoButton = findViewById(R.id.game_undobutton);
		undoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				view.backPressed();
			}
		});
		undoButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				new AlertDialog.Builder(SokobanGameActivity.this).setMessage(R.string.game_restart)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						gameState.restart();
						view.invalidate();
					}
				}).setNegativeButton(android.R.string.cancel, null).show();
				return true;
			}
		});
		findViewById(R.id.game_leavebutton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/** Overridden to handle back button - see {@link SokobanGameActivity#onBackPressed() } */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (IMAGE_SIZE < 136)
				setImageSize(IMAGE_SIZE + 2);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (IMAGE_SIZE > 12)
				setImageSize(IMAGE_SIZE - 2);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			// avoid the beep when pressing the buttons
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(GAME_KEY, gameState);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Immersive mode in kitkat or later.
			if (hasFocus) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
		} else {
			// Dim on older android versions.
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

	private void setImageSize(int newSize) {
		IMAGE_SIZE = newSize;
		SharedPreferences prefs = getSharedPreferences(SokobanPrefs.SHARED_PREFS_NAME, MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(IMAGE_SIZE_PREFS_KEY, newSize);
		editor.commit();
		view.customSizeChanged();
		view.invalidate();
	}

	public void showHelp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Push all red diamonds on the green targets to complete a level. Complete levels to unlock new ones.\n\nZoom in and out using the volume control.\n\nUndo moves with the back button.");
		builder.setPositiveButton("Ok", null);
		builder.create().show();
	}
}