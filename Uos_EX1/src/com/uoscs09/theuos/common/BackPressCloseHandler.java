package com.uoscs09.theuos.common;

/**
 * source from JavaCan : 
 * {@link http://javacan.tistory.com/entry/close-androidapp-by-successive-back-press}<br>
 * �ڷΰ��� ��ư�� �ι� ���� ���� �����Ű�� ���� ó���ϴ� Ŭ����
 */
public class BackPressCloseHandler {
	private long backKeyPressedTime = 0;

	/** @return �ڷ� ��ư�� ó�� ���� ���� 2�� ���� �ٽ� ������ true, 2�ʰ� ���� �� ������ false */
	public boolean onBackPressed() {
		long current = System.currentTimeMillis();
		if (current <= backKeyPressedTime + 2000) {
			return true;
		} else {
			backKeyPressedTime = current;
			return false;
		}
	}
}
