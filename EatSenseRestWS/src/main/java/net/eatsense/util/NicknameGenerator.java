package net.eatsense.util;

import java.util.List;
import java.util.Random;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Nickname;
import net.eatsense.persistence.NicknameRepository;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class generates Nicknames for a {@link CheckIn}. Inspired by
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
 * 
 * @author Frederik Reifschneider
 * 
 */
@Singleton
public class NicknameGenerator {

	private List<Nickname> verbs;
	private List<Nickname> nouns;	
	private NicknameRepository nnRepo;
	
//	private static final char[] symbols = new char[36];
//	private final char[] buf;
	private Random random;
//	private final int NICKNAME_LENGTH = 4;

//	static {
//		for (int idx = 0; idx < 10; ++idx)
//			symbols[idx] = (char) ('0' + idx);
//		for (int idx = 10; idx < 36; ++idx)
//			symbols[idx] = (char) ('a' + idx - 10);
//	}

	@Inject
	public NicknameGenerator(NicknameRepository repository) {
		this.nnRepo = repository;
		random = new Random();
		verbs = nnRepo.getListByProperty("type", "VERB");
		nouns = nnRepo.getListByProperty("type", "NOUN");
		
//		buf = new char[NICKNAME_LENGTH];
	}

	/**
	 * Generates a random Nickname
	 * 
	 * @return
	 */
	public String generateNickname() {
		
		if(verbs.size() < 1 || nouns.size() <1) {
			return "";
		}
		
		StringBuilder nickname = new StringBuilder();
		int i = random.nextInt(verbs.size()-1);
		nickname.append(verbs.get(i).getFragment());
		i = random.nextInt(nouns.size()-1);
		nickname.append(" ");
		nickname.append(nouns.get(i).getFragment());
		
		return nickname.toString();
	}

//	private String nextString() {
//		for (int idx = 0; idx < buf.length; ++idx)
//			buf[idx] = symbols[random.nextInt(symbols.length)];
//		return new String(buf);
//	}

}
