package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import android.util.Log;

import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.tab.booksearch.BookItem;

public class ParseBook extends JerichoParse<BookItem> {
	private static final String HREF = "href";
	private static final String SRC = "src";
	private static final String IFRAME = "iframe";
	private static final String COVER = "cover";
	private static final String LI = "li";

	protected ParseBook(String body) {
		super(body);
	}

	@Override
	protected List<BookItem> parseHttpBody(Source source) throws IOException {
		List<Element> briefList = source.getAllElementsByClass("briefList");
		final List<Element> bookHtmlList = briefList.get(0).getAllElements(LI);

		final int size = bookHtmlList.size();
		if (size > 7) {
			final int halfSize = size / 2;
			FutureTask<ArrayList<BookItem>> task1 = new FutureTask<ArrayList<BookItem>>(new Callable<ArrayList<BookItem>>() {
				@Override
				public ArrayList<BookItem> call() throws Exception {
					return parseListElement(bookHtmlList, 0, halfSize);
				}
			});
			AsyncLoader.excuteFor(task1);
			FutureTask<ArrayList<BookItem>> task2 = new FutureTask<ArrayList<BookItem>>(new Callable<ArrayList<BookItem>>() {
				@Override
				public ArrayList<BookItem> call() throws Exception {
					return parseListElement(bookHtmlList, halfSize, size);
				}
			});
			AsyncLoader.excuteFor(task2);
			ArrayList<BookItem> bookItemList = new ArrayList<BookItem>();
			
			// AsyncTask를 사용한다면 현재 Thread가 interrupt될 가능성이 존재함.
			for(;;){
				try {
					bookItemList.addAll(task1.get());
					break;
				} catch (InterruptedException e){
					Log.e("ParseBook", "interrupted TASK #1");
				} catch (Exception e){
					e.printStackTrace();
					break;
				}
			}
			for(;;){
				try {
					bookItemList.addAll(task2.get());
					break;
				} catch(InterruptedException e){
					Log.e("ParseBook", "interrupted TASK #2");
				} catch (Exception e){
					e.printStackTrace();
					break;
				}
			}
			return bookItemList;
		} else {
			return parseListElement(bookHtmlList, 0, size);
		}
	}

	ArrayList<BookItem> parseListElement(List<Element> bookHtmlList, int start, int end){
		ArrayList<BookItem> bookItemList = new ArrayList<BookItem>();
		for (int i = start; i < end; i++) {
			Element rawBookHtml = bookHtmlList.get(i);
			BookItem item = parseElement(rawBookHtml);
			if (item != null)
				bookItemList.add(item);
		}
		return bookItemList;
	}

	BookItem parseElement(Element rawBookElement) {
		BookItem item = new BookItem();
		Element bookUrl = rawBookElement.getFirstElement(HTMLElementName.A);
		if (bookUrl != null) {
			item.url = bookUrl.getAttributeValue(HREF);
		}

		try {
			Element cover = rawBookElement.getFirstElementByClass(COVER)
					.getFirstElement(IFRAME);
			item.coverSrc = getImgSrc(cover.getAttributeValue(SRC));
		} catch (Exception e) {
		}

		Element title = rawBookElement.getFirstElementByClass("object");
		if (title != null) {
			item.title = title.getTextExtractor().toString();
		}

		List<Element> infoList = rawBookElement.getAllElementsByClass("info");
		if (infoList != null) {
			if (infoList.isEmpty())
				return null;
			Element writer = infoList.get(0);
			item.writer = writer.getTextExtractor().toString();
			Element publisher = infoList.get(1);
			item.bookInfo = publisher.getTextExtractor().toString();

			if (infoList.size() > 2) {
				Element stateAndLocation = infoList.get(2);
				String[] stateAndLocations = stateAndLocation
						.getTextExtractor().toString().split(StringUtil.SPACE);
				item.site = stateAndLocations[0];
				item.bookState = stateAndLocations[1];
			} else {
				List<Element> aElements = rawBookElement
						.getAllElements(HTMLElementName.A);
				if (aElements != null && aElements.size() > 1) {
					Element onlineUrl = aElements.get(1);
					item.site = "http://mlibrary.uos.ac.kr"
							+ onlineUrl.getAttributeValue(HREF);
					item.bookState = "온라인 이용 가능";
				}
			}
		}
		Element bookStateInfos = rawBookElement
				.getFirstElementByClass("downIcon");
		item.bookStateInfoList = null;
		if (bookStateInfos != null) {
			Element a = bookStateInfos.getFirstElement(HTMLElementName.A);
			if (a != null) {
				String functionCallLoc = a.getAttributeValue(HREF);
				String[] params = functionCallLoc.split("', '");

				String sysdCtrl = params[2], location = params[3].split("'")[0];

				String stateInfoUrl = "http://mlibrary.uos.ac.kr/search/prevLoc/"
						+ sysdCtrl + "?loc=" + location;
				item.infoUrl = stateInfoUrl;
			}
		}
		return item;
	}

	private String getImgSrc(String imgUrl) {
		String imgSrc = StringUtil.NULL;
		if (imgUrl == null || imgUrl.equals(StringUtil.NULL))
			return imgSrc;
		try {
			Source source = new Source(HttpRequest.getBody(imgUrl));
			imgSrc = source.getAllElements(HTMLElementName.IMG).get(0)
					.getAttributeValue(SRC);
		} catch (Exception e) {
		}
		return imgSrc;
	}
}
