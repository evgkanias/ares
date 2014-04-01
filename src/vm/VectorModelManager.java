package vm;

import ixm.Index;
import ixm.IndexHandle;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is responsible for executing user's queries.</ br>
 * It implements five methods to calculate query-document relevance.
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 */
public class VectorModelManager {

    private String method;
    private IndexHandle iHandle;

    /**
     * Sets the index handle for this object
     * @param iHandle
     */
    public void setIndexHandle(IndexHandle iHandle){
        this.iHandle = iHandle;
    }

    /**
     * Excecutes user's query.
     *
     * @param query The query to be excecuted.
     * @param method  The relevance method to be used.
     * @return  An <code>ArrayList</code> containing the results of the query.
     */
    public ArrayList<String> excecuteQuery(String query, String method){

        // keep only letters and numbers
        query = query.replaceAll("[^A-Za-z0-9$]", " ");
        query = query.replaceAll(" {2,}", " ").trim();

        // if the query is empty return null value
        if (query.equals("")) return null;

        this.method = method;
        double mark = 0;
        ArrayList<DocumentRank> rank = new ArrayList();

        //for all docs in the diractory
         ArrayList<String> allDocsList = iHandle.getIndex().getDocNames();
        //we call one of the 3 functions
         
         if(method.equals("euclidean")){
             for (String doc : allDocsList){
                 mark = this.EuclideanDistance(doc, query);
                 rank.add(new DocumentRank(doc, mark));
             }
         } else if(method.equals("inner product")){
             for (String doc : allDocsList){
                 mark = this.InnerProduct(doc, query);
                 if (mark > 0.3) rank.add(new DocumentRank(doc, mark));
             }
         } else if (method.equals("cosine")) {
             for (String doc : allDocsList){
                 mark = this.Cosine(doc, query);
                 if (mark >= 0.3) rank.add(new DocumentRank(doc, mark));
             }
         } else if (method.equals("dice")) {
             for (String doc : allDocsList){
                 mark = this.Dice(doc, query);
                 if (mark > 0) rank.add(new DocumentRank(doc, mark));
             }
         } else {
             for (String doc : allDocsList){
                 mark = this.Jaccard(doc, query);
                 if (mark > 0) rank.add(new DocumentRank(doc, mark));
             }
         }

         // in case there are no results return an empty list
         if (rank.size() == 0)
             return new ArrayList();

         rank = quickSort(rank,0,rank.size()-1);
         ArrayList<String> sortedResult = new ArrayList();
         for (int i = 0; i < rank.size(); i++)
             sortedResult.add(rank.get(i).getName());

         return sortedResult;
    }

    /**
     * Returns the k most relevant to the query documents.
     * @param query The query to be excecuted.
     * @param method  The relevance method to be used.
     * @param k  The number of most relevant documents the user wants to get.
     * @return An <code>ArrayList</code> containing thek most relative results of the query.
     */
    public ArrayList<String> getTopK(String query, String method, int k){
        ArrayList<String> sortedResult = this.excecuteQuery(query, method);
        ArrayList<String> result = new ArrayList();

        for (int i = 0; i < k; i++) {
            try {
                result.add(sortedResult.get(i));
            } catch (Exception ex) {
                return result;
            }
        }

        return result;
    }

    /**
     * Calculates the query-document relevance<br/>
     * using the method of Euclidean Distance.
     * @param document The document to whitch we want to find relevance with the query.
     * @param query Users query.
     * @return The query-document relevance as double number.
     */
    public double EuclideanDistance(String document, String query) {

        double queryTermWeight = 0, docTermWeight = 0;
        double sum = 0, dist = 0;

        String[] queryWords = query.split(" ");
        for(String term : queryWords){
            queryTermWeight = this.getQueryTermWeight(term, query);
            docTermWeight = this.getWordsWeight(term, document);

            double diff = queryTermWeight - docTermWeight;
            double absDiff = Math.abs(diff);
            double powerAbsDiff = Math.pow(absDiff, 2);
            sum += powerAbsDiff;
        }

        dist = Math.sqrt(sum);
        
        return dist;
    }

    /**
     * Calculates the query-document relevance<br/>
     * using the method of Inner Product.
     * @param document The document to whitch we want to find relevance with the query.
     * @param query Users query.
     * @return The query-document relevance as double number.
     */
    public double InnerProduct(String document, String query) {

        double sum = 0;
        double queryTermWeight = 0, docTermWeight = 0;

        String[] queryWords = query.split(" ");
        for(String term : queryWords){
            queryTermWeight = this.getQueryTermWeight(term, query);
            docTermWeight = this.getWordsWeight(term, document);

            sum += queryTermWeight*docTermWeight;
        }

        return sum;
    }

    /**
     * Calculates the query-document relevance<br/>
     * by calculating the Cosine of the angle between<br/>
     * query's and document's vectors.
     * @param document The document to whitch we want to find relevance with the query.
     * @param query Users query.
     * @return The query-document relevance as double number.
     */
    public double Cosine(String document, String query) {

        double sum = 0;
        double queryMeter=0, docMeter=0;
        double querySum = 0, docSum=0;
        double queryTermWeight = 0, docTermWeight = 0;

        String[] queryWords = query.split(" ");
        
        for(String term : queryWords){
            queryTermWeight = this.getQueryTermWeight(term, query);
            docTermWeight = this.getWordsWeight(term, document);
            
            sum += queryTermWeight*docTermWeight;
            querySum += Math.pow(queryTermWeight, 2);
            docSum += Math.pow(docTermWeight, 2);
        }

        queryMeter = Math.sqrt(querySum);
        docMeter = Math.sqrt(docSum);

        double cosine;
        if (queryMeter != 0 && docMeter != 0)
            cosine = sum/(queryMeter*docMeter);
        else
            cosine = 0;

        return cosine;
    }

