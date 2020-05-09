package cn.com.geovis.imobile;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Locale;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

public class Graphhopper extends CordovaPlugin {

	private static final String TAG = "Graphhopper";
	private static Gson gson = new GsonBuilder().create();

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Log.v(TAG, "Initialized");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "success destory");
	}
	
	public void reportError(Object reason, final CallbackContext callbackContext) {
		if(reason instanceof Throwable) {
			reason = ((Throwable)reason).getMessage();
		}
		Map<String, Object> status = new HashMap<String, Object>();
		status.put("status", "error");
		status.put("message", reason);
		callbackContext.error(gson.toJson(status));
	}
	
	public void reportSuccess(List<double[]> 
	
	points, double distance,long time,final CallbackContext callbackContext) {
		Map<String, Object> status = new HashMap<String, Object>();
		status.put("status", "success");
		status.put("data", points);
		status.put("distance", distance);
		status.put("time", time);
		callbackContext.success(gson.toJson(status));
	}

	// latFrom, lonFrom, latTo, lonTo,osmFile
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
		if (action.equals("getBestPath")) {

			double lonFrom = args.optDouble(0);
			double latFrom = args.optDouble(1);
			double lonTo = args.optDouble(2);
			double latTo = args.optDouble(3);
			String ghLocation = args.optString(4);

			GraphHopper hopper = new GraphHopperOSM().forMobile();
			hopper.setGraphHopperLocation(ghLocation);
			hopper.setEncodingManager(EncodingManager.create("car"));
			hopper.setMinNetworkSize(200, 200);

			try {
				hopper.importOrLoad();
			} catch (Exception e) {
				reportError(e, callbackContext);
				return true;
			}
			
			GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).setWeighting("fastest").setVehicle("car")
					.setLocale(Locale.CHINESE);
			GHResponse rsp = hopper.route(req);

			if (rsp.hasErrors()) {
				reportError(rsp.getErrors().get(0), callbackContext);
				return true;
			} else {

				PathWrapper path = rsp.getBest();
				PointList pointList = path.getPoints();
				double distance = path.getDistance();
				long time = path.getTime();
	
				Iterator<GHPoint3D> iterator = pointList.iterator();

				List<double[]> points = new ArrayList<double[]>();
				while (iterator.hasNext()) {
					GHPoint3D point = iterator.next();
					points.add(new double[] {point.getLon(),point.getLat() });
				}
				
				reportSuccess(points, distance, time, callbackContext);
				return true;
			}

		}

		return false;
	}
}
