package com.uoscs09.theuos.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;

/**
 * ���� ȭ�鿡�� <b>'�̹��� ���� ���'</b> �� �����ϸ� ��ȯ�Ǵ� Fragment<br>
 * dialog �������� �����ϸ� �Ʒ��� ���� ����� �����Ѵ�.<br>
 * <li>���� ������ ��θ� �����ִ� ���</li> <li>
 * ���丮 Ž�� ���</li> <li>�� ���� ���� ���</li> <li>���丮 ��� ���� ���</li><br>
 * <br>
 * <br>
 * 
 * ���丮 ���ù�ư�� ������ �� ���丮 ��ΰ� preference�� <br>
 * {@code PrefUtil.KEY_SAVE_ROUTE}�� Ű������ �ؼ� ����Ǿ�� �Ѵ�. <br>
 * <br>
 * 
 * ����� ��� preference�� ���� �ٲ��� �ʴ´�.
 */
public class SettingsFileSelectDialogFragment extends DialogFragment {
	protected TextView pathTextView;
	protected String path;
	protected List<File> list;
	protected ArrayAdapter<File> adapter;
	protected final String ROOT = "/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getActivity().getActionBar().setTitle(R.string.setting_save_route);
		list = new ArrayList<File>();
		path = getPathFromPref(getActivity());
	}

	/** Dialog�� View�� �����Ѵ�. */
	private View createView() {
		int layout, listLayout;
		switch (AppUtil.theme) {
		case Black:
			layout = R.layout.dialog_setting_save_route_dark;
			listLayout = R.layout.list_layout_save_route_dark;
			break;
		default:
			layout = R.layout.dialog_setting_save_route;
			listLayout = R.layout.list_layout_save_route;
			break;
		}

		adapter = new FileListAdapter(getActivity(), listLayout, list);
		View rootView = View.inflate(getActivity(), layout, null);
		rootView.findViewById(R.id.dialog_setting_save_route_button_up)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (path.equals(ROOT))
							loadFileListToListView(new File(ROOT));
						else
							loadFileListToListView(new File(path)
									.getParentFile());
					}
				});
		ListView listView = (ListView) rootView
				.findViewById(R.id.dialog_setting_save_route_listView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				loadFileListToListView((File) arg0.getItemAtPosition(position));
			}
		});
		listView.setAdapter(adapter);
		pathTextView = (TextView) rootView
				.findViewById(R.id.dialog_setting_save_route_text_path);
		pathTextView.setText(path);
		return rootView;
	}

	@Override
	public void onResume() {
		loadFileListToListView(new File(path));
		super.onResume();
	}

	protected void loadFileListToListView(File file) {
		if (file == null) {
			AppUtil.showToast(getActivity(),
					R.string.setting_save_route_error_parent, true);
			return;
		}
		if (file.isDirectory()) {
			adapter.clear();
			File[] files = file.listFiles();
			if (files == null)
				return;
			for (File f : files) {
				if (!f.isHidden() && f.isDirectory())
					list.add(f);
			}
			// adapter.addAll(files);

			Collections.sort(list, caseIgnoreComparator);
			adapter.notifyDataSetChanged();
			path = file.getAbsolutePath();
			pathTextView.setText(path);
		}
	}

	protected Comparator<File> caseIgnoreComparator = new Comparator<File>() {
		@Override
		public int compare(File lhs, File rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.setting_save_route)
				.setView(createView())
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								putPathToPref(getActivity(), path);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	protected void putPathToPref(Context context, String path) {
		PrefUtil.getInstance(context).put(PrefUtil.KEY_SAVE_ROUTE, path);
	}

	private String getPathFromPref(Context context) {
		String defaultRoute = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).toString();
		return PrefUtil.getInstance(context).get(PrefUtil.KEY_SAVE_ROUTE,
				defaultRoute);
	}
}
