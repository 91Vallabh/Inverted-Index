import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
//import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.io;



//import javax.imageio.stream.FileImageInputStream;

import java.util.Iterator;
//import javax.swing.text.html.HTMLDocument.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

//import javax.swing.text.html.HTMLDocument.Iterator;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class InvertedIndex {
	
	static HashMap<String, LinkedList<Integer>> hashMap = new HashMap<String, LinkedList<Integer>>();
	static boolean flag = false;
	
	
	public static void main(String[] args) throws IOException{
		
		InvertedIndex obj = new InvertedIndex();
		//HashMap<String, LinkedList<Integer>> hashMap = new HashMap<String, LinkedList<Integer>>();
		hashMap = inverted();
		userQuery();
		//getPostingsList(hashMap);
	}
		private static HashMap<String, LinkedList<Integer>> inverted() throws IOException{
		
		Directory directory = FSDirectory.open(Paths.get("C:\\index\\index"));
		IndexReader reader = DirectoryReader.open(directory);
		Fields fields = MultiFields.getFields(reader);
		Iterator<String> iterator = fields.iterator(); //iterator for fieldstext_en, text_hi.... 
//		HashMap<String, LinkedList<Integer>> hashmap = new HashMap<String, LinkedList<Integer>>();
		HashMap<String, LinkedList<Integer>> hashmap = new HashMap<String, LinkedList<Integer>>();
		String textfield;
		Terms terms;
		
		
		//iterating over textfields
		while(iterator.hasNext()) {
			 textfield = iterator.next();   // this loop runs for a particular field
			if(textfield.equals("id"))  
				continue;
			else {
				terms = MultiFields.getTerms(reader, textfield);
				TermsEnum termsenum = terms.iterator();
				
				while(true) {
					BytesRef termstring = termsenum.next();   //selects next term stored in bytesref format
					LinkedList<Integer> linkedlist = new LinkedList<Integer>();  // linked list for storing postings for a particular term
					
					// if term is blank then go to next term
					if(termstring == null)
						break;
					
					// postings enum refer to all postings for a term.
					PostingsEnum postenum = termsenum.postings(null, PostingsEnum.ALL);
					
					//while there are postings for a term
					while(true) {
					int doc = postenum.nextDoc();
					//check whether the doc no have exceeded the threshold value
					if(doc == DocIdSetIterator.NO_MORE_DOCS)
						break;
					else {
						linkedlist.add(doc);  //update the linked list of a term with the docid
					 }						
					}					
					//update the hashmap with term and its postings list
					hashmap.put(termstring.utf8ToString(), linkedlist);
				}//go to next term
		   }//go to next field
			//System.out.println(hashmap.size());
	}//inverted index is completed
		
		
		//printing the inverted index
//		for(Map.Entry<String, LinkedList<Integer>> entry : hashmap.entrySet()) {
//    		System.out.print(entry.getKey() + "(" + entry.getValue().size() + ") : "  );
//    		
//    		Iterator<Integer> iterator2 = entry.getValue().iterator();
//    		while(iterator2.hasNext()) {
//    			System.out.print(iterator2.next() + " -> ");
//    		}
//    		System.out.println("END");
//    	}
//		
		
		//System.out.println("");
		//print size of dictionary
		System.out.println("There are " + hashmap.size()+ " terms in the dictionary");
		
		return hashmap;
		}	
		
	public static void userQuery() throws IOException{	
		System.out.println("Please Enter the query: ");
		//BufferedReader inputread = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Vallabh\\Desktop\\IRProj2\\input.txt")));
		//new InputStreamReader(new FileInputStream("C:\\Users\\Vallabh\\Desktop\\IRProj2\\input.txt"))
		//BufferedReader inputread = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Vallabh\\Desktop\\IRProj2\\input.txt")),StandardCharsets.UTF_8);
		//BufferedReader inputread = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Vallabh\\Desktop\\IRProj2\\input.txt")));
//		Path path = Paths.get("C:\\Users\\Vallabh\\Desktop\\IRProj2\\input.txt");
//		String query;
//		try (BufferedReader inputread = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
//			query = inputread.readLine();//this will store the query from user
//		}
//		System.out.println(query);
//		
//		File file = new File("/commons/io/project.properties");
//		List lines = FileUtils.readLines(file, "UTF-8");
		BufferedReader inputread1 = new BufferedReader(new InputStreamReader(System.in));
		String query;
		query=inputread1.readLine();
		String querytempvariable = query;
		String[] arrOfStr = querytempvariable.split(" ");
		
		//to check whether each query term is valid or not		
		for(int len = 0; len < arrOfStr.length; len++) {
			if(hashMap.containsKey(arrOfStr[len])) {
				//System.out.println("Query not found!");
				continue;
			}
			else {
				flag = true;
				break;
			}
				
		}
		
		//if query is valid
		if(!flag) {
			
			for(int len = 0; len < arrOfStr.length; len++) {
				getPostingsList(arrOfStr[len],len);
			}
		
		System.out.println();
		
		System.out.println("Please select which query strategy u want to use: ");
		System.out.println("Select 1 for TaatAnd");
		System.out.println("Select 2 for TaatOr");
		System.out.println("Select 3 for DaatAnd");
		System.out.println("Select 4 for DaatOr");
		System.out.println();
		BufferedReader inputread = new BufferedReader(new InputStreamReader(System.in));
		String strategyOption = inputread.readLine();
		
		if(strategyOption.equals("1")) {
			//System.out.println("Hello");
			//System.out.println();
			TaatAnd(query);
		}
		else if(strategyOption.equalsIgnoreCase("2")) {
		TaatOr(query);
	}
//	else if(strategyOption.equalsIgnoreCase("3")) {
//		DaatAnd(query);
//	}
//	else if(strategyOption.equalsIgnoreCase("1")) {
//		DaatOr(query);
//	}		
	}
		else
			System.out.println("query is not valid!");

		
	}
	
	static void getPostingsList(String term, int len) {
		System.out.println("GetPostings");
		System.out.println("term"+len);
		System.out.println("Postings list: " + hashMap.get(term).toString().replace("[", "").replace(",", " ").replace("]", ""));
//		return hashMap.get(term);
	}
	
	
	 //TAATAnd query
	 static void TaatAnd(String query) throws IOException {
		 LinkedList<Integer> templist = new LinkedList<Integer>();
		 int comparison = 0;
		 
		 
		 String querytempvariable = query;
		 String[] arrOfStr = querytempvariable.split(" ");// array of query terms
		 int querytermsNo = arrOfStr.length;//no of query terms
		 //LinkedList[] postingslist = new LinkedList[termsNo];
		 LinkedList postingslist = new LinkedList();
		 ArrayList[] arrayvar = new ArrayList[querytermsNo];
		 
		 //create postings array for each term
		 for(int len = 0; len < querytermsNo; len++) {
			 //postingslist[len] = hashMap.get(arrOfStr[len]);
			 postingslist = hashMap.get(arrOfStr[len]);
			 Iterator<Integer> itr = postingslist.iterator();
			 arrayvar[len] = new ArrayList<Integer>();
			 
			// this add docids to array at different indexes
			 for(int i=0;i<postingslist.size() && itr.hasNext();i++) {
				 int temp=itr.next();
				 arrayvar[len].add(i, temp);
			}//end for
		 }//end for
		 
		 
		 //print each array of each query term
//		 for(int len = 0; len < querytermsNo; len++) {
//			 System.out.print("term" + len + " ");
//			 for(int i=0;i<postingslist.size();i++) {
//				 System.out.println("Size of postings list is " + postingslist.size());
//				 System.out.print(arrayvar[len].get(i)+" ");
//			 }//end for
//			 System.out.println();
//		 }//end for
		 System.out.println();
		 
		 //TaatAnd Algorithm
		ArrayList<Integer> answer = new ArrayList<Integer>();
		ArrayList<Integer> smallestArray = null;
		ArrayList<Integer> iterableArray = null;
		int length = DocIdSetIterator.NO_MORE_DOCS;//setting length to a very high value
		
		//find which array is smallest
//		for(int i=0; i<querytermsNo; i++) {
//			if(arrayvar[i].size() < length) {
//				smallestArray = arrayvar[i];
//				length = smallestArray.size();
//			}
//		}
		
		
		for(int i=0; i<querytermsNo-1; i++) {
			for(int j=i+1; j<querytermsNo; j++) {
				if(arrayvar[i].size() > arrayvar[j].size()) {
					iterableArray = arrayvar[i];
					arrayvar[i] = arrayvar[j];
					arrayvar[j] = iterableArray;
			}
			}
		}
		
		
		smallestArray = arrayvar[0];
		iterableArray = smallestArray;
		
		for(int i=0; i<querytermsNo; i++) {
			if(querytermsNo == 1)
				answer = arrayvar[i];
			else if(smallestArray.equals(arrayvar[i]))
				continue;
			else {
				Iterator p1 = iterableArray.iterator();
				Iterator p2 = arrayvar[i].iterator();
				int value1 = (int) p1.next();
				int value2 = (int) p2.next();
				
				while(!(p1.equals(null)) || !(p2.equals(null))) {
					if(value1 == value2) {
						comparison++;
						if(!(answer.contains(value1)))
							answer.add(value1);
						if(!(p1.hasNext()))
							break; //p1 = null;
						else
							value1 = (int) p1.next();
						if(!(p2.hasNext()))
							break; //p2 = null;
						else
							value2 = (int) p2.next();
					}
					else if(value1 < value2){
						comparison += 1;
						if(!(p1.hasNext()))
							break; //p1 = null;
						else
							value1 = (int) p1.next();
					}
					else {
						comparison += 1;
						if(!(p2.hasNext()))
							break;
						else
							value2 = (int) p2.next();
							
					}					
				}//end while
			}//end else
			iterableArray = answer;
			if(answer.isEmpty())
				break;
				
		}//end for
		
		
		//Result Printing
		System.out.println("TaatAnd");
		for(int i=0; i<querytermsNo; i++) {
			System.out.print(arrOfStr[i] + " ");
		}
		System.out.println();
		if(answer.isEmpty())
			System.out.println("Results: empty");
		else {
			System.out.print("Results: ");
			System.out.println(answer.toString().replace("[", "").replace(",", " ").replace("]", ""));
		}
		System.out.println("Number of documents in results: " + answer.size());
		System.out.println("Number of comparisons: " + comparison);
	}//End TaatEnd
	
	 
	 //TaatOr
	 static void TaatOr(String query) throws IOException{
		 LinkedList<Integer> answer = new LinkedList<Integer>();//this will store final result
		 LinkedList<Integer> term1Postings = new LinkedList<Integer>();
		 LinkedList<Integer> term2Postings = new LinkedList<Integer>();
		 LinkedList<String> queryterms = new LinkedList<String>();// this stores different query terms
		 StringTokenizer tokenizer = new StringTokenizer(query);
		 int comparisons = 0;
		 //String[] qt = query.split(" ")
		 
		 while(tokenizer.hasMoreTokens()) {
			 queryterms.add(tokenizer.nextToken());
		 }
		 //System.out.println(queryterms);
		 //Collections.sort(queryterms);
		 
		 Iterator queryIterator1 = queryterms.iterator();
		 Iterator queryIterator2 = queryterms.iterator();
		 
		 Iterator posting1Iterator;
		 Iterator posting2Iterator;
		 
		 int docId1=-1;
		 int docId2=-1;
		 String firstQueryTerm = (String) queryIterator1.next();
		 term1Postings = hashMap.get(firstQueryTerm);
		 queryIterator2.next();
		 if(queryIterator2.hasNext()) {
			 String secondQueryTerm = (String) queryIterator2.next();
		 	 term2Postings = hashMap.get(secondQueryTerm);
		 }
		 //loops for all terms in a query
		 
		 if(queryterms.isEmpty()) {
			 answer=null;
		 }
		 else if(queryterms.size() == 1)
			 answer = term1Postings;
		 else {
		 while(!queryIterator2.equals(null)) {
      			 posting1Iterator = term1Postings.iterator();
				 posting2Iterator = term2Postings.iterator();
				 docId1 = (int) posting1Iterator.next();
				 docId2 = (int) posting2Iterator.next();
				 while(!posting1Iterator.equals(null) && !posting2Iterator.equals(null)) {
					 
					
					if(docId1 == docId2) {
						comparisons++;
						System.out.println("ram");
						answer.add(docId1);
						if(!answer.contains(docId1))
							answer.add(docId1);
						if(posting1Iterator.hasNext())
							docId1 = (int) posting1Iterator.next();
//						
						if(posting2Iterator.hasNext())
							docId2 = (int) posting2Iterator.next();
						if(!posting1Iterator.hasNext() || !posting2Iterator.hasNext())
							break;
//						
					}
					else if(docId1 < docId2) {
						comparisons++;
						if(!answer.contains(docId1))
							answer.add(docId1);
						if(posting1Iterator.hasNext())
							docId1 = (int) posting1Iterator.next();
						else
							break;
					}
					else {
						comparisons++;
						if(!answer.contains(docId2))
							answer.add(docId2);
						if(posting2Iterator.hasNext())
							docId2 = (int) posting2Iterator.next();
						else
							break;
					}//end else
					
				 }//end while
				 
				 if(posting1Iterator.hasNext() && posting2Iterator.hasNext())
					 continue;
				 if(!posting1Iterator.hasNext()) {
					 if(!answer.contains(docId1)) 
						 answer.add(docId1);
					 while(!posting2Iterator.equals(null)) {
						 if(!answer.contains(docId2))
							 answer.add(docId2);
						 if(!posting2Iterator.hasNext())
							 break;
						 if(posting2Iterator.hasNext())
								docId2 = (int) posting2Iterator.next();
						 else
							 break;
					 }
				 }
				 else if(!posting2Iterator.hasNext()) {
					 if(!answer.contains(docId2)) 
						 answer.add(docId2);
					 while(!posting1Iterator.equals(null)) {
						 if(!answer.contains(docId1)) 
							 answer.add(docId1);
						 if(posting1Iterator.hasNext())
								docId1 = (int) posting1Iterator.next();
						 else
							 break;
					 }
				 }
				 

			term1Postings = answer;
			if(queryIterator2.hasNext())
				term2Postings = hashMap.get((String) queryIterator2.next());
			else
				break;
			
			
			
			
		 }//end while
    }//end else
		 
		 
		 //Result Printing
		 System.out.println("TaatOr");
			for(int i=0; i<queryterms.size(); i++) {
				System.out.print(queryterms.get(i).toString().replace("[", "").replace(",", " ").replace("]", "") + " ");
			}
			System.out.println();
			if(answer.isEmpty())
				System.out.println("Results: empty");
			else {
				System.out.print("Results: ");
				System.out.println(answer.toString().replace("[", "").replace(",", " ").replace("]", ""));
			}
			System.out.println("Number of documents in results: " + answer.size());
			System.out.println("Number of comparisons: " + comparisons);
		 
		 
		 
		 
	 }
	 
	
	 
}//add invertedIndex class


