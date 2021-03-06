package lehigh.seniordesign.homebluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothCommandService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for Bluetooth Command Service
	private BluetoothCommandService mCommandService = null;

	private SeekBar sk1;
	private SeekBar sk2;
	private ImageButton sched1;
	private ImageButton sched2;
	final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.out.println("In onCreate");

		// Set up the window layout
		setContentView(R.layout.main);

		// Add listeners to SeekBars
		sk1 = (SeekBar) findViewById(R.id.seekBar1);
		sk1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int seekProgress;

			public void onStopTrackingTouch(SeekBar seekBar) {
				seekSend(seekProgress, 1);
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				seekProgress = progress;
			};

			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sk2 = (SeekBar) findViewById(R.id.seekBar2);
		sk2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int seekProgress;

			public void onStopTrackingTouch(SeekBar seekBar) {
				seekSend(seekProgress, 2);
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				seekProgress = progress;
			};

			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});

		sched1 = (ImageButton) findViewById(R.id.schedule1);

		sched1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// custom dialog
				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.schedule1);
				dialog.setTitle("Schedule - Ceiling Light");

				// set the custom dialog components - text, image and button
				// TextView text = (TextView) dialog.findViewById(R.id.text);
				// text.setText("Android custom dialog example!");
				Button dialogButton = (Button) dialog
						.findViewById(R.id.dialogButtonOK);
				// if button is clicked, close the custom dialog
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});

		sched2 = (ImageButton) findViewById(R.id.schedule2);

		sched2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// custom dialog
				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.schedule2);
				dialog.setTitle("Schedule - Floor Light");

				// set the custom dialog components - text, image and button
				// TextView text = (TextView) dialog.findViewById(R.id.text);
				// text.setText("Android custom dialog example!");

				Button dialogButton = (Button) dialog
						.findViewById(R.id.dialogButtonOK);
				// if button is clicked, close the custom dialog
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		System.out.println("Out onCreate");
	}

	@Override
	protected void onStart() {
		super.onStart();

		System.out.println("In onStart");

		// If BT is not on, request that it be enabled.
		// setupCommand() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		// otherwise set up the command service
		else {
			if (mCommandService == null)
				setupCommand();
		}

		System.out.println("Out onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mCommandService != null) {
			if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
				mCommandService.start();
			}
		}
	}

	private void setupCommand() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mCommandService = new BluetoothCommandService(this, mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mCommandService != null)
			mCommandService.stop();
	}

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothCommandService.STATE_CONNECTED:
					break;
				case BluetoothCommandService.STATE_CONNECTING:
					break;
				case BluetoothCommandService.STATE_LISTEN:
				case BluetoothCommandService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mCommandService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupCommand();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	public void off1(View view) {
		sk1.setProgress(0);
		mCommandService.write(1);
		mCommandService.write(0);
	}

	public void on1(View view) {
		sk1.setProgress(100);
		mCommandService.write(1);
		mCommandService.write(100);
	}

	public void off2(View view) {
		sk2.setProgress(0);
		mCommandService.write(2);
		mCommandService.write(0);
	}

	public void on2(View view) {
		sk2.setProgress(100);
		mCommandService.write(2);
		mCommandService.write(100);
	}

	public void seekSend(int progress, int addr) {
		mCommandService.write(addr);
		mCommandService.write(progress);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mCommandService.write(BluetoothCommandService.VOL_UP);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mCommandService.write(BluetoothCommandService.VOL_DOWN);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
