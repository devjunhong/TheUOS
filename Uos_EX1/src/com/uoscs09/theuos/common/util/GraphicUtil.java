package com.uoscs09.theuos.common.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ListAdapter;
import android.widget.ListView;

public class GraphicUtil {

	/** �־��� ����Ʈ���� ��ü �����۵��� �ϳ��� ���յ� ��Ʈ������ �����. */
	public static Bitmap getWholeListViewItemsToBitmap(ListView listview) {
		ListAdapter adapter = listview.getAdapter();
		int itemscount = adapter.getCount();
		int allitemsheight = 0;
		List<Bitmap> bmps = new ArrayList<Bitmap>();

		for (int i = 0; i < itemscount; i++) {
			View childView = adapter.getView(i, null, listview);
			childView.measure(MeasureSpec.makeMeasureSpec(listview.getWidth(),
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED));

			childView.layout(0, 0, childView.getMeasuredWidth(),
					childView.getMeasuredHeight());
			childView.setDrawingCacheEnabled(true);
			childView.buildDrawingCache();
			bmps.add(childView.getDrawingCache());
			allitemsheight += childView.getMeasuredHeight();
		}

		Bitmap bigbitmap = Bitmap.createBitmap(listview.getMeasuredWidth(),
				allitemsheight + itemscount, Bitmap.Config.ARGB_8888);
		bigbitmap.eraseColor(Color.WHITE);
		Canvas bigcanvas = new Canvas(bigbitmap);

		Paint paint = new Paint();
		int iHeight = 0;

		int size = bmps.size();
		Bitmap bmp, line;
		line = Bitmap.createBitmap(listview.getWidth(), 1,
				Bitmap.Config.ARGB_8888);
		line.eraseColor(Color.DKGRAY);
		for (int i = 0; i < size; i++) {
			bmp = bmps.get(i);
			bigcanvas.drawBitmap(bmp, 0, iHeight, paint);
			iHeight += bmp.getHeight();
			bigcanvas.drawBitmap(line, 0, iHeight, paint);
			iHeight += 1;
			bmp.recycle();
			bmp = null;
		}
		line.recycle();

		return bigbitmap;
	}

	/**
	 * �ش� ��Ʈ�� �̹����� ���Ϸ� �����Ѵ�.
	 * 
	 * @return ���� ����
	 */
	public static boolean saveImageToFile(String src, Bitmap img)
			throws IOException, FileNotFoundException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(src);
			return img.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * �־��� �並 ��Ʈ������ ĸ���Ѵ�.<br>
	 * {@code View.getDrawingCache()} �� null�� ��ȯ�Ҷ� ����Ѵ�.
	 * */
	public static Bitmap createBitmapFromView(View v)
			throws IllegalArgumentException {

		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.layout(0, 0, v.getWidth(), v.getHeight());
		v.draw(c);

		return b;
	}

	/** �ΰ��� ��Ʈ���� ��ģ��. bmp1�� ���� ��ġ�Ѵ�. */
	public static Bitmap merge(Bitmap bmp1, Bitmap bmp2) {
		Bitmap cs = null;

		int width, height = 0;

		height = bmp1.getHeight() + bmp2.getHeight();
		width = bmp1.getWidth();

		cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas comboImage = new Canvas(cs);

		comboImage.drawBitmap(bmp1, 0f, 0f, null);
		comboImage.drawBitmap(bmp2, 0f, bmp1.getHeight(), null);

		return cs;
	}
}
