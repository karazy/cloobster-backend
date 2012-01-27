package net.eatsense.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.eatsense.domain.Gender;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;
import net.eatsense.restws.NicknameResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Creates funny Nicknames used for checkin.
 * A Nickname consists of an adjective plus a noun.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Singleton
public class NicknameGenerator {

	/**
	 * Holds adjectives.
	 * {@link NicknameAdjective}
	 */
	private List<NicknameAdjective> adjectives;
	// Gender specific hashmap
	/**
	 * Holds adjectives sorted by {@link Gender}.
	 * {@link NicknameAdjective}
	 */
	private HashMap<Gender, List<NicknameAdjective>> adjectivesByGender;

	/**
	 * Holds nouns.
	 * {@link NicknameNoun}
	 */
	private List<NicknameNoun> nouns;
	
	/**
	 * Repository to access adjectives.
	 */
	private NicknameAdjectiveRepository adjectiveRepo;
	
	/**
	 * Repository to access nouns.
	 */
	private NicknameNounRepository nounRepo;

	/**
	 * Used to randomly access adjective and noun lists.
	 */
	private Random random;

	@Inject
	public NicknameGenerator(NicknameAdjectiveRepository adjectiveRepo, NicknameNounRepository nounRepo) {
		this.adjectiveRepo = adjectiveRepo;
		this.nounRepo = nounRepo;
		random = new Random();
		adjectivesByGender = new HashMap<Gender, List<NicknameAdjective>>();
		adjectivesByGender.put(Gender.M, new ArrayList<NicknameAdjective>());
		adjectivesByGender.put(Gender.F, new ArrayList<NicknameAdjective>());
		adjectivesByGender.put(Gender.C, new ArrayList<NicknameAdjective>());
		adjectivesByGender.put(Gender.N, new ArrayList<NicknameAdjective>());
		
		adjectives = new ArrayList<NicknameAdjective>(this.adjectiveRepo.getAll());
		for (NicknameAdjective adj : adjectives) {
			adjectivesByGender.get(adj.getGender()).add(adj);
		}
		
		nouns = new ArrayList<NicknameNoun>(this.nounRepo.getAll());

	}

	/**
	 * Generates a random Nickname.
	 * 
	 * @return
	 */
	public String generateNickname() {
		if (adjectives.size() < 1 || nouns.size() < 1) {
			return "";
		}
		
		StringBuilder nickname = new StringBuilder();
		NicknameNoun noun = null;
		NicknameAdjective adjective = null;
		int i = random.nextInt(nouns.size() - 1);
		noun = nouns.get(i);
		i = random.nextInt(adjectivesByGender.get(noun.getGender()).size() - 1);
		adjective = adjectivesByGender.get(noun.getGender()).get(i);	
		
		nickname.append(adjective.getFragment());
		
		nickname.append(" ");
		nickname.append(noun.getFragment());

		return nickname.toString();
	}

}
