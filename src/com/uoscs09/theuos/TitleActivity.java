package com.uoscs09.theuos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TitleActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title);

		getWindow().getDecorView().postDelayed(new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(getApplicationContext(),
						PagerFragmentActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		}, 400);
	}
}
