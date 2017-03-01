/**
 * 
 */
package edu.cse.iitd.ListExtractor.runner;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.cse.iitd.ListExtractor.domain.ListExtractorConjunctStructure;
import edu.cse.iitd.ListExtractor.domain.ListExtractorConjuncts;
import edu.cse.iitd.ListExtractor.domain.ListExtractorDependencyParse;
import edu.cse.iitd.ListExtractor.helper.ListExtractorIO;
import edu.cse.iitd.ListExtractor.helper.ListExtractorRuleBasedExtractor;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
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
public class ListExtractorMainFile {
	
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
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputFile = args[0];
		String outputFile = args[1];
		
		List<String> inputLines = ListExtractorIO.readFile(inputFile);
		List<String> outputLines = new ArrayList<String>();
		
		LexicalizedParser lp = LexicalizedParser
		          .loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[] { "-maxLength", "500",
		          "-retainTmpSubcategories" });
		
		for(int i=0; i<inputLines.size(); i++) {
			System.out.println(i);
			
			String[] sent = inputLines.get(i).split(" ");
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
			//tp.printTree(parse);
			
			ListExtractorDependencyParse dependencyParse = new ListExtractorDependencyParse(inputLines.get(i), tdl);
			
			ListExtractorConjuncts conjunctStructures = new ListExtractorConjuncts();
			List<String> sentences = ListExtractorRuleBasedExtractor.getSimpleSentences(dependencyParse, conjunctStructures);
			
			addConjunctsInfoToFile(outputLines, conjunctStructures, inputLines.get(i));
			/*if(sentences.size() == 0) {
				sentences = new ArrayList<String>();
				sentences.add(inputLines.get(i));
			}
			
			outputLines.add(inputLines.get(i));
			
			outputLines.add(Integer.toString(sentences.size()));
			for(String simpleSentence : sentences) {
				outputLines.add(simpleSentence);
			}
			
			outputLines.add("\n\n");*/
		}
		
		ListExtractorIO.writeFile(outputFile, outputLines);
	}
	
	private static void addConjunctsInfoToFile(List<String> outputLines, ListExtractorConjuncts conjunctStructures, String sentence) {
		outputLines.add(sentence);
		outputLines.add(Integer.toString(conjunctStructures.numberOfConjunctStructures));
		
		for(int i=0; i<conjunctStructures.numberOfConjunctStructures; i++) {
			ListExtractorConjunctStructure conjunctStructure = conjunctStructures.conjunctStructures.get(i);
			String tempLine = Integer.toString(conjunctStructure.conjunctiveWordIndex) + " " + Integer.toString(conjunctStructure.numberOfConjuncts);
			outputLines.add(tempLine);
			
			for(int j=0; j<conjunctStructure.numberOfConjuncts; j++) {
				List<Integer> conjunctList = conjunctStructure.conjuncts.get(j);
				outputLines.add(Integer.toString(conjunctList.size()));
				String listLine = "";
				for(int k=0; k<conjunctList.size(); k++) {
					listLine += Integer.toString(conjunctList.get(k)) + " ";
				}
				outputLines.add(listLine);
			}
		}
	}
}
