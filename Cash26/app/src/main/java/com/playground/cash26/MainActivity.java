package com.playground.cash26;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, MapInteractor.Callback, LocationHelper.Callback,
		GoogleMap.OnMarkerClickListener {

	private GoogleMap googleMap;
	private MapInteractor mapInteractor;
	private LocationHelper locationHelper;
	private Bank[] banks = new Bank[MapInteractor.MAX_DISPLAYED_BANKS];
	private List<Marker> markers;

	private GoogleMap.OnCameraChangeListener mapCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
		@Override
		public void onCameraChange(CameraPosition cameraPosition) {
			LatLngBounds cameraBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
			if (cameraBounds != null) {
				mapInteractor.onCameraChanged(cameraBounds);
				Log.d("camera changed listener", cameraBounds.toString());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mapInteractor = new MapInteractor(this);
		locationHelper = new LocationHelper(this, this);
		markers = new ArrayList<>();

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;
		this.googleMap.setMyLocationEnabled(true);
		this.googleMap.setOnCameraChangeListener(mapCameraChangeListener);
	}

	@Override
	public void onBanksReceived(Bank[] banks) {
		googleMap.clear();
		markers.clear();
		System.arraycopy(banks, 0, this.banks, 0, banks.length);

		for (int i = 0; i < banks.length; i++) {
			LatLng latLng = new LatLng(Double.parseDouble(banks[i].lat), Double.parseDouble(banks[i].lng));
			markers.add(googleMap.addMarker(new MarkerOptions().position(latLng)));
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
		for (int i = 0; i < markers.size() - 1; i++) {
			if (marker == markers.get(i)) {
				navigateToBank(marker.getPosition());
				return true;
			}
		}
		return false;
	}

	private void navigateToBank(LatLng latlng) {
		mapInteractor.onNavigateToBank(latlng.latitude, latlng.longitude);
	}
}
