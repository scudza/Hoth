package com.playground.cash26;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, MapInteractor.Callback, LocationHelper.Callback,
		GoogleMap.OnMarkerClickListener {

	private static final String TAG = "Mainactivity";

	private GoogleMap googleMap;
	private MapInteractor mapInteractor;
	private LocationHelper locationHelper;
	private Bank[] banks = new Bank[MapInteractor.MAX_DISPLAYED_BANKS];
	private List<Marker> markers;
	private List<com.google.android.gms.maps.model.Polyline> currentPolyLines;
	private Marker navigatingToMarker;

	private SupportMapFragment mapFragment;

	private GoogleMap.OnCameraChangeListener mapCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
		@Override
		public void onCameraChange(CameraPosition cameraPosition) {
			LatLngBounds cameraBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
			if (cameraBounds != null) {
				if (isNetworkAvailable()) {
					mapInteractor.onCameraChanged(cameraBounds);
					Log.d(TAG, cameraBounds.toString());
				} else {
					Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mapInteractor = new MapInteractor(this, getString(R.string.google_maps_key));
		locationHelper = new LocationHelper(this, this);
		markers = new ArrayList<>();

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;
		this.googleMap.setMyLocationEnabled(true);
		this.googleMap.setOnCameraChangeListener(mapCameraChangeListener);
		this.googleMap.setOnMarkerClickListener(this);
	}

	@Override
	public void onBanksReceived(Bank[] banks) {
		if (banks.length > 0) {

			for (int i = 0; i < markers.size(); i++) {
				if (navigatingToMarker != null) {
					if (markers.get(i).getPosition() != navigatingToMarker.getPosition()) {
						markers.get(i).remove();
					}
				} else {
					markers.get(i).remove();
				}
			}

			System.arraycopy(banks, 0, this.banks, 0, banks.length);

			for (int i = 0; i < banks.length; i++) {
				LatLng latLng = new LatLng(Double.parseDouble(banks[i].lat), Double.parseDouble(banks[i].lng));
				markers.add(googleMap.addMarker(new MarkerOptions().position(latLng)));
			}
		} else {
			Toast.makeText(this, getString(R.string.no_atms_in_your_area), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLastKnowLocationReceived(Location location) {
		if (googleMap != null) {
			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location
					.getLongitude()), 17f, 0f, 0f)));
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		locationHelper.disconnectLocationService();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		navigatingToMarker = marker;
		for (int i = 0; i < markers.size() - 1; i++) {
			if (marker.getPosition().latitude == markers.get(i).getPosition().latitude) {
				navigateToBank(marker.getPosition());
				return true;
			}
		}
		return false;
	}

	private void navigateToBank(LatLng latlng) {
		if (locationHelper.getLastKnownLocation() != null) {
			mapInteractor.onNavigateToBank(latlng, locationHelper.getLastKnownLocation());
		} else {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
					.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LocationHelper
						.LOCATION_PERMISSION);
			} else {
				Toast.makeText(this, getString(R.string.your_location_unavailable), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onDirectionsReceived(String encodedPolyLine) {
		//	Log.d(TAG, " steps count " + Integer.toString(steps.size()));
		if (currentPolyLines != null) {
			for (Polyline line : currentPolyLines) {
				line.remove();
			}

			currentPolyLines.clear();
			List<LatLng> drawingPoints = PolyUtil.decode(encodedPolyLine);
			Log.d(TAG, "drawing points " + Integer.toString(drawingPoints.size()));
			currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().addAll(drawingPoints).color(Color.BLUE).width(5f)));
		} else {
			currentPolyLines = new ArrayList<com.google.android.gms.maps.model.Polyline>();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LocationHelper.LOCATION_PERMISSION) {
			if (resultCode == PackageManager.PERMISSION_GRANTED) {
				mapFragment.getMapAsync(this);
			}
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	@Override
	public void onFailure() {
		Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
	}
}
