package net.eatsense.util;

import java.util.Random;

import net.eatsense.domain.CheckIn;

/**
 * This class generates Nicknames for a {@link CheckIn}. Inspired by
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
 * 
 * @author Frederik Reifschneider
 * 
 */
public class NicknameGenerator {

	private static final char[] symbols = new char[36];
	private final char[] buf;
	private final Random random = new Random();
	private final int NICKNAME_LENGTH = 4;

	static {
		for (int idx = 0; idx < 10; ++idx)
			symbols[idx] = (char) ('0' + idx);
		for (int idx = 10; idx < 36; ++idx)
			symbols[idx] = (char) ('a' + idx - 10);
	}

	public NicknameGenerator() {
		buf = new char[NICKNAME_LENGTH];
	}

	/**
	 * Generates a random String
	 * 
	 * @return
	 */
	public static String generateNickname() {
		// TODO generate real nicknames based on a given list
		return new NicknameGenerator().nextString();
	}

	private String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}

}
