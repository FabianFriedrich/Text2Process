package net.didion.jwnl.princeton.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Adjective;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

import java.util.Map;

public class PrincetonWN16DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {
	public Object create(Map params) throws JWNLException {
		return new PrincetonWN16DatabaseDictionaryElementFactory();
	}

	protected Word createWord(Synset synset, int index, String lemma) {
		if (synset.getPOS().equals(POS.ADJECTIVE)) {
			Adjective.AdjectivePosition adjectivePosition = Adjective.NONE;
			if (lemma.charAt(lemma.length() - 1) == ')' && lemma.indexOf('(') > 0) {
				int lparen = lemma.indexOf('(');
				String marker = lemma.substring(lparen + 1, lemma.length() - 1);
				adjectivePosition = Adjective.getAdjectivePositionForKey(marker);
				lemma = lemma.substring(0, lparen);
			}
			return new Adjective(synset, index, lemma, adjectivePosition);
		} else {
			return super.createWord(synset, index, lemma);
		}
	}
}
