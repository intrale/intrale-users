package ar.com.intrale.cloud;

import java.util.Random;

import javax.inject.Singleton;

@Singleton
public class SpecialCharacters implements CharacterMaker {

	private String [] specialCharacters = {"=", "+", "-", "^", "$", "*", ".", "[", "]", "{", "}", "(", ")", "?", "!",  "@", "#",  "%", "&", "/", "\\" , ">", "<", ":", ";", "|", "_",  "~"};	

	private Random random = new Random();
	
	@Override
	public char[] get() {
		Integer index = random.nextInt(specialCharacters.length);
		return specialCharacters[index].toCharArray();
	}

}
