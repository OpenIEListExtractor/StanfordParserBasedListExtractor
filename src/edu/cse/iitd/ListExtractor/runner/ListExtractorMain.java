/**
 * 
 */
package edu.cse.iitd.ListExtractor.runner;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.cse.iitd.ListExtractor.domain.ListExtractorConjuncts;
import edu.cse.iitd.ListExtractor.domain.ListExtractorDependencyParse;
import edu.cse.iitd.ListExtractor.helper.ListExtractorRuleBasedExtractor;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * @author swarna
 *
 */
public class ListExtractorMain {

	/**
	 * @param args
	 */
	private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";        

    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");

    private static final LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);

    public Tree parse(String str) {                
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer =
            tokenizerFactory.getTokenizer(
                new StringReader(str));    
        return tokenizer.tokenize();
    }

    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LexicalizedParser lp = LexicalizedParser
		          .loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[] { "-maxLength", "500",
		          "-retainTmpSubcategories" });
		String sentence = "He 's never chuckled , or giggled , or even smiled .";
		String[] sent = sentence.split(" ");
		List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(sent);
		Tree parse = lp.apply(rawWords);
		//parse.pennPrint();
		//System.out.println();
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		//System.out.println(tdl);
		TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
		tp.printTree(parse);
		
		ListExtractorDependencyParse dependencyParse = new ListExtractorDependencyParse(sentence, tdl);
		
		ListExtractorConjuncts conjunctStructures = new ListExtractorConjuncts();
		List<String> sentences = ListExtractorRuleBasedExtractor.getSimpleSentences(dependencyParse, conjunctStructures);
		
		if(sentences.size() == 0) {
			sentences = new ArrayList<String>();
			sentences.add(sentence);
		}
		
		System.out.println(sentence);
		System.out.println(sentences.size());
		
		for(String simpleSentence : sentences) {
			System.out.println(simpleSentence);
		}
	}
}