    /**
     * Calculates the query-document relevance<br/>
     * using the Dice method.
     * @param document The document to whitch we want to find relevance with the query.
     * @param query Users query.
     * @return The query-document relevance as double number.
     */
    public double Dice(String document, String query){

        double sum = this.InnerProduct(document, query);

        double queryTermWeight = 0, docTermWeight = 0;
        double Ld = 0;
        double Lq = 0;

        String[] queryWords = query.split(" ");

        for(String term : queryWords){
            queryTermWeight = this.getQueryTermWeight(term, query);
            docTermWeight = this.getWordsWeight(term, document);

            Ld += Math.pow(docTermWeight, 2);
            Lq += Math.pow(queryTermWeight, 2);
        }

        Lq = Math.sqrt(Lq);
        Ld = Math.sqrt(Ld);

        double diceResult;
        if (Ld != 0 && Lq != 0)
            diceResult = ( 2/ ( (Math.pow(Ld, 2))+(Math.pow(Lq, 2)) ) ) * sum;
        else
            diceResult = 0;

        return diceResult;
    }

    /**
     * Calculates the query-document relevance<br/>
     * using the Jaccard method.
     * @param document The document to whitch we want to find relevance with the query.
     * @param query Users query.
     * @return The query-document relevance as double number.
     */
    public double Jaccard(String document, String query){

        double sum = this.InnerProduct(document, query);

        double queryTermWeight = 0, docTermWeight = 0;
        double Ld = 0;
        double Lq = 0;

        String[] queryWords = query.split(" ");

        for(String term : queryWords){
            queryTermWeight = this.getQueryTermWeight(term, query);
            docTermWeight = this.getWordsWeight(term, document);

            Ld += Math.pow(docTermWeight, 2);
            Lq += Math.pow(queryTermWeight, 2);
        }

        Lq = Math.sqrt(Lq);
        Ld = Math.sqrt(Ld);

        double jacResult;
        if (Ld != 0 && Lq != 0)
            jacResult =  sum / ( (Math.pow(Ld, 2))+(Math.pow(Lq, 2)) - sum);
        else
            jacResult = 0;

        return jacResult;
    }

    /**
     * Get the weight of the word<br />
     * for a specific document
     * @param word  The word who's weight we want to calculate.
     * @param document The document for whitch we want to calculate a words weight.
     * @return The words weight.
     */
    public double getWordsWeight(String word, String document) {
        try {
            int docIndex = iHandle.getIndex().get(word).indexOf(document);
            return iHandle.getIndex().get(word).get(docIndex).GetWeight();
        } catch(Exception ex) {
            return 0;
        }
    }

    /**
     * Get the weight of a specific term of a query.
     * @param term The term of the query whos weight we want to calculate.
     * @param query Users query.
     * @return Terms weight as a double number.
     */
    public double getQueryTermWeight(String term, String query){

        HashMap<String,Double> termFreqs = new HashMap();
        Index index = iHandle.getIndex();
        double weight = 0.0, maxFreq = 0.0;

        //get terms frequency
        for(String currTerm : query.split(" ")) {
            if (termFreqs.get(currTerm) == null)
                termFreqs.put(currTerm, 0.0);
            termFreqs.put(currTerm, termFreqs.get(currTerm) + 1.0);
            maxFreq = Math.max(termFreqs.get(currTerm),maxFreq);
        }

        double totalDocNumber = index.getDocNames().size();
        if (index.get(term) != null)
            weight = ( 0.5*(termFreqs.get(term)/maxFreq)  + 0.5) * Math.log(totalDocNumber/index.get(term).size());
        else
            weight = 0;

        return weight;
    }

    /**
     * Assistant function for the QuickSort algorithm.
     * @param arr
     * @param left
     * @param right
     * @return
     */
    private int partition(ArrayList<DocumentRank> arr, int left, int right){

    int i = left, j = right;
    DocumentRank tmp;
    DocumentRank pivot = arr.get( (left + right)/2 );


    while (i <= j) {
        if (method.equals("euclidean")) {
            while (arr.get(i).getRank() < pivot.getRank()) i++;
            while (arr.get(j).getRank() > pivot.getRank()) j--;
        } else {
            while (arr.get(i).getRank() > pivot.getRank()) i++;
            while (arr.get(j).getRank() < pivot.getRank()) j--;
        }

        if (i <= j) {
            tmp = arr.get(i);
            arr.set(i, arr.get(j));
            arr.set(j, tmp);
            i++;
            j--;
        }
    }

    return i;

}

    /**
     * Implementation of the QuickSort algorithm
     * @param arr
     * @param left
     * @param right
     * @return An ArrayList containing the documents in asccending ranking order .
     */
    private ArrayList<DocumentRank> quickSort(ArrayList<DocumentRank> arr, int left, int right) {

        int index = partition(arr, left, right);
        if (left < index - 1)
            quickSort(arr, left, index - 1);
        if (index < right)
            quickSort(arr, index, right);

        return arr;
    }
    
}
