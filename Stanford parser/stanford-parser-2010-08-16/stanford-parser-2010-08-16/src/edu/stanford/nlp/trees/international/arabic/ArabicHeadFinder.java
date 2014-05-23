package edu.stanford.nlp.trees.international.arabic;

import java.util.HashMap;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.AbstractCollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;

/**
 * Find the head of an Arabic tree, using the usual kind of heuristic
 * head finding rules.
 * <p>
 * <i>Implementation notes.</i>
 * TO DO: make sure that -PRD marked elements are always chosen as heads.
 * (Has this now been successfully done or not??)
 * <p>
 * Mona: I added the 8 new Nonterm for the merged DT with its following
 * category as a rule the DT nonterm is right headed, the 8 new nonterm DTs
 * are: DTCD, DTRB, DTRP, DTJJ, DTNN, DTNNS, DTNNP, DTNNPS.
 * This was added Dec 7th, 2004.
 *
 * @author Roger Levy
 * @author Mona Diab
 * @author Christopher Manning (added new stuff for ATBp3v3
 */
public class ArabicHeadFinder extends AbstractCollinsHeadFinder {
  private static final long serialVersionUID = 6203368998430280740L;
  protected TagSet tagSet;

  /* A work in progress. There may well be a better way to parameterize the HeadFinders via tagset. */
  public enum TagSet {
    BIES_COLLAPSED {
      @Override
      String prep()  { return "IN"; }
      @Override
      String noun()  { return "NN"; } // really there should be several here.
      @Override
      String det()  { return "DT"; }
      @Override
      String adj()  { return "JJ"; }
      @Override
      String detPlusNoun()  { return "DTNN"; }  // really there should be several here; major point is that the det part is ignored completely
      @Override
      TreebankLanguagePack langPack()  { return new ArabicTreebankLanguagePack(); }
    },
    ORIGINAL {
      @Override
      String prep()  { return "PREP"; }
      @Override
      String noun()  { return "NOUN"; }
      @Override
      String det()  { return "DET"; }
      @Override
      String adj()  { return "ADJ"; }
      @Override
      String detPlusNoun()  { return ArabicTreebankLanguagePack.detPlusNoun; }
      @Override
      TreebankLanguagePack langPack()  { return new ArabicTreebankLanguagePack(true); }
    };

    abstract String prep();
    abstract String noun();
    abstract String adj();
    abstract String det();
    abstract String detPlusNoun();
    abstract TreebankLanguagePack langPack();

    static TagSet tagSet(String str) {
      if(str.equals("BIES_COLLAPSED"))
        return BIES_COLLAPSED;
      else if(str.equals("ORIGINAL"))
        return ORIGINAL;
      else throw new IllegalArgumentException("Don't know anything about tagset " + str);
    }
  }

  public ArabicHeadFinder() {
    this(new ArabicTreebankLanguagePack());
  }

  /**
   * Construct an ArabicHeadFinder with a String parameter corresponding to the tagset in use.
   * @param tagSet Either "ORIGINAL" or "BIES_COLLAPSED"
   */
  public ArabicHeadFinder(String tagSet) {
    this(TagSet.tagSet(tagSet));
  }

  public ArabicHeadFinder(TagSet tagSet) {
    this(tagSet.langPack(), tagSet);
    //this(new ArabicTreebankLanguagePack(), tagSet);
  }

  public ArabicHeadFinder(TreebankLanguagePack tlp) {
    this(tlp,TagSet.BIES_COLLAPSED);
  }

