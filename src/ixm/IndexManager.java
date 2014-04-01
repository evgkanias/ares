package ixm;

import java.io.*;

/**
 * <p>
 * This class is responsible for managing the index files. It creates, drops, opens<br />
 * and closes the indexs' files.
 * </p>
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 *
 * @version 1.0
 */
public class IndexManager implements Serializable {

    final String PATH = "Collections\\INDEXES\\";   // the indexes' directory path
    final String ENDING = ".idx";                   // the indexes' ending

    /**
     * <p>It creates the indexes' directory</p>
     */
    public IndexManager() {
        File newDir = new File(PATH);
        newDir.mkdir();
    }

    /**
     * <p>
     * Creates a new index in the indexes' directory.
     * </p>
     *
     * @param inxName the index name
     * @return informs the developer if the creation has been completed successfully
     */
    public boolean CreateIndex(String inxName) {

        if (this.OpenIndex(inxName) != null) return false;

        IndexHandle iHandle = new IndexHandle(new Index(inxName));
        iHandle.InsertAllDocument("Collections\\" + inxName + "\\DOCS");
        if (this.CloseIndex(iHandle))
            return true;
        else
            return false;
    }

    /**
     * <p>Destroys the index file.</p>
     *
     * @param inxName the index's name
     * @return informs if the index has been destroyed successfully
     */
    public boolean DropIndex(String inxName) {

        try {
            File inxFile = new File(PATH + inxName + ENDING);
            File srcFile = new File("Collections\\" + inxName + "\\DOCS");

            if (!inxFile.exists() || !srcFile.exists())
                throw new IllegalArgumentException("No such file or directory");

            for (File f : srcFile.listFiles())
                f.delete();

            return inxFile.delete() && srcFile.delete();
        } catch (Exception ex) {
            return false;
        }
        
    }

    /**
     * <p>If it cannot find the index's file, returns null.</p>
     *
     * @param inxName the index's name
     * @return the <code>IndexHandle</code> of the opened index
     */
    public IndexHandle OpenIndex(String inxName) {

        try {
            ObjectInputStream reader =
                    new ObjectInputStream(new
                    FileInputStream(PATH + inxName + ENDING));

            Index index = (Index) reader.readObject();

            reader.close();

            return new IndexHandle(index);
        } catch (IOException ex) {
            return null;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * <p>Sets the <code>IndexHandle</code> closed.</p>
     *
     * @param indexHandle the <code>IndexHandle</code> that is going to be closed
     * @return shows if the index has been closed successfully
     */
    public boolean CloseIndex(IndexHandle indexHandle) {
        if (!indexHandle.isOpen()) return false;
        if (!indexHandle.isChanged()) {
            indexHandle.setClosed();
            return true;
        }

        if (this.SaveIndex(indexHandle)) {
            indexHandle.setClosed();
            return true;
        } else return false;
    }

    /**
     * <p>Saves the <code>Index</code> to the Hard Disk.</p>
     *
     * @param indexHandle the <code>IndexHandle</code> of the index that is going to be closed
     * @return informs if the index has been saved successfully
     */
    public boolean SaveIndex(IndexHandle indexHandle) {
        if (!indexHandle.isOpen()) return false;
        if (!indexHandle.isChanged()) {
            return true;
        }

        ObjectOutputStream writer = null;
        try {
            writer =
                    new ObjectOutputStream(new
                    FileOutputStream(PATH + indexHandle.getIndexName() + ENDING));

            indexHandle.getIndex().removeFrequentWords();
            writer.writeObject(indexHandle.getIndex());
            

            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {}
        }
    }
}
