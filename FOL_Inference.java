import java.io.*;
import java.util.*;
import java.util.Map.Entry;
public class FOL_Inference {
		
	public static HashMap<String, ArrayList<String>> kb_hmap = new HashMap<String, ArrayList<String>>(); //knowledge base to store given sentences
	public static int loop_breaker = 0;
	static ArrayList<String> answer_list=new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
        String list[]=readInput();
        int querysize=Integer.parseInt(list[0]);
        ArrayList<String> givenQuery=new ArrayList<String>();
        for(int i=0;i<querysize;i++)
        {
        	givenQuery.add(list[i+1].replace(" ", ""));
        }
        int kbsize=Integer.parseInt(list[querysize+1]);
        ArrayList<String> givenkb=new ArrayList<String>();
        for(int i=0;i<kbsize;i++)
        {
            givenkb.add(list[i+querysize+2]);
        }
        
        ArrayList<String> kb_withoutSpaces = new ArrayList<String>();
        for(int k=0;k<givenkb.size();k++) {
            String a = givenkb.get(k);
            String split_array[] = a.split(" ");
            String b = "";
            for (int i = 0; i < split_array.length; i++)
                b += split_array[i];
            kb_withoutSpaces.add(b);
        }
        
        ArrayList<String> kblist = new ArrayList<String>();