  protected ArabicHeadFinder(TreebankLanguagePack tlp, TagSet tagSet) {
    super(tlp);
    this.tagSet = tagSet;
    //System.err.println("##testing: noun tag is " + tagSet.noun());

    nonTerminalInfo = new HashMap<String,String[][]>();
    nonTerminalInfo.put("SUBROOT", new String[][]{{"right", "S"}});

    nonTerminalInfo.put("NX", new String[][]{{"left", "DT","DTNN","DTNNS","DTNNP", "DTNNPS", "DTJJ", "DTNOUN_QUANT", "NOUN_QUANT"}});
    nonTerminalInfo.put("ADJP", new String[][]{{"rightdis", tagSet.adj(), "DTJJ", "ADJ_NUM", "DTADJ_NUM", "JJR", "DTJJR"}, {"right", "ADJP", "VN", tagSet.noun(), "NNP", "NOFUNC", "NO_FUNC", "NNPS", "NNS", "DTNN", "DTNNS","DTNNP","DTNNPS","DTJJ", "DTNOUN_QUANT", "NOUN_QUANT"}, {"right", "RB", "CD","DTRB","DTCD"}, {"right", "DT"}}); // sometimes right, sometimes left headed??
    nonTerminalInfo.put("ADVP", new String[][]{{"left", "WRB", "RB", "ADVP", "WHADVP","DTRB"}, {"left", "CD", "RP", tagSet.noun(), "CC", tagSet.adj(), "DTJJ", "ADJ_NUM", "DTADJ_NUM", "IN", "NP", "NNP", "NOFUNC","DTRP","DTNN","DTNNP","DTNNPS","DTNNS","DTJJ", "DTNOUN_QUANT", "NOUN_QUANT"}}); // NNP is a gerund that they called an unknown (=NNP, believe it or not...)
    nonTerminalInfo.put("CONJP", new String[][]{{"right", "IN", "RB", tagSet.noun(),"NNS","NNP", "NNPS", "DTRB", "DTNN", "DTNNS", "DTNNP", "DTNNPS", "DTNOUN_QUANT", "NOUN_QUANT"}});
    nonTerminalInfo.put("FRAG", new String[][]{{"left", tagSet.noun(), "NNPS", "NNP","NNS", "DTNN", "DTNNS", "DTNNP", "DTNNPS", "DTNOUN_QUANT", "NOUN_QUANT"}, {"left", "VBP"}});
    nonTerminalInfo.put("INTJ", new String[][]{{"left", "RP", "UH", "DTRP"}});
    nonTerminalInfo.put("LST", new String[][]{{"left"}});
    nonTerminalInfo.put("NAC", new String[][]{{"left", "NP", "SBAR", "PP", "ADJP", "S", "PRT", "UCP"}, {"left", "ADVP"}}); // note: maybe CC, RB should be the heads?
    nonTerminalInfo.put("NP", new String[][]{{"left", tagSet.noun(), tagSet.detPlusNoun(), "NNS", "NNP", "NNPS", "NP", "PRP", "WHNP", "QP", "WP", "DTNNS", "DTNNPS", "DTNNP", "NOFUNC", "NO_FUNC", "DTNOUN_QUANT", "NOUN_QUANT"}, {"left", tagSet.adj(), "DTJJ", "JJR", "DTJJR", "ADJ_NUM", "DTADJ_NUM"}, {"right", "CD", "DTCD"}, {"left", "PRP$"}, {"right", "DT"}}); // should the JJ rule be left or right?
    nonTerminalInfo.put("PP", new String[][]{{"left", tagSet.prep(), "PP", "PRT", "X"}, {"left", "NNP", "RP", tagSet.noun()}, {"left", "NP"}}); // NN is for a mistaken "fy", and many wsT
    nonTerminalInfo.put("PRN", new String[][]{{"left", "NP"}}); // don't get PUNC
    nonTerminalInfo.put("PRT", new String[][]{{"left", "RP", "PRT", "IN", "DTRP"}});
    nonTerminalInfo.put("QP", new String[][]{{"right", "CD", "DTCD", tagSet.noun(), tagSet.adj(), "NNS", "NNP", "NNPS", "DTNN", "DTNNS", "DTNNP", "DTNNPS", "DTJJ", "DTNOUN_QUANT", "NOUN_QUANT"}});

    nonTerminalInfo.put("S", new String[][]{{"left", "VP", "S"}, {"right", "PP", "ADVP", "SBAR", "UCP", "ADJP"}}); // really important to put in -PRD sensitivity here!
    nonTerminalInfo.put("SQ", new String[][]{{"left", "VP", "PP"}}); // to be principled, we need -PRD sensitivity here too.
    nonTerminalInfo.put("SBAR", new String[][]{{"left", "WHNP", "WHADVP", "WRB", "RP", "IN", "SBAR", "CC", "WP", "WHPP", "ADVP", "PRT", "RB", "X", "DTRB", "DTRP"}, {"left", tagSet.noun(), "NNP", "NNS", "NNPS", "DTNN", "DTNNS", "DTNNP", "DTNNPS", "DTNOUN_QUANT", "NOUN_QUANT"}, {"left", "S"}});
    nonTerminalInfo.put("SBARQ", new String[][]{{"left", "WHNP", "WHADVP", "RP", "IN", "SBAR", "CC", "WP", "WHPP", "ADVP", "PRT", "RB", "X"}, {"left", tagSet.noun(), "NNP", "NNS", "NNPS","DTNN", "DTNNS", "DTNNP", "DTNNPS", "DTNOUN_QUANT", "NOUN_QUANT"}, {"left", "S"}}); // copied from SBAR rule -- look more closely when there's time
    nonTerminalInfo.put("UCP", new String[][]{{"left"}});
    nonTerminalInfo.put("VP", new String[][]{{"left", "VBD", "VBN", "VBP", "VBG", "DTVBG", "VN", "DTVN", "VP", "RB", "X","VB"}, {"left", "IN"}, {"left", "NNP","NOFUNC", tagSet.noun(), "DTNN", "DTNNP", "DTNNPS", "DTNNS", "DTNOUN_QUANT", "NOUN_QUANT"}}); // exclude RP because we don't want negation markers as heads -- no useful information?
    //also, RB is used as gerunds

    nonTerminalInfo.put("WHADVP", new String[][]{{"left", "WRB", "WP"}, {"right", "CC"}, {"left", "IN"}});
    nonTerminalInfo.put("WHNP", new String[][]{{"right", "WP"}});
    nonTerminalInfo.put("WHPP", new String[][]{{"left",  "IN", "RB"}});
    nonTerminalInfo.put("X", new String[][]{{"left"}});

    //Added by Mona 12/7/04 for the newly created DT nonterm cat
    nonTerminalInfo.put("DTNN", new String[][]{{"right"}});
    nonTerminalInfo.put("DTNNS", new String[][]{{"right"}});
    nonTerminalInfo.put("DTNNP", new String[][]{{"right"}});
    nonTerminalInfo.put("DTNNPS", new String[][]{{"right"}});
    nonTerminalInfo.put("DTJJ", new String[][]{{"right"}});
    nonTerminalInfo.put("DTRP", new String[][]{{"right"}});
    nonTerminalInfo.put("DTRB", new String[][]{{"right"}});
    nonTerminalInfo.put("DTCD", new String[][]{{"right"}});
    nonTerminalInfo.put("DTIN", new String[][]{{"right"}});

    // stand-in dependency:
    nonTerminalInfo.put("EDITED", new String[][]{{"left"}});
    nonTerminalInfo.put("ROOT", new String[][]{{"left"}});

    // one stray SINV in the training set...garbage head rule here.
    nonTerminalInfo.put("SINV", new String[][]{{"left","ADJP","VP"}});
  }


  private final Pattern predPattern = Pattern.compile(".*-PRD$");

  /**
   * Predicatively marked elements in a sentence should be noted as heads
   */
  @Override
  protected Tree findMarkedHead(Tree t) {
    String cat = t.value();
    if (cat.equals("S")) {
      Tree[] kids = t.children();
      for (Tree kid : kids) {
        if (predPattern.matcher(kid.value()).matches()) {
          return kid;
        }
      }
    }
    return null;
  }

} // end class ArabicHeadFinder
