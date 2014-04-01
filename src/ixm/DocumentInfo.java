package ixm;

import java.io.Serializable;

/**
 * <p>
 * This class has information about a document where a word has been found<br />
 * in a <code>DocumentList</code> of an <code>Index</code>. It has informantion for the document's<br />
 * name and how many times the word has been found in this document.
 * </p>
 *
 * @see DocumentList
 * @see Index
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 *
 * @version 1.0
 */
public class DocumentInfo implements Serializable {
    private String document;    // the document's name
    private int occurrences;   // the number of occurrences of the word in the document
    private double weight;      // the words weight for this document

    /**
     * <p>It initializes the number of occurences to one.</p>
     *
     * @param document the document's name
     */
    public DocumentInfo(String document) {
        this.document = document;
        this.occurrences = 1;
        this.weight = -1;
    }

    /**
     *
     * @return the document's name
     */
    public String GetDocumentName() {
        return document;
    }

    /**
     *
     * @return the number of occurrences of the word in the file
     */
    public int GetOccurences() {
        return occurrences;
    }

    /**
     * Sets the weight of the document's word
     * @param weight
     */
    public void SetWeight(double weight) {
        this.weight = weight;
    }

    /**
     *
     * @return the weight of the document's word
     */
    public double GetWeight() {
        return weight;
    }

    /**
     * <p>It adds an occurrence to this document.</p>
     *
     * @return the new number of occurences
     */
    public int AddOccurrence() {
        return ++occurrences;
    }

    /**
     * <p>
     * This is the format of the returned string:<br />
     * <blockquote>(<i>&lt;document's name&gt;</i>, <i>&lt;number of occurrences&gt;</i>)</blockquote>
     * </p>
     *
     * @return the document information as a string
     */
    @Override
    public String toString() {
        return "(" + document + ", " + occurrences + ")";
    }
}
