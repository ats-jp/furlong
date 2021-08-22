package jp.ats.furlong;

import java.io.Reader;
import java.util.Objects;

/**
 * @author 千葉 哲嗣
 */
public class CharacterStream {

	public final Reader input;

	public final int length;

	public CharacterStream(Reader input, int length) {
		this.input = Objects.requireNonNull(input);
		this.length = length;
	}
}
