package com.uoscs09.theuos.common.impl.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fragment �Ǵ� Activity�� �ı��� ��,<br>
 * <b>({@code Fragment.onDetach()}�Ǵ� {@code Activity.onDestroy()} �� ȣ��� ���)</b><br>
 * ������ Field�� ��Ÿ����.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseWhenDestroy {
}
