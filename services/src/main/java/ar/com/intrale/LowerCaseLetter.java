package ar.com.intrale;

import java.util.Random;

import javax.inject.Singleton;

@Singleton
public class LowerCaseLetter implements CharacterMaker {

	public static final Integer startLowerLetters = 97;
	public static final Integer finishLowerLetters = 122;

	private Random random = new Random();
	
	@Override
	public char[] get() {
		Integer asciiCode = random.ints(startLowerLetters, finishLowerLetters + 1).findFirst().getAsInt();
		return Character.toChars(asciiCode);
	}

}
