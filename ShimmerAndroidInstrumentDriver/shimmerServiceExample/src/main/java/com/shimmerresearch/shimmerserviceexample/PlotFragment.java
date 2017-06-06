package com.shimmerresearch.shimmerserviceexample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.android.guiUtilities.ShimmerDialogConfigurations;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.CallbackObject;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driverUtilities.ShimmerVerDetails;
import com.shimmerresearch.tools.PlotManagerAndroid;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;

import static android.R.attr.width;



public class PlotFragment extends Fragment {

    private static ShimmerService shimmerService;
    static String mBluetoothAddress;
    static String deviceState = "";
    static TextView textViewDeviceName;
    static TextView textViewDeviceState;
    static TextView textViewSensingStatus;
    static TextView textViewDockedStatus;

    private static String LOG_TAG = "PlotFragment";
    Button signalsToPlotButton;

    static Context context;
    private static XYPlot dynamicPlot;
    public static HashMap<String, List<Number>> mPlotDataMap = new HashMap<String, List<Number>>(4);
    public static HashMap<String, XYSeriesShimmer> mPlotSeriesMap = new HashMap<String, XYSeriesShimmer>(4);
    public static int X_AXIS_LENGTH = 500;
    private Paint transparentPaint, outlinePaint;
    static LineAndPointFormatter lineAndPointFormatter1, lineAndPointFormatter2, lineAndPointFormatter3;
    private static Paint LPFpaint;




