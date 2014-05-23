import java.util.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

class ParserDemo {
  public static void main(String[] args) {
    LexicalizedParser lp = new LexicalizedParser("parsers/englishFactored.ser.gz");
    lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});

    Tree parse = (Tree) lp.apply("Try this sentence, which is slightly longer.");

    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();
    TypedDependency td = tdl.iterator().next();
    TreeGraphNode node = td.dep();
    node = (TreeGraphNode) node.parent();
    node.deepCopy();
    

    
  }

}
