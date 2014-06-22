package com.uoscs09.theuos.tab.phonelist;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;

public class PhoneNumberDB {
	private SQLiteDatabase db;
	private static final String TABLE_Name = "PhoneNumberList";
	private static final String TABLE_AttrSite = "Site";
	private static final String TABLE_AttrPhone = "PhoneNumber";
	private PrefUtil pref;
	private static PhoneNumberDB instance = null;

	public synchronized static PhoneNumberDB getInstance(Context context) {
		if (instance == null)
			instance = new PhoneNumberDB(context);
		return instance;
	}

	private PhoneNumberDB(Context context) {
		this.db = context.getApplicationContext().openOrCreateDatabase(
				AppUtil.DB_PHONE, 0, null);
		pref = PrefUtil.getInstance(context);
		createDB();
	}

	public boolean createDB() {
		if (!pref.get(TABLE_AttrPhone, false)) {
			try {
				db.execSQL("CREATE TABLE PhoneNumberList (Site text, PhoneNumber text, PRIMARY KEY(Site))");
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				init();
				pref.put(TABLE_AttrSite, true);
			}
		} else {
			return true;
		}
	}

	public synchronized boolean insert(PhoneItem item) {
		try {
			ContentValues value = new ContentValues();
			value.put(TABLE_AttrSite, item.siteName);
			value.put(TABLE_AttrPhone, item.sitePhoneNumber);
			db.insert(TABLE_Name, null, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public synchronized int update(PhoneItem item) {
		ContentValues value = new ContentValues();
		value.put(TABLE_AttrSite, item.siteName);
		value.put(TABLE_AttrPhone, item.sitePhoneNumber);
		int count = db.update(TABLE_Name, value, "Site = '" + item.siteName
				+ "'", null);
		return count;
	}

	/**
	 * @return update : update�� row ���� <br>
	 *         insert : ���� -> -1, ���� -> -2
	 */
	public int insertOrUpdate(PhoneItem item) {
		if (read(item.siteName) != null) {
			return update(item);
		} else {
			return insert(item) ? -1 : -2;
		}
	}

	public synchronized int delete(String siteName) {
		return db.delete(TABLE_Name, TABLE_AttrSite + " = '" + siteName + "'",
				null);
	}

	public PhoneItem read(String siteName) {
		String query = " WHERE " + TABLE_AttrSite + " = '" + siteName + "'";
		try {
			return select(query).get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<PhoneItem> readAll() {
		return select(StringUtil.NULL);
	}

	private synchronized ArrayList<PhoneItem> select(String query) {
		ArrayList<PhoneItem> itemList = null;
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM " + TABLE_Name + " " + query, null);
			if (c.getCount() > 0) {
				itemList = new ArrayList<PhoneItem>();
				c.moveToFirst();
				do {
					String site = c.getString(0);
					String number = c.getString(1);
					PhoneItem item = new PhoneItem(site, number);
					itemList.add(item);
					c.moveToNext();
				} while (!c.isAfterLast());
			}
		} finally {
			if (c != null)
				c.close();
		}
		return itemList;
	}

	public synchronized void close() {
		try {
			db.close();
			pref = null;
			instance = null;
		} catch (Exception e) {
		}
	}

	private synchronized void init() {
		if (!pref.get(TABLE_Name, false)) {
			insert(new PhoneItem("�������", "02-6490-2003"));
			insert(new PhoneItem("�����а�", "02-6490-2010"));
			insert(new PhoneItem("���������а�", "02-6490-2035"));
			insert(new PhoneItem("�����к�", "02-6490-2051"));
			insert(new PhoneItem("��ȸ�����а�", "02-6490-2075"));
			insert(new PhoneItem("�����а�", "02-6490-2095"));
			insert(new PhoneItem("���к�", "02-6490-2110"));

			insert(new PhoneItem("�濵����", "02-6490-2201"));
			insert(new PhoneItem("�濵�к�", "02-6490-2210"));

			insert(new PhoneItem("��������", "02-6490-2304"));
			insert(new PhoneItem("����������ǻ�Ͱ��к�", "02-6490-2310"));
			insert(new PhoneItem("ȭ�а��а�", "02-6490-2360"));
			insert(new PhoneItem("����������а�", "02-6490-2380"));
			insert(new PhoneItem("�ż�����а�", "02-6490-2400"));
			insert(new PhoneItem("�����а�", "02-6490-2420"));
			insert(new PhoneItem("��ǻ�Ͱ��к�", "02-6490-2440"));

			insert(new PhoneItem("�ι�����", "02-6490-2505"));
			insert(new PhoneItem("������а�", "02-6490-2510"));
			insert(new PhoneItem("������а�", "02-6490-2530"));
			insert(new PhoneItem("�����а�", "02-6490-2550"));
			insert(new PhoneItem("ö�а�", "02-6490-2570"));
			insert(new PhoneItem("�߱��ȭ�а�", "02-6490-2586"));

			insert(new PhoneItem("�ڿ����д���", "02-6490-2601"));
			insert(new PhoneItem("���а�", "02-6490-2606"));
			insert(new PhoneItem("����а�", "02-6490-2625"));
			insert(new PhoneItem("�����а�", "02-6490-2640"));
			insert(new PhoneItem("������а�", "02-6490-2660"));
			insert(new PhoneItem("ȯ������а�", "02-6490-2680"));

			insert(new PhoneItem("���ð��д���", "02-6490-2702"));
			insert(new PhoneItem("�����к�(�������)", "02-6490-2753"));
			insert(new PhoneItem("�����к�(������)", "02-6490-2751"));
			insert(new PhoneItem("���ð��а�", "02-6490-2790"));
			insert(new PhoneItem("������а�", "02-6490-2815"));
			insert(new PhoneItem("�����а�", "02-6490-2835"));
			insert(new PhoneItem("���������а�", "02-6490-2710"));
			insert(new PhoneItem("���û�ȸ�а�", "02-6490-2730"));
			insert(new PhoneItem("�����������а�", "02-6490-2880"));
			insert(new PhoneItem("ȯ����к�", "02-6490-2853"));

			insert(new PhoneItem("����ü������", "02-6490-2902"));
			insert(new PhoneItem("�����а�", "02-6490-2930"));
			insert(new PhoneItem("����������а�", "02-6490-2906"));
			insert(new PhoneItem("ȯ�������а�", "02-6490-2916"));
			insert(new PhoneItem("���������а�", "02-6490-2945"));

			insert(new PhoneItem("���������к�", "02-6490-2126"));

			insert(new PhoneItem("���米����", "02-6490-5202"));
			insert(new PhoneItem("�۾��⼾��", "02-6490-5274"));
			insert(new PhoneItem("���п����", "02-6490-5206"));
			insert(new PhoneItem("�������", "02-6490-5235"));
			insert(new PhoneItem("������ǻ��", "02-6490-5265"));
			insert(new PhoneItem("���繰��", "02-6490-5245"));
			insert(new PhoneItem("����ȭ��", "02-6490-5255"));
			insert(new PhoneItem("�������", "02-6490-5262"));
			insert(new PhoneItem("����ü��", "02-6490-2945"));

			insert(new PhoneItem("��Ȱ��", "02-6490-5186"));
			insert(new PhoneItem("���к��Ǽ�", "02-6490-6590"));
			insert(new PhoneItem("�л���", "02-6490-6212"));
			insert(new PhoneItem("ü����", "02-6490-5165"));
			insert(new PhoneItem("�л�ȸ�� ������", "02-6490-5861"));
			insert(new PhoneItem("�߾ӵ����� ������", "02-6490-5862"));
			insert(new PhoneItem("��Ȱ�� ������", "02-6490-5863"));
			insert(new PhoneItem("WEB", "02-6490-5865"));
			insert(new PhoneItem("Free Zone", "02-6490-5866"));
			insert(new PhoneItem("�׸���...��", "02-6490-5867"));
			insert(new PhoneItem("�ĸ��ٰԶ�", "02-6490-5864"));
			insert(new PhoneItem("���Ǳ�", "02-6490-5852"));
			insert(new PhoneItem("����", "02-2210-2344"));
			insert(new PhoneItem("����", "02-2210-2344"));
			insert(new PhoneItem("����", "02-2210-2358"));
			insert(new PhoneItem("�Ȱ���", "02-2210-2360"));
			insert(new PhoneItem("���ǰ����", "02-2210-2192"));
			insert(new PhoneItem("��ü��", "02-2210-2293"));

			// DB�� �ʱ�ȭ �Ǿ��ٴ� ǥ�ø� ��.
			pref.put(TABLE_Name, true);
		}

	}

}