        int hmap_add_count = 0;
        String tobeputinkblist2="";
        for(int k=0;k<kb_withoutSpaces.size();k++) {
            String next = kb_withoutSpaces.get(k);
            HashMap<String, String> standardized_variable_hmap = new HashMap<String, String>();
            for (int i = 0; i < next.length(); i++) {
                if (next.charAt(i) == '(') {
                    i++;
                    String parameters = next.substring(i, next.indexOf(")", i));
                    i=next.indexOf(")", i);
                    String parameter_list[] = parameters.split(",");
					//standardize variables, leave constants as is
                    for(int l=0;l<parameter_list.length;l++) {
                    	String parameter = parameter_list[l];
                        if (parameter.charAt(0) >= 'a' && parameter.charAt(0) <= 'z') {
                        	if (!standardized_variable_hmap.containsKey(parameter)) {
                        		hmap_add_count++;
                                String standardizedVariable = "";
                                if (hmap_add_count > 0 && hmap_add_count <= 9) {
                                  	standardizedVariable = "p000" + hmap_add_count;
                                } else if (hmap_add_count > 9 && hmap_add_count <= 99) {
                                  	standardizedVariable = "p00" + hmap_add_count;
                                } else if (hmap_add_count > 99 && hmap_add_count <= 999) {
                                   	standardizedVariable = "p0" + hmap_add_count;
                                }else if (hmap_add_count > 999 && hmap_add_count <= 9999) {
                                   	standardizedVariable = "p" + hmap_add_count;
                                }
                                standardized_variable_hmap.put(parameter, standardizedVariable);
                            }
                        }
                    }
                }
            }
			
			
            Iterator<Map.Entry<String, String>> iterator = standardized_variable_hmap.entrySet().iterator();
            String newNext = next;
            while (iterator.hasNext()) {
                Map.Entry<String,String> variable = (Map.Entry<String,String>) iterator.next();
                String predicate="";
                for(int index=0;index<newNext.length();index++)
                {
                    if(newNext.charAt(index)=='(')
                    {
                    	predicate+=newNext.charAt(index);
                        index++;
                        String toreplace = newNext.substring(index, newNext.indexOf(")", index));
                        index = newNext.indexOf(")", index); 
                        predicate+=toreplace.replace(variable.getKey().toString(), variable.getValue().toString());
                        predicate+=')';
                    }
                    else
                    	predicate += newNext.charAt(index);
                }
                tobeputinkblist2=predicate;
                newNext=predicate;
                iterator.remove();
            }
            if(!tobeputinkblist2.equals(""))
                kblist.add(tobeputinkblist2);
            else
                kblist.add(newNext);
            tobeputinkblist2="";

        }
        generateKB(kblist);
        for(int i=0;i<querysize;i++)
        {
            loop_breaker=0;
            Stack<String> querystack=new Stack<String>();
            String querytp=givenQuery.get(i);
            querytp = negateQuery(querytp);
            querystack.push(querytp);
            
            HashMap<String, ArrayList<String>> prevent_looping_map = new HashMap<String, ArrayList<String>>();
            boolean res=recursiveSearch(querystack,loop_breaker,prevent_looping_map);

            if (res == true)
            	answer_list.add("TRUE");
            else
            	answer_list.add("FALSE");
        }
        printOutput(answer_list);
    }
	
	public static String[] readInput() throws IOException
    {
		String[] inputArray;
        FileReader fr=new FileReader("input.txt");
        BufferedReader br=new BufferedReader(fr);
        
        int lineIndex=0;
        while((br.readLine())!=null) {
        	lineIndex++;
        }
        br.close();
        
        inputArray=new String[lineIndex];
        int index=0;
        
        fr=new FileReader("input.txt");
        BufferedReader new_br=new BufferedReader(fr);
        String line;
        //populate array with given input
        while((line = new_br.readLine())!=null) {
        	inputArray[index]=line;
        	index++;
        }
        new_br.close();
        
        return inputArray;
    }
	
	public static void printOutput(ArrayList<String> outputList) throws Exception
    {
		PrintWriter writer = new PrintWriter("output.txt", "UTF-8");

        for(int i=0;i<outputList.size();i++)
        {
        	writer.print(outputList.get(i));
         	if(i!=outputList.size()-1)
        		writer.println();
        }

        writer.close();
    }
	
	 public static void generateKB(ArrayList<String> kbList){
		 for(int l=0;l<kbList.size();l++) {
			 String nextLine = kbList.get(l);
	            for (int i = 0; i < nextLine.length(); i++) {
	                if (nextLine.charAt(i) >= 'A' && nextLine.charAt(i) <= 'Z') {
	                	String predicate;
	                    if(i==0) {
	                    	predicate="";
	                    }
	                    else {
	                    	if (nextLine.charAt(i - 1) == '~') {
	                        	predicate="~";
	                        }
	                        else {
	                        	predicate="";
	                        }
	                    }
	                    boolean isConstant = false;
	                    while (nextLine.charAt(i) != '(') {
		                    if (nextLine.charAt(i) == ',' || nextLine.charAt(i)==')') {
		                    	isConstant = true;
		                        break;
		                    }
		                    predicate += nextLine.charAt(i) + "";
		                    i++;
	                   }
	                   if (!isConstant) {
	                	   if(kb_hmap.containsKey(predicate)){
	                		   ArrayList<String> temper=kb_hmap.get(predicate);
	                           temper.add(nextLine);
	                           kb_hmap.remove(predicate);
	                           kb_hmap.put(predicate,temper);
	                       }
	                       else {
	                    	   ArrayList<String> temp = new ArrayList<String>();
	                           temp.add(nextLine);
	                           kb_hmap.put(predicate, temp);
	                       }
	                  }
	              }
	            }
	        }
	}
	 
	public static String negateQuery(String givenQuery){
		if(givenQuery.charAt(0) == '~')
			return givenQuery.substring(1);
		else
	        return ("~"+givenQuery);
	}
	
	 public static boolean unify(String queryParameter[],String kbParameter[])
	    {
	        int counter=0;
	        HashMap<String,String> unifyMap=new HashMap<String,String>();
	        
	        for(int i=0;i<queryParameter.length;i++)
	        {
	            String query=queryParameter[i];
	            String kb=kbParameter[i];

	            if(query.charAt(0)>='a'&& query.charAt(0)<='z' && kb.charAt(0)>='a' && kb.charAt(0)<='z') {
	            	if(unifyMap.containsKey(query)) {
	            		if (!(unifyMap.get(query).equals(kb))   )
	            		return false;
	            		else {
	            			counter++;
	            		}
	            	}
		            else {
		           		counter++;
		           		unifyMap.put(query, kb);
		           	}
	            }
	            else if(query.charAt(0)>='a'&& query.charAt(0)<='z' && kb.charAt(0)>='A' && kb.charAt(0)<='Z') {
	            	unifyMap.put(query, kb);
	                counter++;
	            }
	            else if(query.charAt(0)>='A'&& query.charAt(0)<='Z' && kb.charAt(0)>='a' && kb.charAt(0)<='z') {
	            	unifyMap.put(kb, query);
	                counter++;
	            }
	            else if(query.equals(kb)) {
	                counter++;
	            }
	        }
	        if(counter==queryParameter.length)
	            return true;
	        else
	            return false;
	    }

	 public static boolean recursiveSearch(Stack<String> queryStack,int loop_breaker,HashMap<String, ArrayList<String>> prevent_looping_map)
	    {
	        while(!queryStack.isEmpty())
	        {
	            String firstel=queryStack.pop();
	            String querytp=negateQuery(firstel);

	            String predicate="";
	            int index=-1;
	            for(int i=0;i<querytp.length();i++)
	            {
	                while(querytp.charAt(i)!='(')
	                {
	                	predicate+=querytp.charAt(i);
	                    i++;
	                }
	                index=i;
	                break;
	            }
	            String temp = querytp.substring(index+1,querytp.indexOf(")"));
	            String queryParameters[]=temp.split(",");
	            
	            ArrayList<String> prevent_looping_list_prev = prevent_looping_map.get(negateQuery(predicate));
	            
	            if(prevent_looping_map.containsKey(negateQuery(predicate))) {
	            	boolean found=false;
	            	if(prevent_looping_list_prev!=null) {
                    	for(String s:prevent_looping_list_prev) {
                    		if(s.equals(firstel)) {
                    			found=true;
                    			break;
                    		}
                    	}
	            	}
                	if(found) {
                		return false;
                	}
	            }
	            
	            if(kb_hmap.containsKey(predicate))
	            {
	            	
	                ArrayList<String> predicate_sentences=kb_hmap.get(predicate);
		            
	                if(prevent_looping_map.containsKey(negateQuery(predicate))) {
    	            	boolean found=false;
    	            	if(prevent_looping_list_prev!=null) {
                        	for(String s:prevent_looping_list_prev) {
                        		if(s.equals(firstel)) {
                        			found=true;
                        			break;
                        		}
                        	}
    	            	}
                    	if(found) {
                    		return false;
                    	}
    	            }
		            
	                for(int i=0;i<predicate_sentences.size();i++)
	                {               	
	                    String combined_sentence=predicate_sentences.get(i);
	                    ArrayList<String> predicateList=new ArrayList<String>();
	                    String separate_predicates[]=combined_sentence.split("\\|");

	                    String match="";
	                    int count=0;
	                    for(int k=0; k<separate_predicates.length; k++) {
	                    	
	                    	String tempPredicate = separate_predicates[k];
	                    	
	                    	String predicate1="";
	                    	int index1=-1;
	        	            for(int ii=0;ii<tempPredicate.length();ii++)
	        	            {
	        	                while(tempPredicate.charAt(ii)!='(')
	        	                {
	        	                	predicate1+=tempPredicate.charAt(ii);
	        	                    ii++;
	        	                }
	        	                index1=ii;
	        	                break;
	        	            }
	        	            String tempP = tempPredicate.substring(index1+1,tempPredicate.indexOf(")"));
	        	            String kbParameters[]=tempP.split(",");
	                    	predicateList.add(tempPredicate);

	                    	if(predicate1.equals(predicate)) {
	                    		if(checkParameters(queryParameters, kbParameters)) {
	                    			match = tempPredicate;
	                    			count++;
	                    		}
	                    	}
	                    }
	                    if(count==0)
	                    	return false;
	                    String kbParametersString="";
	                    for(int j=0;j<match.length();j++)
	                    {
	                        if(match.charAt(j)=='(')
	                        {
	                            j = j+1;
	                            kbParametersString = match.substring(j, match.indexOf(")"));
	                            j =match.indexOf(")");
	                            break;
	                        }
	                    }
	                    String kbParameters[]=kbParametersString.split(",");
	                    
	                    
	                    ArrayList<String> prevent_looping_list = prevent_looping_map.get(negateQuery(predicate));
	                    if(prevent_looping_list==null) {
	                    	if(loop_breaker!=0) {
	                    	prevent_looping_list = new ArrayList<String>();
	                    	prevent_looping_list.add(firstel);
	                    	prevent_looping_map.put(negateQuery(predicate), prevent_looping_list);
	                    	}
	                    }
	                    else {
	                    	boolean found = false;
	                    	for(String inloopingList:prevent_looping_list) {
	                    		if(inloopingList.equals(firstel)) {
	                    			found=true;
	                    			break;
	                    		}
	                    	}
	                    	if(!found) {
		                    	prevent_looping_list.add(firstel);
		                    	prevent_looping_map.put(negateQuery(predicate), prevent_looping_list);
	                    	}
	                    }
	                    
	                    boolean result=unify(queryParameters,kbParameters);
	                    if(result==true)
	                    {
	                    	HashMap<String,String> unifyMap=getUnifyMap(queryParameters, kbParameters);
	                        if(isConstant(kbParameters)) {
	                        	ArrayList<String> prevent_looping_list_to_be_removed = prevent_looping_map.get(negateQuery(predicate));
	                        	if(prevent_looping_list_to_be_removed!=null) {
	                            	for(String s:prevent_looping_list_to_be_removed) {
	                            		if(s.equals(firstel)) {
	                            			prevent_looping_list_to_be_removed.remove(s);
	                            			break;
	                            		}
	                            	}
	        	            	}
	                        }
	                        
	                        String stack[]= queryStack.toArray(new String[queryStack.size()]);
	                        ArrayList<String> stackList=new ArrayList<String>();
	                        int s=0;
	                        while(s < stack.length) {
	                        	stackList.add(stack[s]);
	                        	s++;
	                        }
	                        for(int q=0;q<predicateList.size();q++)
	                        {
	                        	String tempQueryTp = querytp;
	                        	String bkpCurrentElement = predicateList.get(q);
	                            String currentkbelement=predicateList.get(q);
	                            String[] returnValue = getResolvedElement(unifyMap, currentkbelement, querytp);
	                            currentkbelement = returnValue[0];
	                            tempQueryTp = returnValue[1];
	                            
	                            String checking="";
	                            for(int f1=0;f1<currentkbelement.length();f1++){
	                                while(currentkbelement.charAt(f1)!='('){
	                                    checking+=currentkbelement.charAt(f1)+"";
	                                    f1++;
	                                }
	                                break;
	                            }
	                            
	                            
	                            if(checking.equals(predicate)) {
	                            	//if both sentences are exactly same, don't add
	                            	// if this is same predicate as the query but has different parameters, check if it can be unified.
	                            	// if yes, replace that variable with new variable for all clauses and continue
	                            	// if not, leave it be
	                            	
	                            	
	                            	if(bkpCurrentElement.equals(tempQueryTp)) {
	                            		//don't add
	                            	}
	                            	else if(currentkbelement.equals(tempQueryTp)){
	                            		//don't add
	                            	}
	                            	else {
	                            		String[] currentElementParameters = getParameters(currentkbelement);
	                            		
	                            		if(isConstant(currentElementParameters)) {
	                            			stackList.add(currentkbelement);
	                            		}
	                            		else {
	                            			//check if it can be unified
	                            			boolean result2=unify(queryParameters,currentElementParameters);
	                            			if(result2==true) {
	                            				
	                            				HashMap<String,String> unifyMap2=getUnifyMap(queryParameters, currentElementParameters);
	                            				predicateList = getUnifiedPredicateList(predicateList, unifyMap2);
	                            				stackList = getUnifiedPredicateList(stackList, unifyMap2);
	                            			}
	                            			
	                            		}
	                            	}
	                            }
	                            
	                            else if(!checking.equals(predicate)){
	                                String original=currentkbelement;
	                                String tempNegatedPredicate=negateQuery(original);
	                                boolean samePredicate=false;
	                                for (Iterator<String> iterator2 = stackList.iterator(); iterator2.hasNext();) {
	                                    String string = iterator2.next();
	                                    if (string.equals(tempNegatedPredicate)) {
	                                        iterator2.remove();
	                                        samePredicate=true;
	                                    }
	                                }
	                                if(!samePredicate)
	                                	stackList.add(original);
	                            }
	                        }
	                        
	                        ArrayList<String> stackListNew = new ArrayList<String>();   
	                       
	                        for(int q=0;q<stackList.size();q++) {
	                        	String currentkbelement=stackList.get(q);
	                        	String currentParams[] = getParameters(currentkbelement);
	                        	for(int x=0;x<currentParams.length;x++) {
	                        		String sCurrent = currentParams[x];
		                            Iterator<Entry<String, String>> iterator1 = unifyMap.entrySet().iterator();
		                            while (iterator1.hasNext()) {
		                                Map.Entry<String,String> pair = (Map.Entry<String,String>)iterator1.next();
		                                if(sCurrent.contains((String)pair.getKey())) {
		                                	sCurrent=sCurrent.replace((String)pair.getKey(),(String)pair.getValue());
		                                	currentParams[x] = sCurrent;
		                                }
		                            }
	                        	}
	                        	
	                        	String currentPredicate = getPredicate(currentkbelement);
	                        	currentPredicate += "(";
	                        	for(int x=0;x<currentParams.length;x++) {
	                        		currentPredicate = currentPredicate + currentParams[x];
	                        		if(x!=currentParams.length-1) {
	                        			currentPredicate = currentPredicate + ",";
	                        		}
	                        	}
	                        	
	                        	currentPredicate+=")";
	                        	
	                        	stackListNew.add(currentPredicate);
	                        }
	                        Stack<String> finalstack=new Stack<String>();
	                        int pushIndex=0;
	                        ArrayList<String> stackListFinal = checkResolvablePredicatesInClause(stackListNew);
	                        while(pushIndex<stackListFinal.size()) {
	                        	finalstack.push(stackListFinal.get(pushIndex));
	                        	pushIndex++;
	                        }
	                        
	                        boolean nextResult=recursiveSearch(finalstack,loop_breaker+1,prevent_looping_map);
	                        if(nextResult==true) {
	                        	 ArrayList<String> prevent_looping_list2 = prevent_looping_map.get(negateQuery(predicate));
	                        	 if(prevent_looping_list2 !=null)
		     	                    if(prevent_looping_list2.size()==1) {
		     	                    	prevent_looping_list2.remove(negateQuery(predicate));
		     	                    }
		     	                    else {
		     	                    	for(String s2:prevent_looping_list2) {
		     	                    		if(s2.equals(firstel)) {
		     	                    			prevent_looping_list2.remove(s2);
		     	                    			break;
		     	                    		}
		     	                    	}
		     	                    }
	                        	prevent_looping_map.remove(negateQuery(predicate));
	                            return true;
	                        }
	                    }
	                }
	                return false;
	            }
	            else
	                return false;
	        }
	        return true;
	    }
	 
	 
	 public static ArrayList<String> checkResolvablePredicatesInClause(ArrayList<String> stackListNew) {
		 int[] visitedStackList = new int[stackListNew.size()];
         
         String[] stackListArray = stackListNew.toArray(new String[stackListNew.size()]);
         
         
         ArrayList<String> newStackList=new ArrayList<String>();
         int[] toBeRemoved = new int[stackListArray.length];
         int toBeRemovedIndex=0;
         int g=0;
         boolean tobeAdded=true;
         while(g<stackListNew.size()) {
         	String predicate2 = getPredicate(stackListNew.get(g));
         	String negatedPredicate = getPredicate(negateQuery(stackListArray[g]));
         	for(int d=0;d<stackListArray.length;d++) {
         		if(visitedStackList[d]!=1)
         		if(getPredicate(stackListArray[d]).equals(negatedPredicate)) {
         			String[] parameters1 = getParameters(stackListArray[g]);
         			String[] parameters2 = getParameters(stackListArray[d]);
         			
         			boolean resolutionWithinClause = unify(parameters1, parameters2);
         			if(resolutionWithinClause) {
         				tobeAdded = false;
         				visitedStackList[d]=1;
         				HashMap<String,String> unifyMap2=new HashMap<String,String>();
	                        int f11 = 0;
	                        while(f11<parameters1.length) {
	                            String queryParams11=parameters1[f11];
	                            String kbParams11=parameters2[f11];
	                            if(!unifyMap2.containsKey(kbParams11))
	                            	unifyMap2.put(kbParams11,queryParams11);
	                            f11++;
	                        }
							
	                        for(int q=0;q<stackListNew.size();q++)
	                        {
	                            String currentkbelement=stackListNew.get(q);
	                            Iterator<Entry<String, String>> iterator1 = unifyMap2.entrySet().iterator();
	                            while (iterator1.hasNext()) {
	                                Map.Entry<String,String> pair = (Map.Entry<String,String>)iterator1.next();
	                                if(currentkbelement.contains((String)pair.getKey())) {
	                                    currentkbelement=currentkbelement.replace((String)pair.getKey(),(String)pair.getValue());
	                                    toBeRemoved[q] = 1;
	                                    toBeRemovedIndex++;
	                                }
	                                else {
	                                }
	                            }
	                        }
         			}
         		}
         	}
         	g++;
         }
         ArrayList<String> stackListFinal = new ArrayList<String>();
         for(int h=0;h<toBeRemoved.length;h++) {
         	if(!(toBeRemoved[h]==1)) {
         		stackListFinal.add(stackListNew.get(h));
         	}
         }
         return stackListFinal;
	 }
	 
	 public static ArrayList<String> getUnifiedPredicateList(ArrayList<String> predicateList, HashMap<String, String> unifyMap) {
		 for(int q1=0;q1<predicateList.size();q1++)
         {
             String currentkbelement=predicateList.get(q1);
             Iterator<Entry<String, String>> iterator = unifyMap.entrySet().iterator();
             while (iterator.hasNext()) {
                 Map.Entry<String,String> pair = (Map.Entry<String,String>)iterator.next();
                 if(currentkbelement.contains((String)pair.getKey())) {
                	 currentkbelement=currentkbelement.replace((String)pair.getKey(),(String)pair.getValue());
                     predicateList.set(q1, currentkbelement);
                 }
             }
         }
		 return predicateList;
	 }
	 
	 public static String[] getResolvedElement(HashMap<String,String> unifyMap, String currentKbElement, String queryTp) {
		 String tempQueryTp = queryTp;
         Iterator<Entry<String, String>> iterator1 = unifyMap.entrySet().iterator();
         while (iterator1.hasNext()) {
             Map.Entry<String,String> pair = (Map.Entry<String,String>)iterator1.next();
             if(currentKbElement.contains((String)pair.getKey())) {
            	 currentKbElement=currentKbElement.replace((String)pair.getKey(),(String)pair.getValue());
             }
             if(tempQueryTp.contains((String)pair.getKey())) {
             	tempQueryTp=tempQueryTp.replace((String)pair.getKey(),(String)pair.getValue());
             }
         }
         
         return new String[]{currentKbElement,tempQueryTp};
	 }
	 
	 public static HashMap<String, String> getUnifyMap(String[] queryParameters, String[] kbParameters){
		 HashMap<String,String> unifyMap=new HashMap<String,String>();
         int f = 0;
         while(f<queryParameters.length) {
             String queryParams=queryParameters[f];
             String kbParams=kbParameters[f];
             
             char kbChar = kbParams.charAt(0);
             char queryChar = queryParams.charAt(0);
             
             if(kbChar == queryChar) {
             	if(!unifyMap.containsKey(kbParams))
                 	unifyMap.put(kbParams,queryParams);
             }
             else if(kbChar >= 'A' && kbChar <= 'Z') {
             	if(!unifyMap.containsKey(queryParams))
             		unifyMap.put(queryParams,kbParams);
             }
             else if(queryChar>='A' && queryChar<='Z') {
             	if(!unifyMap.containsKey(kbParams))
                 	unifyMap.put(kbParams,queryParams);
             }
             f++;
         }
         return unifyMap;
	 }
	 
	 public static boolean unifiedWithConstants(HashMap<String,String> unifyMap) {
		 Iterator<Entry<String, String>> iterator1 = unifyMap.entrySet().iterator();
		 int count=0;
         while (iterator1.hasNext()) {
             Map.Entry<String,String> pair = (Map.Entry<String,String>)iterator1.next();
             String key = pair.getKey();
             String value = pair.getValue();
             
             if(key.charAt(0)>='A' && key.charAt(0)<='Z') {
            	 count++;
             }
             else if(value.charAt(0)>='A' && value.charAt(0)<='Z') {
            	 count++;
             }
             else {
            	 //do nothing
             }
         }
         
         if(unifyMap.size() == count) {
        	 return true;
         }
         else
        	 return false;
	 }
	 
	 
	 public static boolean isConstant(String[] parameterList) {
		 int count=0;
		 for(int j=0;j<parameterList.length;j++) {
			 String temp = parameterList[j];
			 
			 if(temp.charAt(0)>='A' && temp.charAt(0) <='Z') {
				 count++;
			 }
		 }
		 if(count == parameterList.length)
			 return true;
		 else
			 return false;
	 }
	 
	 public static String[] getParameters(String query) {
		 String predicate1="";
     	int index1=-1;
         for(int ii=0;ii<query.length();ii++)
         {
             while(query.charAt(ii)!='(')
             {
             	predicate1+=query.charAt(ii);
                 ii++;
             }
             index1=ii;
             break;
         }
         String tempP = query.substring(index1+1,query.indexOf(")"));
         String parameters[]=tempP.split(",");
         return parameters;
	 }
	 
	 
	 public static String getPredicate(String predicate) {
		 String predicate1="";
		 int index1=-1;
         for(int ii=0;ii<predicate.length();ii++)
         {
             while(predicate.charAt(ii)!='(')
             {
             	predicate1+=predicate.charAt(ii);
                 ii++;
             }
             index1=ii;
             break;
         }
         return predicate1;
	 }
	 
	 
	 public static boolean checkParameters(String[] queryParameters, String[] kbParameters) {
		 ArrayList<Integer> queryIndex = new ArrayList<Integer>();
		 ArrayList<Integer> kbIndex = new ArrayList<Integer>();
		 
		 for(int i=0;i<queryParameters.length;i++) {
			 String s = queryParameters[i];
			 if(s.charAt(0) >= 'A' && s.charAt(0) <='Z') {
				 queryIndex.add(i);
			 }
		 }
		 for(int i=0;i<kbParameters.length;i++) {
			 String s = kbParameters[i];
			 if(s.charAt(0) >= 'A' && s.charAt(0) <='Z') {
				 kbIndex.add(i);
			 }
		 }
		 
		 if(queryIndex.size() == 0 || kbIndex.size() == 0) {
			 return true;
		 }
		 else if(queryIndex.size()>0 && kbIndex.size()>0) {
			 boolean isEqual = false;
			 for(int i:queryIndex) {
				 if(queryParameters[i].equals(kbParameters[i])) {
					 isEqual = true;
				 }
			 }
			 if(isEqual) {
				 return true;
			 }
			 else 
				 return false;
		 }
		 else
			 return true;
		 
	 }
}
