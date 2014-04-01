package ixm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is a list of <code>DocumentInfo</code> objects. It also has information<br />
 * about the number of occurrences that the word has from all the documents.
 *
 * @see Index
 * @see DocumentInfo
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 *
 * @version 1.0
 */
public class DocumentList extends ArrayList<DocumentInfo> implements Serializable {
    private long occurrences = 0;   // number of occurreces from all the documents

    /**
     * <p>
     * Adds a document to the list. If the document already exists in the list,<br />
     * adds an occurrence to the specific document.
     * </p>
     *
     * @param docName the document's name
     * @return a value that shows if the document has been added successfully
     */
    public boolean add(String docName) {
        for (DocumentInfo docInfo : this) {
            if (docInfo.GetDocumentName().equals(docName)) {
                docInfo.AddOccurrence();
                occurrences++;
                return true;
            }
        }

        this.add(new DocumentInfo(docName));
        occurrences++;
        return true;
    }

    /**
     * <p>Removes a document from the list.</p>
     *
     * @param docName the document's name
     * @return a value that shows if the document has been removed successfully
     */
    public boolean remove(String docName) {

        for (DocumentInfo docInfo : this) {
            if (docInfo.GetDocumentName().equals(docName)) {
                if (this.remove(docInfo)) {
                    occurrences--;
                    return true;
                } else return false;
            }
        }

        return false;
    }

    /**
     *
     * @param docName the document's name
     * @return the document's index in the <code>DocumentList</code>
     */
    public int indexOf(String docName) {
        for (DocumentInfo info : this)
            if (info.GetDocumentName().equals(docName))
                return this.indexOf(info);

        return -1;
    }

    /**
     *
     * @return the number of occurrences of the specific word in all the documents
     */
    public long getOccurrences() {
        return occurrences;
    }

    /**
     * <p>
     * This is the format of the returned string:<br />
     * <blockquote>[<i>&lt;number of occurrences&gt;</i> : <i>&lt;document info #1&gt;</i>, <i>&lt;document info #2&gt;</i>, ...]</blockquote>
     * </p>
     *
     * @return The list as a string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        sb.append(this.occurrences);
        for (int i = 0; i < this.size(); i++) {
            if (i > 0) sb.append(", ");
            else sb.append(" : ");
            sb.append(this.get(i).toString());
        }

        sb.append("]");
        return sb.toString();
    }
    
}
