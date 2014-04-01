/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vm;

import ixm.Parser;
import java.util.ArrayList;

/**
 * Calculate the Recall and Precision of each reference query.
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 */
public class Metrics {

    int queryNumber;
    private ArrayList<String> queryResults;
    private ArrayList<ArrayList<String>> collectionRelevantDocsList;

    public Metrics(String collection) {
        collectionRelevantDocsList = Parser.ParsRelevant("Collections\\" + collection + "\\" +
                                            collection.toLowerCase() + "_relevant.txt");
    }

    /**
     * Calculates and initializes the number of documents relevant to this query (R)
     *
     * @param queryNumber The number of the query
     */
    public void setQueryNumber(int queryNumber) {
        this.queryNumber = queryNumber;
    }

    /**
     * Calculates the number of documents that a query returned (A)</ br>
     * and initializes the number of documents that were retrieved </ br>
     * by the query and were indeed relevant (Ar).
     * @param results The list with the querie's results.
     */
    public void setResults (ArrayList<String> results) {
        queryResults = results;
    }

    /**
     * Calculates the recall of a query
     * @return Query's recall.
     */
    public double Recall(){
        return getRelativeFromResult()/getActualRelativeDocsNumber();
    }

    /**
     * Calculates the precision of a query
     * @return Query's precision
     */
    public double Prescision(){
        return getRelativeFromResult()/getResultDocNumber();
    }

    public double getResultDocNumber(){
        return queryResults.size();
    }

    public double getActualRelativeDocsNumber(){
        return collectionRelevantDocsList.get(queryNumber - 1).size();
    }

    /**
     * Calculates the number of documents that were returned by a query</ br>
     * and were indeed relevant to it (Ar).
     * @param queryResults The list with the querie's results.
     * @return The number of documents that were returned by a query and were indeed relevant to it.
     */
    public double getRelativeFromResult(){

        int numberOfMatches = 0;
        for(String docName : queryResults){
            if(collectionRelevantDocsList.get(queryNumber - 1).contains(docName)){
                numberOfMatches++;
            }
        }
        
        return numberOfMatches;
    }
}
