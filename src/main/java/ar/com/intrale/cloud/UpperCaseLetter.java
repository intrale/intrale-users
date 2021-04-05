package ar.com.intrale.cloud;

import java.util.Random;

import javax.inject.Singleton;

@Singleton
public class UpperCaseLetter implements CharacterMaker {

	public static final Integer startUpperLetters = 65;
	public static final Integer finishUpperLetters = 90;

	private Random random = new Random();
	
	@Override
	public char[] get() {
		Integer asciiCode = random.ints(startUpperLetters, finishUpperLetters + 1).findFirst().getAsInt();
		return Character.toChars(asciiCode);
	}

}
