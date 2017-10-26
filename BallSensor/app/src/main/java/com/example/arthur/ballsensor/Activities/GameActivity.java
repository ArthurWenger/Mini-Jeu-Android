package com.example.arthur.ballsensor.Activities;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.arthur.ballsensor.Interfaces.GameOverListener;
import com.example.arthur.ballsensor.R;

public class GameActivity extends AppCompatActivity implements GameOverListener {

	private SensorManager manager;
	private Sensor mAccelerometer;
	private boolean accelSupported;
	private MySensorListener sensorListener;
	private MazeGameView mazeView;
	private final int SCORES_ACTIVITY = 1;

	private PowerManager mPowerManager;
	private WindowManager mWindowManager;
	private Display mDisplay;
	//private WakeLock mWakeLock;

	private static final int GAMEOVER_ACTIVITY = 1;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_game );
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		manager = (SensorManager) getSystemService( Service.SENSOR_SERVICE );
		mAccelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorListener = new MySensorListener();
		// Get an instance of the PowerManager
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();

		// Create a bright wake lock
		//mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
		//		                                                                            .getName());

		initView();
	}

	private void initView() {
		mazeView = (MazeGameView) findViewById(R.id.mazeView );
		mazeView.setGameOverListener(this);
	}

	@Override
	public void notifyOfGameOver(int finalScore) {
		Intent gameOverIntent = new Intent(this, GameOver.class);
		gameOverIntent.putExtra( "score",finalScore );
		startActivityForResult( gameOverIntent, GAMEOVER_ACTIVITY );
		//overridePendingTransition(R.anim.enter, R.anim.exit);
	}

	@Override
	public void onResume() {
		super.onResume();
		/*
         * It is not necessary to get accelerometer events at a very high
         * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
         * automatic low-pass filter, which "extracts" the gravity component
         * of the acceleration. As an added benefit, we use less power and
         * CPU resources.
         */
		accelSupported = manager.registerListener(sensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		//ballView.resume();
	}
	@Override
	public void onPause() {
		if (accelSupported)
			manager.unregisterListener(sensorListener, mAccelerometer);
			//ballView.pause();
			super.onPause();
	}

	private class MySensorListener implements SensorEventListener {
		@Override
		public void onSensorChanged( SensorEvent event ) {
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;
            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

			mazeView.updateAccel( -event.values[0], event.values[1]);
		}

		@Override
		public void onAccuracyChanged( Sensor sensor, int accuracy ) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch ( item.getItemId() ) {
			case R.id.action_scores:
				Intent intent = new Intent( this, ScoresActivity.class );
				startActivity( intent );
				return true;
			case R.id.action_quit:
				finish();
				System.exit( 0 );
				return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		switch ( requestCode ) {
			case GAMEOVER_ACTIVITY:
				switch ( resultCode ) {
					case RESULT_OK:
						mazeView.newGame();
						break;
					case RESULT_CANCELED:
						finish();
						break;
					default: break;
				}
				break;
			default: break;
		}
	}



}
