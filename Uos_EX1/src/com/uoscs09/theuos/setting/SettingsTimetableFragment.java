package com.uoscs09.theuos.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;

public class SettingsTimetableFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private AlertDialog timetableLimitPickerDialog;
	private int limit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().setTitle(R.string.setting_timetable);
		addPreferencesFromResource(R.xml.prefrence_timetable);
		bindPreferenceSummaryToValue();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		switch (preference.getTitleRes()) {
		case R.string.setting_timetable_limit:
			showNumberPicker();
			return true;
		default:
			return false;
		}
	}

	private void bindPreferenceSummaryToValue() {
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
		String[] keys = { PrefUtil.KEY_TIMETABLE_LIMIT, };
		for (String key : keys) {
			onSharedPreferenceChanged(pref, key);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(PrefUtil.KEY_TIMETABLE_LIMIT)) {
			Preference connectionPref = findPreference(key);
			int limit = sharedPreferences.getInt(key, 15);
			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.setting_timetable_limit_desc));
			if (limit == 15) {
				sb.append("\n* ��� ���ð� ǥ�õ˴ϴ�.");
			} else {
				sb.append("\n* ").append(limit).append("���� ���� ǥ�õ˴ϴ�.");
			}
			connectionPref.setSummary(sb.toString());
		}
	}

	/** �ð�ǥ ǥ�����ѿ� ���Ǵ� NumberPicker�� �����ش�. ���� null�̶�� �ʱ�ȭ�� �Ѵ�. */
	private void showNumberPicker() {
		if (timetableLimitPickerDialog == null) {
			NumberPicker np = new NumberPicker(getActivity());
			np.setMaxValue(PrefUtil.TIMETABLE_LIMIT_MAX);
			np.setMinValue(PrefUtil.TIMETABLE_LIMIT_MIN);
			np.setOnValueChangedListener(new OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal,
						int newVal) {
					limit = newVal;
				}
			});
			timetableLimitPickerDialog = new AlertDialog.Builder(getActivity())
					.setView(np)
					.setTitle("ǥ�� �� ���ø� �����ϼ���.")
					.setPositiveButton(android.R.string.ok,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (limit < PrefUtil.TIMETABLE_LIMIT_MIN) {
										limit = PrefUtil.TIMETABLE_LIMIT_MIN;
									} else if (limit > PrefUtil.TIMETABLE_LIMIT_MAX) {
										limit = PrefUtil.TIMETABLE_LIMIT_MAX;
									}
									Context context = getActivity();
									PrefUtil.getInstance(context)
											.put(PrefUtil.KEY_TIMETABLE_LIMIT,
													limit);
									AppUtil.showToast(context,
											String.valueOf(limit)
													+ "���� ���� ǥ�õ˴ϴ�.", true);
									AppUtil.timetable_limit = limit;
									onSharedPreferenceChanged(
											getPreferenceScreen()
													.getSharedPreferences(),
											PrefUtil.KEY_TIMETABLE_LIMIT);
								}
							}).create();
		}
		timetableLimitPickerDialog.show();
	}
}
