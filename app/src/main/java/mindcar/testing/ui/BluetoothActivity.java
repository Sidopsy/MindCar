package mindcar.testing.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.common.io.Files;
import com.neurosky.thinkgear.TGDevice;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import mindcar.testing.R;
import mindcar.testing.objects.Command;
import mindcar.testing.objects.Connected;
import mindcar.testing.objects.EegBlink;
import mindcar.testing.objects.SmartCar;
import mindcar.testing.util.DatabaseAccess;
import mindcar.testing.util.MessageParser;
import mindcar.testing.objects.ComparePatterns;
import mindcar.testing.objects.Connection;
import mindcar.testing.objects.Eeg;
import mindcar.testing.objects.Pattern;
import mindcar.testing.util.NeuralNetworkHelper;

/**Johan, Sarah, Sanja, Mattias, Nicos
 * Created by Sarah And Johan, refactored and commented by Sarah on 2016-05-01.
 * Refactored and integrated by Sanja on 2016-05-22.
 * This class starts the bluetooth adapter, then pairs the adapter to Nemesis system.
 */
public class BluetoothActivity extends Activity implements AdapterView.OnItemClickListener {

    protected static final int SUCCESS_CONNECT = 0;//Handler for situation of connection status
    protected static final int MESSAGE_READ = 1;//Handler for situation of connection status
    private Button exit; // terminates the application
    private Button activate;// turns Bluetooth on and initiates methods
    private ListView listView;//shows paired items
    private ProgressDialog mProgressDlg;//for discovery progress
    private ProgressBar bar;// waiting indicator for pairing and connecting
    public static BluetoothAdapter theAdapter = BluetoothAdapter.getDefaultAdapter(); //the bluetooth adapter
    private Set<BluetoothDevice> paired_devices; //a set of bonded devices
    private ArrayAdapter<String> mylist; //viewed in the list view, the paired devices
    private ArrayList<BluetoothDevice> mDeviceList;// discovery list for adding requiered devices
    private IntentFilter filter; // an intent
    private boolean deviceOne = false;//booleans for stopping discovery
    private boolean deviceTwo = false;//booleans for stopping discovery
    private ArrayList<String> connectedDevices;//list of connected bluetooth devices
    public static TGDevice tgDevice;//Mindreading headset
    boolean isConnected = false;// status of connection
    public static Connection connect;// class for handling communication with the car
    public static Connected connected; //Class for streaming data
    int attentionLevel = 0;
    final int ATTENTIONLIMIT = 40;
    public static Boolean startLearning = false; //flag used to indicate mind pattern learning

    //Mattias
        //Mind control variables
    int times = 1000;
    int nrTimes = 0;
    private Pattern pattern;
    private Eeg eeg;

    //Nikos && Sanja
        //Blink control variables
    public EegBlink eegBlink = new EegBlink();
    private Command command;
    private SmartCar car;
    private Boolean sendCommand = false;
    long lastBlink = 0;
    int blinkCount = 0;

    private DatabaseAccess databaseAccess;
    private int patternCounter = 0;
    private int eegTimes = 0;

