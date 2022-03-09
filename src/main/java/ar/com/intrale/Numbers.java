package ar.com.intrale;

import java.util.Random;

import javax.inject.Singleton;

@Singleton
public class Numbers implements CharacterMaker {

	private static Integer startNumbers = 48;
	private static Integer finishNumbers = 57;

	private Random random = new Random();
	
	@Override
	public char[] get() {
		Integer asciiCode = random.ints(startNumbers, finishNumbers + 1).findFirst().getAsInt();
		return Character.toChars(asciiCode);
	}

}
