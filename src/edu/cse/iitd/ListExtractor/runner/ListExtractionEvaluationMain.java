/**
 * 
 */
package edu.cse.iitd.ListExtractor.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cse.iitd.ListExtractor.comparators.ListExtractorComparatorIntegerList;
import edu.cse.iitd.ListExtractor.domain.ListExtractorConjunctStructure;
import edu.cse.iitd.ListExtractor.domain.ListExtractorConjuncts;
import edu.cse.iitd.ListExtractor.helper.ListExtractorIO;

/**
 * @author swarna
 *
 */
public class ListExtractionEvaluationMain {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String goldsetFile = args[0];
		String testFile = args[1];
		
		List<ListExtractorConjuncts> allConjunctsGold = storeIntoConjunctStructures(goldsetFile);
		List<ListExtractorConjuncts> allConjunctsTest = storeIntoConjunctStructures(testFile);
		
		calculatePrecisionRecall(allConjunctsGold, allConjunctsTest);
	}
	
	private static List<ListExtractorConjuncts> storeIntoConjunctStructures(String file) throws IOException {
		List<ListExtractorConjuncts> allConjuncts = new ArrayList<ListExtractorConjuncts>();
		
		List<String> lines = ListExtractorIO.readFile(file);
		
		for(int i=0; i<lines.size(); i++) {
			String line = lines.get(i);
			if(line.charAt(0) < '0' || line.charAt(0) > '9') {
				ListExtractorConjuncts listExtractorConjuncts = new ListExtractorConjuncts(line);
				i++;
				int numberOfConjunctStructures = Integer.parseInt(lines.get(i));
				listExtractorConjuncts.setNumberOfConjuncts(numberOfConjunctStructures);
				
				for(int j=0; j<numberOfConjunctStructures; j++) {
					i++;
					line = lines.get(i);
					String[] parts = line.split(" ");
					int conjunctiveWordIndex = Integer.parseInt(parts[0]);
					ListExtractorConjunctStructure conjunctStructure = new ListExtractorConjunctStructure(conjunctiveWordIndex);
					int numberOfConjuncts = Integer.parseInt(parts[1]);
					conjunctStructure.setNumberOfConjuncts(numberOfConjuncts);
					for(int k=0; k<numberOfConjuncts; k++) {
						i++;
						line = lines.get(i);
						int conjunctSize = Integer.parseInt(line);
						i++;
						line = lines.get(i);
						parts = line.split(" ");
						List<Integer> conjunct = new ArrayList<Integer>();
						for(int l=0; l<parts.length; l++) {
							conjunct.add(Integer.parseInt(parts[l]));
						}
						assert conjunctSize != conjunct.size();
						conjunctStructure.conjuncts.add(conjunct);
					}
					listExtractorConjuncts.addConjunctStructure(conjunctStructure);
				}
				
				allConjuncts.add(listExtractorConjuncts);
			}
		}
		
		return allConjuncts;
	}
	
	private static void calculatePrecisionRecall(List<ListExtractorConjuncts> allConjunctsGold, List<ListExtractorConjuncts> allConjunctsTest) {
		assert allConjunctsGold.size() != allConjunctsTest.size();
		int numberOfSentences = allConjunctsGold.size();
		
		double precision = 0.0, recall = 0.0;
		
		for(int i=0; i<numberOfSentences; i++) {
			ListExtractorConjuncts conjunctsGold = allConjunctsGold.get(i);
			ListExtractorConjuncts conjunctsTest = allConjunctsTest.get(i);
			
			Map<Integer, List<List<Integer>>> conjunctsMapGold = new HashMap<Integer, List<List<Integer>>>();
			Map<Integer, List<List<Integer>>> conjunctsMapTest = new HashMap<Integer, List<List<Integer>>>();
			
			for(int j=0; j<conjunctsGold.numberOfConjunctStructures; j++) {
				conjunctsMapGold.put(conjunctsGold.conjunctStructures.get(j).conjunctiveWordIndex, conjunctsGold.conjunctStructures.get(j).conjuncts);
			}
			
			for(int j=0; j<conjunctsTest.numberOfConjunctStructures; j++) {
				conjunctsMapTest.put(conjunctsTest.conjunctStructures.get(j).conjunctiveWordIndex, conjunctsTest.conjunctStructures.get(j).conjuncts);
			}
			
			double precisionEachSentence = calculatePrecisionForEachSentence(conjunctsMapGold, conjunctsMapTest);
			precision += precisionEachSentence;
			//System.out.println(precisionEachSentence);
			
			double recallEachSentence = calculateRecallForEachSentence(conjunctsMapGold, conjunctsMapTest);
			recall += recallEachSentence;
			//System.out.println(recallEachSentence);
		}
		
		System.out.println("Precision is " + Double.toString(precision/(double)numberOfSentences));
		System.out.println("Recall is " + Double.toString(recall/(double)numberOfSentences));
	}
	
	private static double calculatePrecisionForEachSentence(Map<Integer, List<List<Integer>>> conjunctsMapGold, Map<Integer, List<List<Integer>>> conjunctsMapTest) {
		double precision = 0.0;
		for(Integer conjunctiveWordIndex : conjunctsMapTest.keySet()) {
			List<List<Integer>> conjunctsTest = conjunctsMapTest.get(conjunctiveWordIndex);
			if(!conjunctsMapGold.containsKey(conjunctiveWordIndex)) {
				continue;
			}
			List<List<Integer>> conjunctsGold = conjunctsMapGold.get(conjunctiveWordIndex);
			
			Collections.sort(conjunctsGold, new ListExtractorComparatorIntegerList());
			Collections.sort(conjunctsTest, new ListExtractorComparatorIntegerList());
			
			double precisionEachStructure = 0.0;
			Map<Integer, Boolean> conjunctsConsidered = new HashMap<Integer, Boolean>();
			for(List<Integer> conjunctTest : conjunctsTest) {
				int maxIntersectionSize = 0, maxMatchingIndex = 0;
				for(int i=0; i<conjunctsGold.size(); i++) {
					if(conjunctsConsidered.containsKey(i)) continue;
					int currentIntersectionSize = getIntersectionSize(conjunctTest, conjunctsGold.get(i));
					if(maxIntersectionSize < currentIntersectionSize) {
						maxIntersectionSize = currentIntersectionSize;
						maxMatchingIndex = i;
					}
				}
				conjunctsConsidered.put(maxMatchingIndex, true);
				precisionEachStructure += (1.0/(double)conjunctsTest.size())*(((double)maxIntersectionSize)/(double)conjunctTest.size());
			}
			
			precision += (1.0/(double)conjunctsMapTest.size())*precisionEachStructure;
		}
		
		return precision;
	}
	
	private static double calculateRecallForEachSentence(Map<Integer, List<List<Integer>>> conjunctsMapGold, Map<Integer, List<List<Integer>>> conjunctsMapTest) {
		double recall = 0.0;
		for(Integer conjunctiveWordIndex : conjunctsMapGold.keySet()) {
			List<List<Integer>> conjunctsGold = conjunctsMapGold.get(conjunctiveWordIndex);
			if(!conjunctsMapTest.containsKey(conjunctiveWordIndex)) {
				continue;
			}
			List<List<Integer>> conjunctsTest = conjunctsMapTest.get(conjunctiveWordIndex);
			
			Collections.sort(conjunctsGold, new ListExtractorComparatorIntegerList());
			Collections.sort(conjunctsTest, new ListExtractorComparatorIntegerList());
			
			double recalleachStrcuture = 0.0;
			Map<Integer, Boolean> conjunctsConsidered = new HashMap<Integer, Boolean>();
			for(List<Integer> conjunctGold : conjunctsGold) {
				int maxIntersectionSize = 0, maxMatchingIndex = 0;
				for(int i=0; i<conjunctsTest.size(); i++) {
					if(conjunctsConsidered.containsKey(i)) continue;
					int currentIntersectionSize = getIntersectionSize(conjunctGold, conjunctsTest.get(i));
					if(maxIntersectionSize < currentIntersectionSize) {
						maxIntersectionSize = currentIntersectionSize;
						maxMatchingIndex = i;
					}
				}
				conjunctsConsidered.put(maxMatchingIndex, true);
				recalleachStrcuture += (1.0/(double)conjunctsGold.size())*(((double)maxIntersectionSize)/(double)conjunctGold.size());
			}
			recall += (1.0/(double)conjunctsMapGold.size())*recalleachStrcuture;
		}
		
		return recall;
	}
	
	private static int getIntersectionSize(List<Integer> list1, List<Integer> list2) {
		int size = 0;
		
        for (Integer t : list1) {
            if(list2.contains(t)) {
                size++;
            }
        }

        return size;
	}

}
