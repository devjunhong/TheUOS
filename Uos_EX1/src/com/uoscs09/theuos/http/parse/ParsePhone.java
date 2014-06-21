package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.tab.phonelist.PhoneItem;

import net.htmlparser.jericho.*;

public class ParsePhone extends JerichoParse<PhoneItem> {
	private int howTo;
	private String LOCAL_NUMBER = "02)";

	public ParsePhone(String body, int howTo) {
		super(body);
		this.howTo = howTo;
	}

	@Override
	protected List<PhoneItem> parseHttpBody(Source source)
			throws IOException {
		ArrayList<PhoneItem> itemList = new ArrayList<PhoneItem>();

		switch (howTo) {
		case ParseFactory.Value.BOTTOM: // ��Ÿ �ֿ�ü� �Ľ�(������ �Ʒ��κ��� ��ȭ��ȣ �Ľ�)
		{
			Element div = source.getElementById("chargeBorder");
			List<Element> list = div.getAllElementsByClass("ml10");
			String name, tel;
			name = tel = StringUtil.NULL;
			int i = 0;
			for (Element li : list) {
				String string = li.getContent().getTextExtractor().toString();
				if (i == 0) {
					name = string;
				} else if (string.startsWith(LOCAL_NUMBER)) {
					tel = string;
					tel = tel.replace(LOCAL_NUMBER, "02-");
				}
				i++;
			}
			itemList.add(new PhoneItem(name, tel));
		}
			break;
		case ParseFactory.Value.BODY: // �� ���ǽü� ��ȭ��ȣ �Ľ�
		{
			List<Element> div = source.getAllElementsByClass("floatL ml16");
			for (Element element : div) {
				// ��� �̸� ����
				Element img = element.getAllElements(HTMLElementName.IMG)
						.get(0);
				String site = img.getAttributeValue("title");
				site = site.replace("(�ѹ̸���Ʈ)", StringUtil.NULL);
				if (site == null) {
					throw new IOException();
				}
				// ��ȭ��ȣ ����
				Element telNumber = element.getAllElementsByClass("d3").get(1);
				String tel2 = telNumber.getContent().getTextExtractor()
						.toString();
				String telNumberString = tel2.replace("��ȭ��ȣ : 02) ", "02-");
				if (telNumberString.startsWith("��ȭ��ȣ")) {
					telNumberString = telNumberString.replace(
							"��ȭ��ȣ : ������ 02) ", "������ 02-");
					String[] array = telNumberString.split(StringUtil.SPACE);
					telNumberString = array[0] + StringUtil.SPACE + array[1]
							+ StringUtil.NEW_LINE + array[2] + StringUtil.SPACE
							+ array[3];
				}
				PhoneItem item = new PhoneItem(site, telNumberString);
				itemList.add(item);
			}
		}
			break;
		case ParseFactory.Value.SUBJECT: // �� �а�/�к� �繫�� ��ȭ��ȣ �Ľ�
		{
			List<Element> div_site = source.getAllElementsByClass("floatL");
			List<Element> div_number = source
					.getAllElementsByClass("floatL mt3 ml10 mb14 cTel");
			ArrayList<String> list_site = new ArrayList<String>();
			ArrayList<String> list_num = new ArrayList<String>();
			for (Element element : div_site) {
				// ��� �̸� ����
				try {
					Element li = element.getAllElementsByClass("d1").get(0);
					Element img = li.getAllElements(HTMLElementName.IMG).get(0);
					// title �Ӽ��� ������ ���� ������
					// Ȩ�������� �߸� �Է��� �κ��� �־
					// alt�� ������
					String site = img.getAttributeValue("alt");
					list_site.add(site);
				} catch (Exception e) {
				}
			}
			for (Element element : div_number) {
				// ��ȭ��ȣ ����
				String telNumber = element.getContent().getTextExtractor()
						.toString();
				String[] array = telNumber.split(StringUtil.SPACE);
				telNumber = StringUtil.NULL;
				int i = 0;
				for (String string : array) {
					if (i == 2) {
						telNumber += string.replace(')', '-');
						telNumber = StringUtil.remove(telNumber,
								StringUtil.CODE_R_PRNTSIS);
					} else if (i > 2) {
						telNumber += string;
					}
					i++;
				}
				telNumber = StringUtil.remove(telNumber, "]");
				list_num.add(telNumber);
			}
			int size = list_site.size() > list_num.size() ? list_num.size()
					: list_site.size();
			for (int i = 0; i < size; i++) {
				String site = list_site.get(i);
				if (itemList.indexOf(site) == -1) {
					String telNumberString = list_num.get(i);
					PhoneItem item = new PhoneItem(site, telNumberString);
					itemList.add(item);
				}
			}
		}
			break;
		case ParseFactory.Value.CULTURE: {
			ArrayList<String> list_site = new ArrayList<String>();
			ArrayList<String> list_num = new ArrayList<String>();
			List<Element> div_site = source.getAllElementsByClass("m0p0");
			for (Element element : div_site) {
				// ��� �̸� ����
				try {
					Element li = element.getAllElementsByClass("d1").get(0)
							.getAllElements(HTMLElementName.IMG).get(0);
					String site = li.getAttributeValue("alt");
					list_site.add(site);
				} catch (Exception e) {
				}
			}
			Pattern p = Pattern.compile("\\[�а��繫�� :.+\\]");
			Matcher m = p.matcher(source);
			while (m.find()) {
				list_num.add(StringUtil.removeRegex(m.group().split(":")[1],
						"( |\\])").replace(")", "-"));
			}

			int size = list_site.size() > list_num.size() ? list_num.size()
					: list_site.size();
			for (int i = 0; i < size; i++) {
				String site = list_site.get(i);
				if (itemList.indexOf(site) == -1) {
					String telNumberString = list_num.get(i);
					PhoneItem item = new PhoneItem(site, telNumberString);
					itemList.add(item);
				}
			}
		}
			break;
		default:
			break;
		}
		itemList.trimToSize();
		return itemList;
	}
}
