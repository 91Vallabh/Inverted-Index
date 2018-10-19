import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

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
	
	//Global varibales
	static HashMap<String, LinkedList<Integer>> hashMap = new HashMap<String, LinkedList<Integer>>();
	static boolean flag = false;
	static BufferedWriter bufferWriter;
	
	
	//--------------------------------------------------------------main function------------------------------------------------//
	public static void main(String[] args) throws IOException{
		
		String start1 = args[0];
		String fileOutput = args[1];
		String fileInput = args[2];
		
		//opening the output file
		bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOutput), "UTF-8"));
		//InvertedIndex obj = new InvertedIndex();
		hashMap = inverted(start1);// creating inverted index
		userQuery(fileInput);//passing user query
		bufferWriter.close(); // closing the ouput file
	}
	
	
	
	
	
	
	//--------------------------------------------------------------creating inverted index------------------------------------------------//
	
	private static HashMap<String, LinkedList<Integer>> inverted(String start1) throws IOException{
    
		//changes
		Path path = Paths.get(start1);
		Directory directory = FSDirectory.open(path);
		IndexReader reader = DirectoryReader.open(directory);
		Fields fields = MultiFields.getFields(reader);
		Iterator<String> iterator = fields.iterator(); //iterator for fieldstext_en, text_hi.... 
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
			
	     }//end while
		
		
		/*
		//--------------------------------------------------------------printing the inverted index------------------------------------------------//
		for(Map.Entry<String, LinkedList<Integer>> entry : hashmap.entrySet()) {
    		System.out.print(entry.getKey() + "(" + entry.getValue().size() + ") : "  );
    		
    		Iterator<Integer> iterator2 = entry.getValue().iterator();
    		while(iterator2.hasNext()) {
    			System.out.print(iterator2.next() + " -> ");
    		}
    		System.out.println("END");
    	}
		
		//		print size of dictionary
		System.out.println("There are " + hashmap.size()+ " terms in the dictionary");
		
		*/
		
	    return hashmap;
    }//end   inverted index function 
		
	
		
	//--------------------------------------------------------------userQuery(String fileInput)------------------------------------------------//	
	
		
	public static void userQuery(String fileInput) throws IOException{
		
		BufferedReader inputread1 = new BufferedReader(new InputStreamReader(new FileInputStream(fileInput),StandardCharsets.UTF_8));
		StringBuilder stringbuilder = new StringBuilder();
		String line =null;
		
		//processing query from user and printing postings list for each term
		try {
			while((line=inputread1.readLine()) != null) {
				stringbuilder.append(line);
				String query = stringbuilder.toString();
				String querytempvariable = query;
				String[] arrOfStr = querytempvariable.split(" ");
				
				//calling getpostingsList
				for(int len = 0; len < arrOfStr.length; len++) {
						getPostingsList(arrOfStr[len],len);
				}
			
				//searching strategies
				TaatAnd(query);
				TaatOr(query);	
			
				stringbuilder.delete(0, line.length());
			}//end while
		}finally {
					inputread1.close();
		}
	}//end userQuery
	
	
	
	//--------------------------------------------------------------get Postings List------------------------------------------------//
	
	static void getPostingsList(String term, int len) throws IOException {
		bufferWriter.write("GetPostings\n");
		bufferWriter.write(term + " " +"\n");
		bufferWriter.write("Postings list: " + hashMap.get(term).toString().replaceAll("\\[|\\]|,","") + "\n");
	}
	
		
	//--------------------------------------------------------------TaatAnd  Query------------------------------------------------//
	
	 static void TaatAnd(String query) throws IOException {
		 
		 int comparison = 0;
		 String querytempvariable = query;
		 String[] arrOfStr = querytempvariable.split(" ");// array of query terms
		 int querytermsNo = arrOfStr.length;//no of query terms
		 LinkedList<Integer> postingslist = new LinkedList<Integer>();
		 ArrayList[] arrayvar = new ArrayList[querytermsNo];
		 
		 //create postings array for each term
		 for(int len = 0; len < querytermsNo; len++) {
			 postingslist = hashMap.get(arrOfStr[len]);
			 Iterator<Integer> itr = postingslist.iterator();
			 arrayvar[len] = new ArrayList<Integer>();
			 
			// this add docids to array at different indexes
			 for(int i=0;i<postingslist.size() && itr.hasNext();i++) {
				 int temp=itr.next();
				 arrayvar[len].add(i, temp);
			}//end for
		 }//end for
		 
		  
		 //TaatAnd Algorithm
		 ArrayList<Integer> answer = new ArrayList<Integer>();
		 ArrayList<Integer> smallestArray = null;
		 ArrayList<Integer> iterableArray = null;
		 int length = DocIdSetIterator.NO_MORE_DOCS;//setting length to a very high value
		
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
				  Iterator<Integer> p1 = iterableArray.iterator();
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
		  bufferWriter.write("TaatAnd\n");
		  for(int i=0; i<querytermsNo; i++) {
			  bufferWriter.write(arrOfStr[i] + " ");
		  }
		  bufferWriter.write("\n");
		  if(answer.isEmpty())
			  bufferWriter.write("Results: empty\n");
		  else {
			  bufferWriter.write("Results: ");
			  bufferWriter.write(answer.toString().replace("[", "").replace(",", " ").replace("]", "") + "\n");
		  }
		  bufferWriter.write("Number of documents in results: " + answer.size() + "\n");
		  bufferWriter.write("Number of comparisons: " + comparison + "\n");
	  }//End TaatEnd
	
	 
	//--------------------------------------------------------------TaatOr  Query------------------------------------------------//
	 
	 
	 static void TaatOr(String query) throws IOException{
		 LinkedList<Integer> answer = new LinkedList<Integer>();//this will store final result
		 LinkedList<Integer> term1Postings = new LinkedList<Integer>();
		 LinkedList<Integer> term2Postings = new LinkedList<Integer>();
		 LinkedList<String> queryterms = new LinkedList<String>();// this stores different query terms
		 StringTokenizer tokenizer = new StringTokenizer(query);
		 int comparisons = 0;
		 		 
		 while(tokenizer.hasMoreTokens()) {
			 queryterms.add(tokenizer.nextToken());
		 }
		 
		 Iterator<String> queryIterator1 = queryterms.iterator();
		 Iterator<String> queryIterator2 = queryterms.iterator();
		 Iterator<Integer> posting1Iterator;
		 Iterator<Integer> posting2Iterator;
		 
		 int docId1=-1;
		 int docId2=-1;
		 
		 String firstQueryTerm = queryIterator1.next();
		 term1Postings = hashMap.get(firstQueryTerm);
		 queryIterator2.next();
		 
		 if(queryIterator2.hasNext()) {
			 String secondQueryTerm = queryIterator2.next();
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
						answer.add(docId1);
						if(!answer.contains(docId1))
							answer.add(docId1);
						if(posting1Iterator.hasNext())
							docId1 = (int) posting1Iterator.next();
						if(posting2Iterator.hasNext())
							docId2 = (int) posting2Iterator.next();
						if(!posting1Iterator.hasNext() || !posting2Iterator.hasNext())
							break;	
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
				term2Postings = hashMap.get(queryIterator2.next());
			else
				break;
		 }//end while
      }//end else
		 
		 
		 //Result Printing
		 bufferWriter.write("TaatOr\n");
			for(int i=0; i<queryterms.size(); i++) {
				bufferWriter.write(queryterms.get(i).toString().replace("[", "").replace(",", " ").replace("]", "") + " ");
			}
			bufferWriter.write("\n");
			if(answer.isEmpty())
				bufferWriter.write("Results: empty\n");
			else {
				bufferWriter.write("Results: ");
				bufferWriter.write(answer.toString().replace("[", "").replace(",", " ").replace("]", "") + "\n");
			}
			bufferWriter.write("Number of documents in results: " + answer.size() + "\n");
			bufferWriter.write("Number of comparisons: " + comparisons + "\n");	 
	   } 
    }//add invertedIndex class