    //Sarah
        // Below starts the connection handler:
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case TGDevice.STATE_CONNECTED:
                            isConnected = true;
                            tgDevice.start();
                            Log.i("wave", "Connected");
                            break;
                    }
                    break;
                //Mattias
                     //Mind controller
                    //Handles both learning of patterns (startlearning=true)
                    //and mind pattern based control (startlearning=false)
                case TGDevice.MSG_RAW_DATA:
                    if (startLearning) { //REGISTRATION LEARNING DATA

                            if (eegTimes < 20) {
                                MessageParser.parseRawData(msg, RegisterPatternActivity.tmpEeg);
                                eegTimes++;
                                break;
                            }
                            if (patternCounter < RegisterPatternActivity.PATTERN_SIZE) {
                                Log.i("Some", patternCounter + " ");
                                RegisterPatternActivity.tmpPattern.add(RegisterPatternActivity.tmpEeg);
                                RegisterPatternActivity.tmpEeg = new Eeg();
                                eegTimes = 0;
                                patternCounter++;
                                break;
                            }
                            if (patternCounter == RegisterPatternActivity.PATTERN_SIZE) {
                                Log.i("Some", "next value");
                                RegisterPatternActivity.nextValue();
                                patternCounter = 0;
                            }

                        if (RegisterPatternActivity.baselineBoolean) {
                            Log.i("Some", "hello1");
                            RegisterPatternActivity.populateArray(RegisterPatternActivity.baseline);

                        } else if (RegisterPatternActivity.leftBoolean) {
                            Log.i("Some", "hello2");
                            RegisterPatternActivity.populateArray(RegisterPatternActivity.left);

                        } else if (RegisterPatternActivity.rightBoolean) {
                            Log.i("Some", "hello3");
                            RegisterPatternActivity.populateArray(RegisterPatternActivity.right);

                        } else if (RegisterPatternActivity.forwardBoolean) {
                            Log.i("Some", "hello4");
                            RegisterPatternActivity.populateArray(RegisterPatternActivity.forward);

                        } else if (RegisterPatternActivity.stopBoolean) {
                            Log.i("Some", "hello5");
                            RegisterPatternActivity.populateArray(RegisterPatternActivity.stop);

                            RegisterPatternActivity.populateInputs();
                            RegisterPatternActivity.extendTrainingSet();

                            if (nrTimes < 5) {
                                Log.i("Something", nrTimes + "");
                                Log.i("Something", SavePatterns.toString(RegisterPatternActivity.baseline));
                                Log.i("Something", SavePatterns.toString(RegisterPatternActivity.left));
                                Log.i("Something", SavePatterns.toString(RegisterPatternActivity.right));
                                Log.i("Something", SavePatterns.toString(RegisterPatternActivity.forward));
                                Log.i("Something", SavePatterns.toString(RegisterPatternActivity.stop));
                                RegisterPatternActivity.baselinePattern = null;

                                RegisterPatternActivity.initializeArrays();
                                RegisterPatternActivity.inputs = new LinkedList<>();

                                nrTimes++;
                            } else {
                                Log.i("Something", "final else");
                                RegisterPatternActivity.neuralNetwork.learnInNewThread(RegisterPatternActivity.trainingSet);

                                try {
                                    NeuralNetworkHelper.saveNetwork(BluetoothActivity.this, RegisterPatternActivity.neuralNetwork, "ta.nnet");
                                    File nnet = BluetoothActivity.this.getFileStreamPath("ta.nnet");
                                    byte[] b = Files.toByteArray(nnet);
                                    databaseAccess.open();
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("neuralnetwork", b);
                                    databaseAccess.update("Users", contentValues, RegistrationActivity.user_name);
                                    databaseAccess.close();
                                    startLearning = false;
                                    next();
                                } catch (IOException e) {
                                    Log.i("Something", e.getMessage());
                                }
                            }
                        }
                        break;

                    } else {// MIND CONTROL IN USERACTIVITY
                        if (UserActivity.appRunning) {
                            if (UserActivity.mindControl) {
                                MessageParser.parseRawData(msg, eeg);
                                if (times <= 0) {
                                    Log.i("Time start ", System.currentTimeMillis() + "");
                                    pattern.add(eeg);
                                    ComparePatterns compPatt = new ComparePatterns(pattern.toArray(), UserActivity.neuralNetwork);
                                    String send = compPatt.compare(UserActivity.databaseAccess);
                                    Log.i("Send message ", send);
                                    if (send == "w") {
                                        connected.write("s");
                                    } else {
                                        connected.write(send);
                                    }
                                    eeg = new Eeg();
                                    times = 1000;
                                    Log.i("Time stop ", System.currentTimeMillis() + "");
                                } else {
                                    times--;
                                }
                            }
                        }
                    }
                    break;

                //Nikos & Sanja
                    //Counts blinks for both Blink & Attention Control and Attention Only Control
                case TGDevice.MSG_BLINK:
                    //Count blinks
                    if (UserActivity.appRunning && UserActivity.blinkControl && attentionLevel > ATTENTIONLIMIT) {
                        if ((System.currentTimeMillis() - lastBlink) < 1000 && (System.currentTimeMillis() - lastBlink) > 100)
                            blinkCount++;
                        else
                            blinkCount = 1;
                        //remember when last blink occured
                        lastBlink = System.currentTimeMillis(); // remember when last blink occured
                    } else
                        blinkCount = 0; // reset when application is paused
                    break;

                //Nikos & Sanja & Johan
                    //Main Blink & Attention handler
                case TGDevice.MSG_ATTENTION:
                    if (UserActivity.appRunning) {
                        attentionLevel = msg.arg1;
                        if (UserActivity.blinkControl) { //Blink & Attention Control
                            if (attentionLevel > ATTENTIONLIMIT) {
                                //if no blinks for 1000ms, execute command
                                if ((System.currentTimeMillis() - lastBlink) >= 1000) {
                                    if (blinkCount >= 2 && blinkCount <= 4) { // left is 3 +-1
                                        connected.write("l");
                                        synchronized (this) { // pause app until turn is finnished
                                            try {
                                                this.wait(1200);
                                            } catch (Exception e) {
                                            }
                                        }
                                        connected.write("f");
                                        blinkCount = 0;
                                    } else if (blinkCount >= 5) { // right is 5+, aim for 6 +-1
                                        connected.write("r");
                                        synchronized (this) { // pause app until turn is finnished
                                            try {
                                                this.wait(1200);
                                            } catch (Exception e) {
                                            }
                                        }
                                        connected.write("f");
                                        blinkCount = 0;
                                    } else {
                                        connected.write("f");
                                        blinkCount = 0;
                                    }
                                }
                            } else {
                                connected.write("STOP");
                                blinkCount = 0;
                            }
                            //Johan
                        } else if (UserActivity.attentionControl) {//ATTENTION ONLY CONTROL
                            if (attentionLevel > ATTENTIONLIMIT) {
                                connected.write("f");
                            } else
                                connected.write("STOP");
                        }
                    }
                    break;
            }

        }
    };
    //Sarah
        //Main constructor
    public BluetoothActivity() {
    }

    //Sarah
        //Creates the Bluetooth connection activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setupUI();

        //below are actions registered in the receiver
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE);
        registerReceiver(discoveryResult, filter);

        //CREATE MIND CONTROL OBJECTS
        eeg = new Eeg();
        pattern = new Pattern(100);
        databaseAccess = DatabaseAccess.getInstance(this);

        //CREATE BLINK CONTROL OBJECTS
        car = new SmartCar();
        command = car.getCommands();
    }

    //Sarah
        //Create list objects and buttons
    public void setupUI() {
        connectedDevices = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);//the list with the paired items
        listView.setOnItemClickListener(this); // registers callback when an item been clicked
        mylist = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);//layout
        listView.setAdapter(mylist);//pouring myList into the listView
        bar = (ProgressBar) findViewById(R.id.progressBar2);// bar indicator of pairing and connection status
        bar.setVisibility(View.GONE);
        activate = (Button) findViewById(R.id.button);
        activate.setVisibility(View.VISIBLE);
        activate.setOnClickListener(new View.OnClickListener() { //turning on and off bluetooth and changing text of button
            @Override
            public void onClick(View v) {
                if (!theAdapter.isEnabled()) {
                    setBluetooth(true);
                    bar.setVisibility(View.VISIBLE);
                    activate.setText("Cancel Pairing");
                } else {
                    bar.setVisibility(View.GONE);
                    activate.setText("Discover and Pair");
                    checkItems();
                }
            }
        });
    }
    //Sarah
        //goes to next activity
    private void next() {
        startActivity(new Intent(this, StartActivity.class));
    }

    //Sarah
        //handles click events on listed Bluetooth adapters
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (theAdapter.isDiscovering()) { // checking to Cancel discovery if on at connecting
            cancelDisc();
        }
        BluetoothDevice selectedDevice = mDeviceList.get(position); //getting the position in mDeviceList
        if (selectedDevice.getName().equals("Group 2")) {
            connect = new Connection(selectedDevice); //passing in the selectedDevice and connecting it
            activate.setText("Cancel Pairing");
            connect.start();
            bar.setVisibility(View.VISIBLE);
            connected = new Connected(connect.bluetoothSocket);
        } else if (selectedDevice.getName().equals("MindWave Mobile")) {
            tgDevice = new TGDevice(theAdapter, mHandler);
            if (tgDevice.getState() != TGDevice.STATE_CONNECTING
                    && tgDevice.getState() != TGDevice.STATE_CONNECTED) {
                activate.setText("Cancel Pairing");
                tgDevice.connect(true);
                bar.setVisibility(View.VISIBLE);
            }
        }
    }
    //Sarah
        // this method turn on and off bluetooth device
    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        return true;
    }

   //Sarah
    //this method, pairs, and adds items to the list, also starts and ends discovery.
    private void checkItems() {
        paired_devices = theAdapter.getBondedDevices();
        if (paired_devices.size() != 0) {
            for (BluetoothDevice device : paired_devices) {
                if (device.getName().equals("Group 2")) {
                    if (!mDeviceList.contains(device)) {
                        mylist.add("Click To Connect: " + device.getName() + " " + " " + "\n" + "Address: " + device.getAddress());
                        mDeviceList.add(device);
                        deviceOne = true;
                    }
                    listView.setAdapter(mylist);
                } else if (device.getName().equals("MindWave Mobile")) {
                    if (!mDeviceList.contains(device)) {
                        mylist.add("Click To Connect: " + device.getName() + " " + " " + "\n" + "Address: " + device.getAddress());
                        mDeviceList.add(device);
                        deviceTwo = true;
                    }
                    listView.setAdapter(mylist);
                }
            }
            if (!deviceOne || !deviceTwo) {
                theAdapter.startDiscovery();
            } else {
                activate.setText("Discover and Pair");
                bar.setVisibility(View.GONE);
            }
        } else {
            theAdapter.startDiscovery();
        }
    }

    //Sarah
        //this method simply prints toasts anywhere in the code as needed
    private void toastMaker(String input) {
        Toast.makeText(BluetoothActivity.this, input, Toast.LENGTH_LONG).show();
    }

    //Sarah
        //here is the receiver and all the actions being registered and events happen as the result proceeds.
    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT) // this Api works with the method "CreateBond"
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (theAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    checkItems();
                }
            }
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                if (theAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    toastMaker("BlueTooth Is Now Off");
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mProgressDlg.show();
                bar.setVisibility(View.VISIBLE);
                activate.setText("Cancel pairing");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                toastMaker("Discovery ended.");
                mProgressDlg.dismiss();
            }
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                if ((!device.getName().equals(null) && (device.getName().equals("Group 2") || device.getName().equals("MindWave Mobile")))) {
                    connectedDevices.add(device.getName());
                    //mDeviceList.remove(device);
                    toastMaker("Connected to: " + device.getName());
                    bar.setVisibility(View.GONE);
                    activate.setText("Discover and Pair");
                }
                if (connectedDevices.contains("Group 2") && connectedDevices.contains("MindWave Mobile")) {
                    next();
                }
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connectedDevices.remove(device.getName());
                toastMaker("Disconnected From: " + device.getName());
                if (device.getName().equals("Group 2")) {
                }
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    if (device.getName().equals("Group 2")) {
                        device.createBond();
                    } else if (device.getName().equals("MindWave Mobile")) {
                        device.createBond();
                    }
                }
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    toastMaker(device.getName() + " has been paired successfully!");
                    checkItems();
                }
            }
        }

    };

    //Sarah
        //this method cancels discovery and is called in the onClickListener of the ListView
    private void cancelDisc() {
        if (theAdapter.getState() == BluetoothAdapter.STATE_ON) {
            theAdapter.cancelDiscovery();
        }
    }
}


