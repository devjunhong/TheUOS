package com.uoscs09.theuos.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.concurrent.Callable;

import android.content.Context;

import com.uoscs09.theuos.common.impl.AsyncLoader;
import com.uoscs09.theuos.common.impl.AsyncLoader.OnTaskFinishedListener;

public class IOUtil {
	public static final String FILE_TIMETABLE = "timetable_file_v_semester";
	public static final String FILE_COLOR_TABLE = "color_table_file";
	public static final String FILE_REST = "rest_file";

	/**
	 * �־��� �̸��� ������ �о�´�.
	 * 
	 * @return ������ �����ϰ�, ���������� �о���� ��� : �ش� ��ü <br>
	 *         ������ ���ų� ���ܰ� �߻��� ��� : null
	 * @throws IOException
	 * @throws StreamCorruptedException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readFromFile(Context context, String fileName)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream ois = null;
		T object;
		try {
			fis = context.openFileInput(fileName);
			bis = new BufferedInputStream(fis);
			ois = new ObjectInputStream(bis);
			object = (T) ois.readObject();
		} finally {
			closeStream(ois);
			closeStream(bis);
			closeStream(fis);
		}
		return object;
	}

	public static <T> T readFromFileSuppressed(Context context, String fileName) {
		try {
			return readFromFile(context, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean saveToFileSuppressed(Context context,
			String fileName, int mode, Object obj) {
		try {
			return saveToFile(context, fileName, mode, obj);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * �־��� �̸����� ������ �����Ѵ�.
	 * 
	 * @param mode
	 *            Context Ŭ������ mode ����
	 * @param obj
	 *            ������ ��ü
	 * @return ���� ����
	 * @throws IOException
	 */
	public static boolean saveToFile(Context context, String fileName,
			int mode, Object obj) throws IOException {
		boolean state = false;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			fos = context.openFileOutput(fileName, mode);
			bos = new BufferedOutputStream(fos);
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			state = true;
		} finally {
			closeStream(oos);
			closeStream(bos);
			closeStream(fos);
		}
		return state;
	}

	private static void closeStream(Closeable close) {
		if (close != null) {
			try {
				close.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * ������ �� ���������� �д´�. <br>
	 * �������� result�� exception �߻� ���ΰ� ���޵ǰ�, <br>
	 * data�� ���� �� ��� ���ϴ� data, ���� ���� ��� exception�� ���޵ȴ�.
	 */
	public static void readFromFileAsync(final Context context,
			final String fileName, OnTaskFinishedListener l) {
		new AsyncLoader<Object>().excute(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return readFromFile(context, fileName);
			}
		}, l);
	}

	/**
	 * ������ �� ���������� �����Ѵ�. <br>
	 * �������� result�� exception �߻� ���ΰ� ���޵ǰ�, <br>
	 * data�� ���� �� ��� ���� ����, ���� ���� ��� exception�� ���޵ȴ�.
	 */
	public static void saveToFileAsync(final Context context,
			final String fileName, final int mode, final Object obj,
			OnTaskFinishedListener l) {
		new AsyncLoader<Object>().excute(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return saveToFile(context, fileName, mode, obj);
			}
		}, l);
	}

}
