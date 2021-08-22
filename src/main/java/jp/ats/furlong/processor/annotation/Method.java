package jp.ats.furlong.processor.annotation;

/**
 * @author 千葉 哲嗣
 */
public @interface Method {

	/**
	 * メソッド名
	 * 
	 * @return メソッド名
	 */
	String name();

	/**
	 * メソッドの引数名
	 * 
	 * @return メソッドの引数名
	 */
	String[] args();

	/**
	 * メソッドの引数の型
	 * 
	 * @return メソッドの引数の型
	 */
	Class<?>[] argTypes();

	/**
	 * 戻り値の型パラメータで示される {@link DataObject} クラス
	 * 
	 * @return 戻り値の型パラメータで示される {@link DataObject} クラス
	 */
	Class<?> dataObjectClass() default Object.class;
}
