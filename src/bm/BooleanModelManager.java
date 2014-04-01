package bm;

import ixm.DocumentInfo;
import ixm.DocumentList;
import ixm.IndexHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * This class is responsible for executing</ br>
 * user's queries.
 *
 * @author Evripidis Gkanias
 */
public class BooleanModelManager {

    private IndexHandle iHandle;


    public void setIndexHandle(IndexHandle iHandle) {

        this.iHandle = iHandle;
    }

    /**
     *
     * @param query User's query (must be a logical expresion).
     * @return A list of the query's results.
     */
    public ArrayList<String> excecuteQuery(String query) {
        query = query.trim();
        query = InfixToPostfix(query);
        String[] keywords = query.split(" ");
        Stack stack = new Stack();

        for (int i = 0; i < keywords.length; i++) {
            
            if (keywords[i].equals("NOT")) {
                ArrayList<String> temp = (ArrayList<String>) stack.pop();
                stack.push(Not(temp));

                continue;
            }
            if (keywords[i].equals("AND")) {
                ArrayList<String> temp1 = (ArrayList<String>) stack.pop();
                ArrayList<String> temp2 = (ArrayList<String>) stack.pop();
                stack.push(And(temp1,temp2));
                
                continue;
            }
            if (keywords[i].equals("OR")) {
                ArrayList<String> temp1 = (ArrayList<String>) stack.pop();
                ArrayList<String> temp2 = (ArrayList<String>) stack.pop();
                stack.push(Or(temp1,temp2));
                
                continue;
            }

            ArrayList<String> temp = new ArrayList();
            DocumentList dList;
            if ((dList = iHandle.getIndex().get(keywords[i])) == null) return null;
            DocumentInfo[] dArray = dList.toArray(new DocumentInfo[dList.size()]);
            for (DocumentInfo doc : dArray)
                temp.add(doc.GetDocumentName());
            stack.push(temp);
        }

        return (ArrayList<String>) stack.pop();
    }

    /**
     * Hanldes the "AND" operator
     * @param term1
     * @param term2
     * @return A list of the documents that contain both terms.
     */
    private ArrayList<String> And(ArrayList<String> term1, ArrayList<String> term2) {

        ArrayList<String> result = new ArrayList();
        for (String doc : term1) {
            if (term2.contains(doc))
                result.add(doc);
        }

        return result;
    }

    /**
     * Hanldes the "OR" operator.
     * @param term1
     * @param term2
     * @return A list of the documents that contain either of the two terms.
     */
    private ArrayList<String> Or(ArrayList<String> term1, ArrayList<String> term2) {
        ArrayList<String> result = term1;

        for (String doc : term2) {
            if (!term1.contains(doc))
                result.add(doc);
        }

        return result;
    }

    /**
     * Handles the "NOT" operator.
     * @param term
     * @return A list of the documents that do not contain this term.
     */
    private ArrayList<String> Not(ArrayList<String> term) {
        ArrayList<String> results = iHandle.getIndex().getDocNames();

        for (String docName : term) {
            results.remove(docName);
        }

        return results;
    }

    /**
     * Turns the logical expression from <i>infix</i> to <i>postfix</i> form
     * @param infix User's query in infix form.
     * @return The <i>postfix</i> form of the query.
     */
    private String InfixToPostfix(String infix) {
        String postfix = "";
        Stack stack = new Stack();

        HashMap<String, Integer> priority = new HashMap();
        HashMap<String, Integer> operators = new HashMap();
        String[] opNames = {"(", ")", "NOT", "AND", "OR"};
        for (int i = 0; i < opNames.length; i++)
            priority.put(opNames[i], opNames.length - i);

        String term;

        while (true) {
            operators = new HashMap();

            for (String op : opNames) {
                int pos = infix.indexOf(op);
                if (pos >= 0) operators.put(op, pos);
            }
            opNames = operators.keySet().toArray(new String[operators.size()]);

            if (opNames.length == 0) break;

            String tempOp = "";
            for (String op : opNames) {
                if (tempOp.equals("")) {
                    tempOp = op;
                    continue;
                }
                if (operators.get(op) < operators.get(tempOp)) {
                    tempOp = op;
                }
            }

            term = infix.substring(0, operators.get(tempOp));
            postfix += term.trim() + " ";
            infix = infix.substring(term.length());

            try {
                while(true) {
                    if (tempOp.equals(")")) {
                        while(!((String) stack.peek()).equals("(")) {
                            String temp = (String) stack.pop();
                            postfix += temp + " ";
                        }
                        stack.pop();
                        infix = infix.substring(tempOp.length()).trim();

                        break;
                    }

                    if (priority.get((String) stack.peek()) < priority.get(tempOp) ||
                            ((String) stack.peek()).equals("(")) {
                        stack.push(tempOp);
                        infix = infix.substring(tempOp.length()).trim();
                        
                        break;
                    } else {
                        String temp = (String) stack.pop();
                        postfix += temp + " ";
                    }
                }
            } catch (Exception ex) {
                stack.push(tempOp);
                infix = infix.substring(tempOp.length()).trim();
            }
        }

        postfix += infix + " ";
        while(true) {
            try {
                String temp = (String) stack.pop();
                postfix += temp + " ";
                infix = infix.substring(temp.length());
            } catch (Exception ex) {
                break;
            }
        }

        return postfix.trim().replaceAll(" {2,}", " ");
    }
}
