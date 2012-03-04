package me.vermon.ticketcheck;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity class. Lets user to start or stop checking for tickets
 * 
 * @author Maido Käära
 * 
 */
public class TicketcheckActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		updateStatus();
		final SeekBar intervalbar = (SeekBar) findViewById(R.id.intervalbar);
		intervalbar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						setInterval(progress);
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}

				});
		final Button start = (Button) findViewById(R.id.buttonstart);
		start.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				int showId = getShowId();
				if (showId != 0) {
					int interval = getInterval();
					AlarmReceiver.setAlarm(TicketcheckActivity.this, interval,
							showId);
					updateStatus();
					CharSequence startedWithInterval = getResources().getText(
							R.string.startedwithinterval)
							+ " "
							+ interval
							+ " "
							+ getResources().getText(R.string.minutes);
					Toast toast = Toast.makeText(TicketcheckActivity.this,
							startedWithInterval, Toast.LENGTH_SHORT);
					toast.show();
				} else {
					showError();
				}
			}
		});
		final Button stop = (Button) findViewById(R.id.buttonstop);
		stop.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				AlarmReceiver.removeAlarm(TicketcheckActivity.this);
				updateStatus();
				Toast toast = Toast.makeText(TicketcheckActivity.this,
						R.string.alarmstopped, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	private void showError() {
		Toast toast = Toast.makeText(TicketcheckActivity.this,
				R.string.errorshow, Toast.LENGTH_SHORT);
		toast.show();
	}

	private int getShowId() {
		final EditText showId = (EditText) findViewById(R.id.show);
		try {
			return Integer.parseInt(showId.getText().toString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private int getInterval() {
		final SeekBar intervalbar = (SeekBar) findViewById(R.id.intervalbar);
		return intervalbar.getProgress() + 1;
	}

	private void setInterval(int minutes) {
		final TextView interval = (TextView) findViewById(R.id.interval);
		CharSequence minutesText = getResources().getText(R.string.minutes);
		interval.setText(minutes + 1 + " " + minutesText);
	}

	/**
	 * Updates current status text.
	 */
	private void updateStatus() {
		final TextView status = (TextView) findViewById(R.id.status);
		status.setText(getResources().getText(
				AlarmReceiver.isAlarmSet(this) ? R.string.started
						: R.string.stopped));
	}
}