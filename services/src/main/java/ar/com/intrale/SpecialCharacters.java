package ar.com.intrale;

import java.util.Random;

import javax.inject.Singleton;

@Singleton
public class SpecialCharacters implements CharacterMaker {

	//private String [] specialCharacters = {"=", "+", "-", "^", "$", "*", ".", "[", "]", "{", "}", "(", ")", "?", "!",  "@", "#",  "%", "&", "/", "\\" , ">", "<", ":", ";", "|", "_",  "~"};	
	private String [] easySpecialCharacters = {"=", "+", "*", "@", "#"};	
	
	//@Inject
	//private TemporaryPasswordConfig temporaryPasswordConfig;
	
	private Random random = new Random();
	
	@Override
	public char[] get() {
		/*if (temporaryPasswordConfig.complex) {
			Integer index = random.nextInt(specialCharacters.length);
			return specialCharacters[index].toCharArray();
		} else {*/
			Integer index = random.nextInt(easySpecialCharacters.length);
			return easySpecialCharacters[index].toCharArray();
		//}
	}

}
