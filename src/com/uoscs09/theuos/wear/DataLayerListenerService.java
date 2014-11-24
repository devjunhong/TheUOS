package com.uoscs09.theuos.wear;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.tab.libraryseat.TabLibrarySeatFragment;

public class DataLayerListenerService extends WearableListenerService implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {
	private static final String PATH_LIBRARY_SEAT = "/data/library_seat";
	private GoogleApiClient mGoogleClient;

	@Override
	public void onDestroy() {
		super.onDestroy();
		mGoogleClient.disconnect();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mGoogleClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		mGoogleClient.connect();
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		Log.d("TheUosHost",
				"receive From Wearable " + messageEvent.getSourceNodeId());
		Log.d("TheUosHost",
				"receive From Wearable Path :" + messageEvent.getPath());
		if (PATH_LIBRARY_SEAT.equals(messageEvent.getPath())) {
			sendMessageToWearable(PATH_LIBRARY_SEAT);
		}
		super.onMessageReceived(messageEvent);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
	}

	@Override
	public void onConnected(Bundle connectionHint) {
	}

	@Override
	public void onConnectionSuspended(int cause) {
	}

	private void sendMessageToWearable(final String path) {
		AsyncLoader.excute(new Runnable() {

			@Override
			public void run() {
				NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
						.getConnectedNodes(mGoogleClient).await();

				for (Node node : nodes.getNodes()) {
					try {
						Log.d("TheUosHost",
								"send To Wearable " + node.getDisplayName());
						SendMessageResult result = Wearable.MessageApi
								.sendMessage(mGoogleClient, node.getId(), path,
										getMessageByPath(path)).await();
						if (!result.getStatus().isSuccess()) {
							Log.e("TheUosHost",
									"ERROR: failed to send Message: "
											+ result.getStatus());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	private byte[] getMessageByPath(String path) throws Exception {
		if (PATH_LIBRARY_SEAT.equals(path)) {
			return IOUtil.toByteArray(TabLibrarySeatFragment
					.getLibaraySeatList());

		}
		return null;
	}
}
