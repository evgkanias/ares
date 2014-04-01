package ixm;

import java.io.*;

/**
 * <p>
 * This class handles an index. It can insert, delete and show an index.
 * </p>
 *
 * @see IndexManager
 * @see Index
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 *
 * @version 1.0
 */
public class IndexHandle implements Serializable {
    private boolean open = false;   // shows if the index handler is opened
    private boolean changed;        // shows if there are changes on the index
    private Index index;            // the index structure
    private String lastWord;        // the last word of the previous line

    /**
     * <p>Opens the index handle and shows that it's not changed.</p>
     *
     * @param index the index structure
     */
    public IndexHandle(Index index) {
        this.index = index;
        open = true;
        changed = false;
        lastWord = "";
    }

    /**
     *
     * @return the index structure
     */
    public Index getIndex() {
        return index;
    }

    /**
     * <p>
     * If the parametre is null it is false. In another case it replaces the index<br />
     * structure and returns true.
     * </p>
     *
     * @param index the new index structure
     * @return shows if the replacement is done
     */
    public boolean setIndex(Index index) {
        if (index == null) return false;
        
        this.index = index;
        changed = true;
        return true;
    }

    /**
     *
     * @return the open status
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * <p>Closes the <code>Index Handle</code></p>
     */
    public void setClosed() {
        open = false;
    }

    /**
     *
     * @return the change status
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     *
     * @return the index's name
     */
    public String getIndexName() {
        return this.index.getName();
    }

    /**
     * <p>
     * Adds a document to the index from a file path. Cleans the document from<br />
     * useless characters and puts its words to the index structure.<br /><br />
     * In case the file path is invalid, it returns false.
     * </p>
     *
     * @param filePath the document's path
     * @return informs the developer about the success of the insert
     */
    public boolean InsertDocument(String filePath) {

        String fileName = filePath.split("\\\\")[filePath.split("\\\\").length - 1];
        this.DeleteDocument(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));

            while (reader.ready()) {
                String[] words = this.cleanString(reader.readLine()).split(" ");
                index.putAll(words, fileName);
            }
            index.updateWeights();
            changed = true;
            
            return true;

        } catch (Exception ex) {
            return false;
        }
        
    }

    /**
     * <p>
     * Inserts all the documents of a folder, using the <code>InsertDocumet</code><br />
     * method.
     * </p>
     *
     * @param dirPath the directory's path
     * @return informs the developer about the success of the insert
     */
    public boolean InsertAllDocument(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory() || !dir.exists()) return false;

        File[] arrayFiles = dir.listFiles();
        for (File file : arrayFiles) {
            lastWord = "";
            if (file.getName().matches("[A-Za-z0-9]+\\.txt$"))
                this.InsertDocument(file.getPath());
        }

        changed = true;
        return true;
    }

    /**
     * <p>
     * Removes a document from the index. It removes the <code>DocumentInfo</code><br />
     * of the specific document from all the words of the index structure.
     * </p>
     *
     * @param filePath the document's path
     * @return informs the developer about the success of the delete
     */
    public boolean DeleteDocument(String filePath) {
        
        String fileName = filePath.split("\\\\")[filePath.split("\\\\").length - 1];

        if (index.removeDoc(fileName) > 0) {
            index.updateWeights();

            changed = true;
            return true;
        }
        else return false;

    }

    /**
     * <p>
     * Keeps only the characters and the number of a string and removes the<br />
     * additional white spaces.
     * </p>
     *
     * @param str the string that is going to be cleaned
     * @return the new (cleaned) string
     */
    private String cleanString(String str) {
        str = str.trim();
        if (!lastWord.equals("")) {
            str = lastWord + str;
        }
        lastWord = str.split(" ")[str.split(" ").length-1];
        if (!lastWord.matches("[A-Za-z-]+-$")) lastWord = "";
        else lastWord = lastWord.replaceAll("-$","");

        str = str.replaceAll("[^A-Za-z0-9,.]", " ");
        str = str.replaceAll("[,.]", "");
        str = str.replaceAll(" {2,}", " ");
        str = str.trim();
        return str;
    }

    /**
     *
     * @see #index
     * @return the index as string
     */
    @Override
    public String toString() {
        return index.toString();
    }

}
