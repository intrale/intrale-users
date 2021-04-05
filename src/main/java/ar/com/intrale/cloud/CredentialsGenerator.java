package ar.com.intrale.cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CredentialsGenerator {

	@Inject
	private UpperCaseLetter upperCaseLetter;
	@Inject
	private LowerCaseLetter lowerCaseLetter;
	@Inject	
	private Numbers numbers;
	@Inject
	private SpecialCharacters specialCharacters;
	
	private List<CharacterMaker> makers = new ArrayList<CharacterMaker>();
	
	@PostConstruct
	public void initialize() {
		makers.add(upperCaseLetter);
		makers.add(lowerCaseLetter);
		makers.add(numbers);
		makers.add(specialCharacters);
	}
	
	public String generate(Integer size) {
		
		StringBuilder generated = new StringBuilder();
		
		while (generated.length()<size) {
			Collections.shuffle(makers);
			Iterator<CharacterMaker> it = makers.iterator();
			while (it.hasNext()) {
				CharacterMaker characterMaker = (CharacterMaker) it.next();
				generated.append(characterMaker.get());
			}
		}
		
		return generated.toString();
	}
	
}
