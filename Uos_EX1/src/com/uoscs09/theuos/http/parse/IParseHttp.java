package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.List;
/** �ʿ��� ������ parsing�ϴ� interface*/
public interface IParseHttp {
	/** �־��� ������ parsing�Ͽ� List�� ��ȯ�Ѵ�.*/
	public List<?> parse() throws IOException;
}
