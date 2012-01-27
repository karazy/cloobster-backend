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

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Creates funny Nicknames used for checkin. A Nickname consists of an adjective
 * plus a noun.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Singleton
public class NicknameGenerator {

	// Gender specific hashmap
	/**
	 * Holds adjectives sorted by {@link Gender}. {@link NicknameAdjective}
	 */
	private HashMap<String, HashMap<Gender, List<NicknameAdjective>>> adjectivesByGender;

	/**
	 * Holds nouns. {@link NicknameNoun}
	 * key: language
	 * value: nouns
	 */
	private HashMap<String, List<NicknameNoun>> nouns;

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
		
		loadOrRefreshNicknames();
	}

	/**
	 * Generates a random Nickname.
	 * @param lang
	 * 		Language for which to create the nickname. e.g. EN
	 * @return
	 */
	public String generateNickname(String lang) {
		if (lang == null || nouns.get(lang) == null || nouns.get(lang).size() < 1) {
			return "";
		}
		
		//make sure always use uppercase language abbreviations
		lang = lang.toUpperCase();

		StringBuilder nickname = new StringBuilder();
		NicknameNoun noun = null;
		NicknameAdjective adjective = null;
		//get noun depending on language
		int i = random.nextInt(nouns.get(lang).size() - 1);
		noun = nouns.get(lang).get(i);
		
		//check if adjectives for this language exist
		if(adjectivesByGender.get(lang) != null || adjectivesByGender.get(lang).size() < 1) {
			i = random.nextInt(adjectivesByGender.get(lang).get(noun.getGender()).size() - 1);

			if (i >= 0) {
				adjective =  adjectivesByGender.get(lang).get(noun.getGender()).get(i);
				nickname.append(adjective.getFragment());
				nickname.append(" ");
			}
		}


		nickname.append(noun.getFragment());

		return nickname.toString();
	}
	
	/**
	 * Generates a random Nickname.
	 * @return
	 */
	public String generateNickname() {
		//TODO remove hardcoded language and method
		return generateNickname("DE");
	}

	/**
	 * Loads or reloads nickname data from datastore.
	 */
	public void loadOrRefreshNicknames() {
		if (nouns != null) {
			nouns.clear();
		}
		nouns = null;		
		nouns = new HashMap<String, List<NicknameNoun>>();
		
		ArrayList<NicknameNoun> allNouns = new ArrayList<NicknameNoun>(this.nounRepo.getAll());
		for (NicknameNoun noun : allNouns) {
			//create new noun list for each language
			if(nouns.get(noun.getLang()) == null) {
				nouns.put(noun.getLang(), new ArrayList<NicknameNoun>());
			}
			nouns.get(noun.getLang()).add(noun);
		}
	
		
		if (adjectivesByGender != null) {
			adjectivesByGender.clear();
		}
		adjectivesByGender = null;

		adjectivesByGender = new HashMap<String, HashMap<Gender,List<NicknameAdjective>>>();
		
		List<NicknameAdjective> adjectives = new ArrayList<NicknameAdjective>(this.adjectiveRepo.getAll());
		
		for (NicknameAdjective adj : adjectives) {
			//create new adjective map for each language
			if(adjectivesByGender.get(adj.getLang()) == null) {
				adjectivesByGender.put(adj.getLang(), new HashMap<Gender, List<NicknameAdjective>>());
				adjectivesByGender.get(adj.getLang()).put(Gender.M, new ArrayList<NicknameAdjective>());
				adjectivesByGender.get(adj.getLang()).put(Gender.F, new ArrayList<NicknameAdjective>());
				adjectivesByGender.get(adj.getLang()).put(Gender.C, new ArrayList<NicknameAdjective>());
				adjectivesByGender.get(adj.getLang()).put(Gender.N, new ArrayList<NicknameAdjective>());
			}
			adjectivesByGender.get(adj.getLang()).get(adj.getGender()).add(adj);
		}

	}

}
