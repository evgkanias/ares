package vm;

/**
 * Instaces of this class hold the rank of a document for a specific query.
 * 
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 */
public class DocumentRank {
    private String name;
    private double rank;

    /**
     * Initializes the rank of a document for a specific query.
     * @param name Document's name.
     * @param rank The rank this document got for a specific query.
     */
    public DocumentRank(String name, double rank) {
        this.name = name;
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public double getRank() {
        return rank;
    }

    
}
