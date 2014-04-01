package ixm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>
 * This class contains information about a collection's catalogue. It is a kind<br />
 * of <code>HashMap</code> that has words as <code>keys</code> and <code>DocumentList</code><br />
 * as values.
 * </p>
 *
 * @see IndexHandle
 * @see DocumentList
 * @see DocumentInfo
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 */
public class Index extends HashMap<String,DocumentList> implements Serializable {
    private String name;                    // the index's name
    private ArrayList<String> docs;         // a list with the contained documents
    private ArrayList<Integer> docsMaxFreq; // a list with the max frequency for every document

    /**
     * <p>Initializes the private fields</p>
     *
     * @param name the index's name
     */
    public Index(String name) {
        super();
        this.name = name;
        docs = new ArrayList();
        docsMaxFreq = new ArrayList();
    }

    /**
     * <p>
     * It puts a pair of word and document. If the word appears for the first time<br />
     * in this index, a new entry is created for the new word. In another case,<br />
     * it adds the document to the word's list.
     * </p>
     *
     * @param word the word that has been found in the document
     * @param docName the document's name
     * @return a value that shows if the document has been added successfully
     */
    public boolean put(String word, String docName) {

        if (this.get(word) == null) {
            super.put(word, new DocumentList());
        }
        this.get(word).add(docName);
        if (!docs.contains(docName))
            docs.add(docName);
        int dIndex;
        if ((dIndex = this.get(word).indexOf(docName)) >= 0) {
            DocumentInfo dInfo = this.get(word).get(dIndex);
            int maxFreq;
            try {
                maxFreq = Math.max(dInfo.GetOccurences(), docsMaxFreq.get(docs.indexOf(docName)));
            } catch (Exception ex ) {
                docsMaxFreq.add(0);
                maxFreq = dInfo.GetOccurences();
            }
            docsMaxFreq.set(docs.indexOf(docName),maxFreq);
        }

        return true;
    }

    /**
     * <p>It puts many documents to a word using the simple <code>put</code> method.</p>
     *
     * @param word the word that has been found in the documents
     * @param docNames an array of the documents' names
     * @return a value that shows if the documents have been added successfully
     */
    public boolean putAll(String word, String[] docNames) {
        for (String docName : docNames) {
            this.put(word, docName);
        }
        return true;
    }

    /**
     *
     * <p>It puts a document to many words using the simple <code>put</code> method.</p>
     *
     * @param words an array of the words that have been found in the document
     * @param docName the document's name
     * @return a value that shows if the document has been added successfully to the words
     */
    public boolean putAll(String[] words, String docName) {
        for (String word : words) {
            this.put(word, docName);
        }

        return true;
    }

    /**
     * <p>
     * Removes a word from the <code>Index</code> and updates the documents' list,<br />
     * the documents' max frequence and the weights.
     * </p>
     *
     * @param word the word that is going to be removed
     * @return the removed word's documents' list
     */
    public DocumentList remove(String word) {
        for (DocumentInfo dInfo : this.get(word)) {
            int maxFreq = 0;
            int dIndex = docs.indexOf(dInfo.GetDocumentName());

            if (dInfo.GetOccurences() == docsMaxFreq.get(dIndex)) {
                String[] words = new String[this.size()];
                words = this.keySet().toArray(words);
                boolean done = false;
                for (String w : words) {
                    int docIndex = this.get(w).indexOf(dInfo.GetDocumentName());
                    if (!w.equals(word) && docIndex > -1) {
                        maxFreq = Math.max(maxFreq, this.get(w).get(docIndex).GetOccurences());
                        done = true;
                    }
                }
                if (done)
                    docsMaxFreq.set(dIndex,maxFreq);
                else {
                    docs.remove(dIndex);
                    docsMaxFreq.remove(dIndex);
                }
            }
        }

        DocumentList list = super.remove(word);

        return list;
    }

    /**
     * <p>
     * Removes a document from all the words. If after this the word hasn't got any<br />
     * documents, the word will also be removed.
     * </p>
     *
     * @param docName the document's name
     * @return a value that shows if the document has been removed from all the words
     */
    public long removeDoc(String docName) {
        String[] words = new String[this.size()];
        words = this.keySet().toArray(words);
        long counter = 0;
        try {
            if (this.docsMaxFreq.remove(this.docs.indexOf(docName)) == null)
                return -1;
            if (!this.docs.remove(docName))
                return -1;
        } catch (Exception ex) {
            return -1;
        }
        for (String word : words) {
            if (this.get(word).remove(docName)) counter++;
            if (this.get(word).size() == 0) this.remove(word);
        }

        return counter;
    }

    /**
     * <p>
     * Removes the words that are very frequent. A word is very frequent when over<br />
     * 80% of all the documents contain this word.
     * </p>
     */
    public void removeFrequentWords() {
        double threshold = 0.80;
        String[] words = this.keySet().toArray(new String[this.size()]);

        for (String word : words) {
            double wordPercentage = (double) this.get(word).size()/ (double) this.docs.size();
            if (wordPercentage > threshold)
                this.remove(word);
        }

        this.updateWeights();
    }

    /**
     * <p>Updates the weight of every document's word.</p>
     */
    public void updateWeights() {
        String[] words = this.keySet().toArray(new String[this.size()]);
        for (String word : words) {
            for (DocumentInfo doc : this.get(word)) {
                this.computeWeight(word,doc.GetDocumentName());
            }
        }
    }

    /**
     * <p>Computes the weight of a document's word.</p>
     *
     * @param word the word of the document
     * @param document the document
     */
    public void computeWeight(String word, String document) {
        double maxf = (double) this.docsMaxFreq.get(this.docs.indexOf(document));
        double wordf = 0.0;
        double totalDocNumber = 0.0;
        double idf = 0.0;
        int documentIndex = -1;

        //FIND WORDS FREQUENCY IN THE DOCUMENT
        if ((documentIndex = this.get(word).indexOf(document)) >= 0) {
            wordf = (double) this.get(word).get(documentIndex).GetOccurences();
        } else {
            return;
        }

        double nf = wordf/maxf;

        totalDocNumber = (double) docs.size();
        //numberOfDocsContainWord = wdList.size();
        if (this.get(word) != null){
            double wordsIndexSize = (double) this.get(word).size();
            idf = Math.log(totalDocNumber / wordsIndexSize);
        } else {
            idf = 0.0;
        }

        double nidf = idf/Math.log(totalDocNumber);

        double finalWeight = nf*nidf;
        this.get(word).get(documentIndex).SetWeight(finalWeight);
    }

    /**
     *
     * @return an <code>ArrayList</code> of the implemented documents in the <code>Index</code>
     */
    public ArrayList<String> getDocNames() {
        return docs;
    }

    /**
     *
     * @return the <code>Index</code>'s name
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * This is the format of the returned string:<br />
     * <blockquote><i>&lt;document's list #1&gt;</i><br /><i>&lt;document's list #2&gt;</i><br />...</blockquote>
     * </p>
     *
     * @return The index as a string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        String[] words = new String[this.size()];
        words = this.keySet().toArray(words);

        for (String word : words) {
            sb.append("\"");
            sb.append(word);
            sb.append("\" => ");
            sb.append(this.get(word).toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}
