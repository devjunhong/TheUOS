package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.libraryseat.SeatItem;

public class ParseSeat extends JerichoParse<SeatItem> {
	private String roomName = null;
	private String occupySeat = null;
	private String vacancySeat = null;
	private String utilizationRate = null;
	private List<Element> trList;
	private Element tr, td, a, font;
	private String tmp;

	protected ParseSeat(String body) {
		super(body);
	}

	@Override
	protected List<SeatItem> parseHttpBody(Source src) throws IOException {
		List<SeatItem> resultList = new ArrayList<SeatItem>();
		int index = 0;

		List<Element> tableList = src.getAllElements(HTMLElementName.TABLE);
		Element table = tableList.get(1);
		Element dismissTable = tableList.get(3);
		trList = table.getAllElements(HTMLElementName.TR);
		// 0-12 ���͵��
		for (index = 16; index <= 28; index++) {
			resultList.add(parseWeb(index));
		}

		// ! 13 �߾ӵ����� ���͵�� ��Ʈ�� �ڳ�
		resultList.add(parseWeb(10));

		// !14-21 �߾ӵ����� ��1 ������ ~ ��4 ������, ���� ������
		for (index = 2; index <= 9; index++) {
			resultList.add(parseWeb(index));
		}

		// ! 22 �濵�������������� ����������
		resultList.add(parseWeb(15));

		// 23-28 �濵�������������� �׷� ���͵�� 1 ~ 5, ���̳���
		for (index = 29; index <= 34; index++) {
			resultList.add(parseWeb(index));
		}
		// !29-32 ��������������
		for (index = 11; index <= 14; index++) {
			resultList.add(parseWeb(index));
		}

		List<Element> dismissTR = dismissTable
				.getAllElements(HTMLElementName.TR);
		StringBuilder sb = new StringBuilder();
		for (Element e : dismissTR) {
			List<Element> tds = e.getAllElements(HTMLElementName.TD);
			if (tds.size() < 2)
				continue;
			sb.append(tds.get(0).getTextExtractor().toString()
					+ StringUtil.NEW_LINE
					+ tds.get(1).getTextExtractor().toString()
					+ StringUtil.NEW_LINE);
		}
		resultList.add(new SeatItem("�¼� ���� ���� �ð�ǥ", sb.toString(),
				StringUtil.NULL, StringUtil.NULL, -1));
		return resultList;
	}

	private SeatItem parseWeb(int i) {
		roomName = null;
		occupySeat = null;
		vacancySeat = null;
		utilizationRate = null;
		tmp = null;

		tr = trList.get(i);

		// roomName �����Ǹ� ���. ��) �߾ӵ����� �� 1 ������
		td = tr.getAllElements(HTMLElementName.TD).get(1);
		a = td.getFirstElement(HTMLElementName.A);
		tmp = new String(a.getTextExtractor().toString().substring(1));// ���� �տ�
																		// " " ��
																		// ���ڰ�
		// �� ����
		// tmp = new String(tmp.getBytes(), "EUC-KR"); //���� ���� ���� ���ڵ� ����!
		roomName = tmp;

		// occupySeat ��� �¼��� ���. ��) 81
		td = tr.getAllElements(HTMLElementName.TD).get(3);
		font = td.getAllElements(HTMLElementName.FONT).get(0);
		tmp = font.getTextExtractor().toString();
		occupySeat = tmp;

		// vacancySeat �ܿ� �¼��� ���. ��) 317
		td = tr.getAllElements(HTMLElementName.TD).get(4);
		font = td.getAllElements(HTMLElementName.FONT).get(0);
		tmp = font.getTextExtractor().toString();
		vacancySeat = tmp;

		// utilizationRate �̿��� ���. ��) 20.35
		td = tr.getAllElements(HTMLElementName.TD).get(5);
		font = td.getAllElements(HTMLElementName.FONT).get(0);
		tmp = font.getTextExtractor().toString();

		// ���� ���� " %"�� �� ����.
		utilizationRate = new String(tmp.substring(0, tmp.length() - 2));

		return new SeatItem(roomName, occupySeat, vacancySeat, utilizationRate,
				indexToRoomNumber(i));
	}

	private int indexToRoomNumber(int idx) {
		if (idx < 10)
			return idx - 1;
		else if (idx < 16)
			return idx + 1;
		else
			return idx + 5;
	}
}