    public PlotFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlotFragment.
     */
    public static PlotFragment newInstance() {
        PlotFragment fragment = new PlotFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plot, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        initPlot();
        textViewDeviceName = (TextView) getView().findViewById(R.id.textViewDeviceName);
        textViewDeviceState = (TextView) getView().findViewById(R.id.textViewDeviceState);
        signalsToPlotButton = (Button) getView().findViewById(R.id.button);
        signalsToPlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShimmerDialogConfigurations.showSelectSensorPlot(getActivity(), shimmerService, mBluetoothAddress, dynamicPlot);
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * This function sets up the graph
     */
    private void initPlot() {
        dynamicPlot = (XYPlot) getView().findViewById(R.id.dynamicPlot);

        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        lineAndPointFormatter1 = new LineAndPointFormatter(Color.rgb(51, 153, 255), null, null); // line color, point color, fill color
        LPFpaint = lineAndPointFormatter1.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter1.setLinePaint(LPFpaint);
        lineAndPointFormatter2 = new LineAndPointFormatter(Color.rgb(245, 146, 107), null, null);
        LPFpaint = lineAndPointFormatter2.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter2.setLinePaint(LPFpaint);
        lineAndPointFormatter3 = new LineAndPointFormatter(Color.rgb(150, 150, 150), null, null);
        LPFpaint = lineAndPointFormatter3.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter3.setLinePaint(LPFpaint);
        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        //lineAndPointFormatter1.setLinePaint(p);
        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //dynamicPlot.setDomainStepValue(series1.size());
        dynamicPlot.getLegendWidget().setTableModel(new DynamicTableModel(1, 4));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(width/2, SizeLayoutType.ABSOLUTE, height/3, SizeLayoutType.ABSOLUTE));
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.parseColor("#969696")); // black
        Paint transparentLinePaint = new Paint();
        transparentLinePaint.setColor(Color.TRANSPARENT);
        dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
        dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
//        dynamicPlot.getGraphWidget().setMargins(0, 20, 10, 10);
        dynamicPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().setGridLinePaint(transparentPaint);
        dynamicPlot.getGraphWidget().setDomainOriginLabelPaint(transparentLinePaint);
        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
//        dynamicPlot.getGraphWidget().setDomainLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainLabelPaint(transparentLinePaint);
        dynamicPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);
        dynamicPlot.getDomainLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getDomainOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getGraphWidget().setClippingEnabled(false);
        dynamicPlot.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);
        dynamicPlot.getRangeLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getRangeOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getRangeLabelWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());

    }

    /**
     * Passes the Shimmer Service to the fragment and sets its
     * @param service must be an already running Shimmer Service
     */
    public void setShimmerService(ShimmerService service) {
        shimmerService = service;
        shimmerService.setGraphHandler(graphHandler);
        if(shimmerService.mPlotManager == null) {
            shimmerService.mPlotManager = new PlotManagerAndroid(false);
        }
        shimmerService.mPlotManager.updateDynamicPlot(dynamicPlot);
    }


    private static Handler graphHandler = new Handler() {


        public void handleMessage(Message msg) {
            switch (msg.what) {
                //TODO: Check if ShimmerBluetooth msg works
                //case Shimmer.MESSAGE_STATE_CHANGE:
                case ShimmerBluetooth.MSG_IDENTIFIER_STATE_CHANGE:
                    ShimmerBluetooth.BT_STATE state=null;
                    String shimmerName = "";
                    if (msg.obj instanceof ObjectCluster){
                        state = ((ObjectCluster)msg.obj).mState;
                        mBluetoothAddress = ((ObjectCluster)msg.obj).getMacAddress();
                        shimmerName = ((ObjectCluster) msg.obj).getShimmerName();
                    } else if(msg.obj instanceof CallbackObject){
                        state = ((CallbackObject)msg.obj).mState;
                        mBluetoothAddress = ((CallbackObject)msg.obj).mBluetoothAddress;
                        shimmerName = "";
                    }
                    switch (state) {
                        case CONNECTED:
                            Log.d(LOG_TAG,"Message Fully Initialized Received from Shimmer driver");
                            shimmerService.enableGraphingHandler(true);
                            deviceState = "Connected";
                            textViewDeviceName.setText(mBluetoothAddress);
                            textViewDeviceState.setText(deviceState);
                            //buttonMenu.setEnabled(true);
                    /*
                    if(mService.isUsingLogAndStreamFW(mBluetoothAddress)){
                    	mService.readStatusLogAndStream(mBluetoothAddress);
                    	try {
    						Thread.sleep(300);
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
                    	logAndStreamStatusLayout.setVisibility(View.VISIBLE);
                    	if(mService.isDocked(mBluetoothAddress))
                    		textDockedStatus.setText("Yes");
                    	else
                    		textDockedStatus.setText("No");

                    	if(mService.isSensing(mBluetoothAddress))
                    		textSensingStatus.setText("Yes");
                    	else
                    		textSensingStatus.setText("No");
                    }*/
                            break;
                        case SDLOGGING:
                            Log.d(LOG_TAG,"Message Fully Initialized Received from Shimmer driver");
                            shimmerService.enableGraphingHandler(true);
                            deviceState = "Connected";
                            textViewDeviceName.setText(mBluetoothAddress);
                            textViewDeviceState.setText(deviceState);
                            //buttonMenu.setEnabled(true);
                    /*
                    if(mService.isUsingLogAndStreamFW(mBluetoothAddress)){
                    	mService.readStatusLogAndStream(mBluetoothAddress);
                    	try {
    						Thread.sleep(300);
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
                    	logAndStreamStatusLayout.setVisibility(View.VISIBLE);
                    	if(mService.isDocked(mBluetoothAddress))
                    		textDockedStatus.setText("Yes");
                    	else
                    		textDockedStatus.setText("No");

                    	if(mService.isSensing(mBluetoothAddress))
                    		textSensingStatus.setText("Yes");
                    	else
                    		textSensingStatus.setText("No");
                    }*/
                            break;
                        case CONNECTING:
                            Log.d(LOG_TAG,"Driver is attempting to establish connection with Shimmer device");
                            deviceState = "Connecting";
                            textViewDeviceName.setText(mBluetoothAddress);
                            textViewDeviceState.setText(deviceState);
                            break;
                        case STREAMING:
                            //textViewSensingStatus.setText("Yes");
                            deviceState="Streaming";
                            textViewDeviceName.setText(mBluetoothAddress);
                            textViewDeviceState.setText(deviceState);
                            //TODO: set the enable logging regarding the user selection
                            //shimmerService.setEnableLogging(mEnableLogging);
                            //TODO: if(!mSensorView.equals(""))
                            //TODO:	setLegend();
                            //TODO: else{
                            List<String> sensorList = shimmerService.getBluetoothManager().getListofEnabledSensors(mBluetoothAddress);
                            if(sensorList!=null){
                                if(shimmerService.getBluetoothManager().getShimmerVersion(mBluetoothAddress)== ShimmerVerDetails.HW_ID.SHIMMER_3){
                                    sensorList.remove("ECG");
                                    sensorList.remove("EMG");
                                    if(sensorList.contains("EXG1")){
                                        sensorList.remove("EXG1");
                                        sensorList.remove("EXG2");
                                        if(shimmerService.isEXGUsingECG24Configuration(mBluetoothAddress)){
                                            sensorList.add("ECG");
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
                                            //sensorName[0] = "ECG LL-RA";
                                            //sensorName[1] = "ECG LA-RA";
                                            //sensorName[2] = "EXG2 CH1";
                                            //sensorName[3] = "ECG Vx-RL";
                                        }

                                        else if(shimmerService.isEXGUsingEMG24Configuration(mBluetoothAddress)){
                                            sensorList.add("EMG");
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);
                                        }

                                        else if(shimmerService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
                                            sensorList.add("ExG Test Signal");
                                    }
                                    if(sensorList.contains("EXG1 16Bit")){
                                        sensorList.remove("EXG1 16Bit");
                                        sensorList.remove("EXG2 16Bit");
                                        if(shimmerService.isEXGUsingECG16Configuration(mBluetoothAddress)){
                                            sensorList.add("ECG 16Bit");
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT);
                                        }
                                        else if(shimmerService.isEXGUsingEMG16Configuration(mBluetoothAddress)){
                                            sensorList.add("EMG 16Bit");
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT);
                                            sensorList.add(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT);
                                        }
                                        else if(shimmerService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
                                            sensorList.add("ExG Test Signal 16Bit");
                                    }
                                }

                                sensorList.add("Timestamp");

                                //TODO: mSensorView = sensorList.get(0);
                                //TODO: setLegend();
                            }

                            //}
                            break;
                        case STREAMING_AND_SDLOGGING:
                            //textViewSensingStatus.setText("Yes");
                            deviceState="Streaming";
                            textViewDeviceName.setText(mBluetoothAddress);
                            textViewDeviceState.setText(deviceState);
                            //TODO: set the enable logging regarding the user selection
                            //shimmerService.setEnableLogging(mEnableLogging);
                    /*if(!mSensorView.equals(""))
                    	setLegend();
                    else{
                    	List<String> sensorList = mService.getListofEnabledSensors(mBluetoothAddress);
                    	if(sensorList!=null){
    	            		if(mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3){
    	            			sensorList.remove("ECG");
    	            			sensorList.remove("EMG");
    	            			if(sensorList.contains("EXG1")){
    	            				sensorList.remove("EXG1");
    	            				sensorList.remove("EXG2");
    	            				if(mService.isEXGUsingECG24Configuration(mBluetoothAddress)){
    	            					sensorList.add("ECG");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_24BIT);
    	            					//sensorName[0] = "ECG LL-RA";
    	                    			//sensorName[1] = "ECG LA-RA";
    	                    			//sensorName[2] = "EXG2 CH1";
    	                    			//sensorName[3] = "ECG Vx-RL";
    	            				}

    	            				else if(mService.isEXGUsingEMG24Configuration(mBluetoothAddress)){
    	            					sensorList.add("EMG");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH1_24BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);
    	            				}

    	            				else if(mService.isEXGUsingTestSignal24Configuration(mBluetoothAddress))
    	            					sensorList.add("ExG Test Signal");
    	            			}
    	            			if(sensorList.contains("EXG1 16Bit")){
    	            				sensorList.remove("EXG1 16Bit");
    	            				sensorList.remove("EXG2 16Bit");
    	            				if(mService.isEXGUsingECG16Configuration(mBluetoothAddress)){
    	            					sensorList.add("ECG 16Bit");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LA_RA_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_RA_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_LL_LA_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.ECG_VX_RL_16BIT);
    	            				}
    	            				else if(mService.isEXGUsingEMG16Configuration(mBluetoothAddress)){
    	            					sensorList.add("EMG 16Bit");
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH1_16BIT);
    	            					sensorList.add(Shimmer3.ObjectClusterSensorName.EMG_CH2_16BIT);
    	            				}
    	            				else if(mService.isEXGUsingTestSignal16Configuration(mBluetoothAddress))
    	            					sensorList.add("ExG Test Signal 16Bit");
    	            			}
    	            		}
                    	}

                		sensorList.add("Timestamp");
                    	mSensorView = sensorList.get(0);
                    	setLegend();
                    }*/
                            break;
                        case DISCONNECTED:
                            Log.d(LOG_TAG,"Shimmer No State");
                            mBluetoothAddress=null;
                            // this also stops streaming
                            deviceState = "Disconnected";
                            textViewDeviceName.setText("Unknown");
                            textViewDeviceState.setText(deviceState);
                            //buttonMenu.setEnabled(true);
                            //TODO: Set LogAndStreamStatusLayout
                            //logAndStreamStatusLayout.setVisibility(View.INVISIBLE);
                            break;
                    }


                    break;
                case Shimmer.MESSAGE_READ:

                    if ((msg.obj instanceof ObjectCluster)){

                    }

                    break;
                case Shimmer.MESSAGE_ACK_RECEIVED:

                    break;
                case Shimmer.MESSAGE_DEVICE_NAME:
                    // save the connected device's name

                    Toast.makeText(context, "Connected to "
                            + mBluetoothAddress, Toast.LENGTH_SHORT).show();
                    break;


                case Shimmer.MESSAGE_TOAST:
                    Toast.makeText(context, msg.getData().getString(Shimmer.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;

                case Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED:
                    int docked = msg.arg1;
//                    int sensing = msg.arg2;
//                    if(docked==1)
//                        textViewDockedStatus.setText("Yes");
//                    else
//                        textViewDockedStatus.setText("No");
//
//                    if(sensing==1)
//                        textViewSensingStatus.setText("Yes");
//                    else
//                        textViewSensingStatus.setText("No");
                    break;
            }
        }
    };




}
