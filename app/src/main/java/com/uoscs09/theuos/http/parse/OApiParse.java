package com.uoscs09.theuos.http.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OApiParse<ReturnType, ParseType> implements IParseHttp<List<ReturnType>> {
	protected static final String CDATA = "CDATA";
	protected static final String PTN = "(.|\\n|\\r)*?";
	protected static final String LIST = "<list>";
	protected String body;

	protected OApiParse(String body) {
		this.body = body;
	}

	protected static String getPattern(String str) {
		StringBuilder sb = new StringBuilder();
		sb.append('<').append(str).append('>').append(PTN).append('<')
				.append('/').append(str).append('>');
		return sb.toString();
	}

	protected ArrayList<ParseType> parseToArrayList(String[] splitedBody,String[] PATTERNS) {
		Pattern p;
		Matcher m;
		ArrayList<ParseType> itemList = new ArrayList<>();
		ArrayList<String> list = new ArrayList<>();

		for (String str : splitedBody) {
			try {
				for (String ptn : PATTERNS) {
					p = Pattern.compile(getPattern(ptn));
					m = p.matcher(str);
					while (m.find()) {
						list.add(removePattern(ptn, m.group()));
					}
				}
				if (list.size() != 0)
					initItem(list, itemList);
			} catch (Exception e) {
                e.printStackTrace();
			} finally {
				list.clear();
			}
		}
		return itemList;
	}

	protected static String removePattern(String ptn, String str) {
		if (str.contains(CDATA)) {
			return str.substring(ptn.length() + 11, str.length() - ptn.length()- 6);
		} else {
			return str.substring(ptn.length() + 2, str.length() - ptn.length()- 3);
		}
	}

	protected abstract void initItem(ArrayList<String> parsedStringList, ArrayList<ParseType> returningList);
}
