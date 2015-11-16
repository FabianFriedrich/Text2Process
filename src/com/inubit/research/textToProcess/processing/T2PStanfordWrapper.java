package com.inubit.research.textToProcess.processing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.inubit.research.textToProcess.text.T2PSentence;
import com.inubit.research.textToProcess.text.Text;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Test;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

/**
 * A Wrapper for the calls to the stanford API 
 * @author ff
 *
 */
public class T2PStanfordWrapper {
	
	private DocumentPreprocessor f_dpp = new DocumentPreprocessor();
	private LexicalizedParser f_parser;
	private TreebankLanguagePack f_tlp;
    private GrammaticalStructureFactory f_gsf;
	/**
	 * 
	 */
	public T2PStanfordWrapper() {
		try {
			ObjectInputStream in;
		    InputStream is;
		    URL u = T2PStanfordWrapper.class.getResource("/englishFactored.ser.gz");
		    if(u == null){
		    	//opening from IDE
		    	is = new FileInputStream(new File("resources/englishFactored.ser.gz"));		    		    	
		    }else{
		    	//opening from jar
		    	URLConnection uc = u.openConnection();
			    is = uc.getInputStream(); 				    
		    }
		    in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(is)));  
		    f_parser = new LexicalizedParser(in);
			f_tlp = new PennTreebankLanguagePack(); //new ChineseTreebankLanguagePack();
		    f_gsf = f_tlp.grammaticalStructureFactory();
		}catch(Exception ex) {
			ex.printStackTrace();
		}	    
		//option flags as in the Parser example, but without maxlength
		f_parser.setOptionFlags(new String[]{"-retainTmpSubcategories"});				
		//f_parser.setOptionFlags(new String[]{"-segmentMarkov"});				
		Test.MAX_ITEMS = 4000000; //enables parsing of long sentences
	}
	
	public Text createText(File f){
		return createText(f,null);
	}
	
	public Text createText(File f, ITextParsingStatusListener listener){
		try{
			Text _result = new Text();
			List<List<? extends HasWord>> _sentences = f_dpp.getSentencesFromText(f.getAbsolutePath());
//			BufferedReader _r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "Unicode"));
//			ArrayList<List<? extends HasWord>> _sentences = new ArrayList<List<? extends HasWord>>();
//			String s;
//			while((s = _r.readLine()) != null) {
//				ArrayList<Word> sent = new ArrayList<Word>();
//				sent.add(new Word(s));
//				_sentences.add(sent);
//			}
			
			if(listener != null) listener.setNumberOfSentences(_sentences.size());
			int _sentenceNumber = 1;
			int sentenceOffset = 0;
			for(List<? extends HasWord> _sentence:_sentences){
				if(_sentence.get(0).word().equals("#")) {
					//comment line - skip
					if(listener != null) listener.sentenceParsed(_sentenceNumber++);
					sentenceOffset += ((Word)_sentence.get(_sentence.size()-1)).endPosition();
					continue;
				}
				ArrayList<Word> _list = new ArrayList<Word>();
				for(HasWord w:_sentence){
					if(w instanceof Word){
						_list.add((Word) w);
					}else{
						System.out.println("Error occured while creating a Word!");
					}
				}
				T2PSentence _s = createSentence(_list);
				_s.setCommentOffset(sentenceOffset);
				_result.addSentence(_s);	
				if(listener != null) listener.sentenceParsed(_sentenceNumber++);				
			}
			return _result;
		}catch(Exception ex){
			System.out.println("Could not load file: "+f.getPath());
			ex.printStackTrace();
			return null;
		}
	}

	private T2PSentence createSentence(ArrayList<Word> _list) {
		T2PSentence _s = new T2PSentence(_list);
		Tree _parse = f_parser.apply(_s);
		_s.setTree(_parse);
		GrammaticalStructure _gs = f_gsf.newGrammaticalStructure(_parse);
		_s.setGrammaticalStructure(_gs);
		return _s;
	}

}
