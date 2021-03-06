package com.ijoomer.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.ijoomer.common.classes.IjoomerSuperMaster;
import com.ijoomer.common.classes.IjoomerUtilities;
import com.ijoomer.common.classes.ViewHolder;
import com.ijoomer.customviews.IjoomerEditText;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.src.R;
import com.smart.framework.CustomAlertNeutral;
import com.smart.framework.ItemView;
import com.smart.framework.SmartListAdapterWithHolder;
import com.smart.framework.SmartListItem;

public class IjoomerMapAddress extends IjoomerSuperMaster {

	private MapView mapview;
	private GestureDetector gestureDetector;

	private ListView lstMapAddress;
	ArrayList<HashMap<String, String>> addressList;
	private LinearLayout lnrLstMapAddress;
	private ProgressBar pbrLstMapAddress;
	private IjoomerTextView txtMapAddressHints;

	private IjoomerEditText editSearch;
	private ImageView imgSearch;

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {

	}

	@Override
	public int setLayoutId() {
		return R.layout.ijoomer_map_address;
	}

	// @Override
	// public int setHeaderLayoutId() {
	// return 0;
	// }

	@Override
	public int setFooterLayoutId() {
		return 0;
	}

	@Override
	public void initComponents() {
		mapview = getMapView();
		mapview.setBuiltInZoomControls(false);
		mapview.setStreetView(true);
		// mapview.setSatellite(true);
		mapview.getController().setZoom(6);

		lstMapAddress = (ListView) findViewById(R.id.lstMapAddress);
		lnrLstMapAddress = (LinearLayout) findViewById(R.id.lnrLstMapAddress);
		pbrLstMapAddress = (ProgressBar) findViewById(R.id.pbrLstMapAddress);
		txtMapAddressHints = (IjoomerTextView) findViewById(R.id.txtMapAddressHints);
		editSearch = (IjoomerEditText) findViewById(R.id.editSearch);
		imgSearch = (ImageView) findViewById(R.id.imgSearch);
		gestureDetector = new GestureDetector(new Detector());
		mapview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		try {
			Address address = IjoomerUtilities.getAddressFromLatLong(0, 0);
			mapview.getController().animateTo(new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6)));
			setAddressData(address.getLatitude(), address.getLongitude());
		} catch (Exception e) {
		}

	}

	@Override
	public void prepareViews() {
	}

	@Override
	public void setActionListeners() {
		imgSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				hideSoftKeyboard();
				if (editSearch.getText().toString().trim().length() > 0) {
					try {
						Address address = IjoomerUtilities.getLatLongFromAddress(editSearch.getText().toString().trim());
						mapview.getController().animateTo(new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6)));
						setAddressData(address.getLatitude(), address.getLongitude());
						editSearch.setText(null);
					} catch (Exception e) {
						editSearch.setText(null);
					}
				} else {
					editSearch.setError(getString(R.string.validation_value_required));
				}
			}
		});
	}

	private void setAddressData(double lat, double lng) {
		Geocoder geocoder;
		if (lat != 0 && lng != 0) {
			geocoder = new Geocoder(IjoomerMapAddress.this);
			try {
				List<Address> list = geocoder.getFromLocation(lat, lng, 10);

				if (list != null && list.size() > 0) {
					if (txtMapAddressHints.getVisibility() == View.GONE) {
						pbrLstMapAddress.setVisibility(View.VISIBLE);
					}
					addressList = new ArrayList<HashMap<String, String>>();
					for (Address address : list) {
						HashMap<String, String> data = new HashMap<String, String>();
						if (address.getAddressLine(0).toString().trim().length() > 0) {
							data.put("address", address.getAddressLine(0));
							data.put("latitude", String.valueOf(address.getLatitude()));
							data.put("longitude", String.valueOf(address.getLongitude()));
							addressList.add(data);
						} else if (address.getAddressLine(1).toString().trim().length() > 0) {
							data.put("address", address.getAddressLine(1));
							data.put("latitude", String.valueOf(address.getLatitude()));
							data.put("longitude", String.valueOf(address.getLongitude()));
							addressList.add(data);
						} else if (address.getLocality().toString().trim().length() > 0) {
							data.put("address", address.getLocality());
							data.put("latitude", String.valueOf(address.getLatitude()));
							data.put("longitude", String.valueOf(address.getLongitude()));
							addressList.add(data);
						} else if (address.getAdminArea().toString().trim().length() > 0) {
							data.put("address", address.getAdminArea());
							data.put("latitude", String.valueOf(address.getLatitude()));
							data.put("longitude", String.valueOf(address.getLongitude()));
							addressList.add(data);
						} else if (address.getCountryName().toString().trim().length() > 0) {
							data.put("address", address.getAdminArea());
							data.put("latitude", String.valueOf(address.getLatitude()));
							data.put("longitude", String.valueOf(address.getLongitude()));
							addressList.add(data);
						}
					}

					lstMapAddress.setAdapter(getListAdapter(prepareList(addressList)));
					if (txtMapAddressHints.getVisibility() == View.VISIBLE) {
						lstMapAddress.setVisibility(View.VISIBLE);
						txtMapAddressHints.setVisibility(View.GONE);
					} else {
						pbrLstMapAddress.setVisibility(View.GONE);
					}

				} else {
					IjoomerUtilities.getCustomOkDialog(getString(R.string.address_from_map), getString(R.string.lat_long_not_found), getString(R.string.ok),
							R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

								@Override
								public void NeutralMathod() {

								}
							});
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private class Detector extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {

			if (mapview.getZoomLevel() > 5) {
				GeoPoint p = mapview.getProjection().fromPixels((int) event.getX(), (int) event.getY());
				double lat = p.getLatitudeE6() / 1E6;
				double lng = p.getLongitudeE6() / 1E6;
				setAddressData(lat, lng);
			} else {
				IjoomerUtilities.getCustomOkDialog(getString(R.string.address_from_map), getString(R.string.minimum_zoom_level_requires_map_address), getString(R.string.ok),
						R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

							@Override
							public void NeutralMathod() {

							}
						});
			}

			return true;
		}
	}

	private SmartListAdapterWithHolder getListAdapter(ArrayList<SmartListItem> data) {

		SmartListAdapterWithHolder adapterWithHolder = new SmartListAdapterWithHolder(this, R.layout.ijoomer_map_address_item, data, new ItemView() {

			@Override
			public View setItemView(final int position, View v, final SmartListItem item, final ViewHolder holder) {

				holder.txtMapAddressData = (IjoomerTextView) v.findViewById(R.id.txtMapAddressData);

				@SuppressWarnings("unchecked")
				final HashMap<String, String> row = (HashMap<String, String>) item.getValues().get(0);

				holder.txtMapAddressData.setText(row.get("address"));

				holder.txtMapAddressData.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent();
						intent.putExtra("MAP_ADDRESSS_DATA", row);
						setResult(Activity.RESULT_OK, intent);
						finish();
					}
				});

				return v;
			}

			@Override
			public View setItemView(int position, View v, SmartListItem item) {
				return null;
			}

		});
		return adapterWithHolder;

	}

	public ArrayList<SmartListItem> prepareList(ArrayList<HashMap<String, String>> data) {
		ArrayList<SmartListItem> listData = new ArrayList<SmartListItem>();
		for (HashMap<String, String> hashMap : data) {
			SmartListItem item = new SmartListItem();
			item.setItemLayout(R.layout.ijoomer_map_address_item);
			ArrayList<Object> obj = new ArrayList<Object>();
			obj.add(hashMap);
			item.setValues(obj);
			listData.add(item);
		}
		return listData;
	}

	@Override
	public View setLayoutView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setHeaderLayoutId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] setTabItemNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setTabBarDividerResId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setTabItemLayoutId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] setTabItemOnDrawables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] setTabItemOffDrawables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] setTabItemPressDrawables() {
		// TODO Auto-generated method stub
		return null;
	}
}
