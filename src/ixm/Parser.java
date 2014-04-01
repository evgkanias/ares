package ixm;

import java.io.*;
import java.util.ArrayList;

/**
 * <p>
 * This class parses the collections' files and creates their documents in a new<br />
 * directory named "DOCS" in the collection's directory.
 * </p>
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 *
 * @version 1.0
 */
public class Parser {
    private static final String ID = ".I";          // the identifier's symbol
    private static final String TITLE = ".T";       // the title's symbol
    private static final String NOTHING_1 = ".A";   //
    private static final String NOTHING_2 = ".B";   //
    private static final String CONTENT = ".W";     // the content's symbol

    /**
     * <p>
     * Creates the collection's documents in a new directory named "DOCS" in the<br />
     * collection's directory.
     * </p>
     *
     * @param filePath the collection's file path
     */
    public static void ParsFile(String filePath) {
        String[] sFilePath = filePath.split("\\\\");
        int pos = sFilePath.length -1;
        String dirPath = filePath.replace(sFilePath[pos],"");
        dirPath += "DOCS\\";
        String mode = "";
        
        File newDir = new File(dirPath);
        newDir.mkdir();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            BufferedWriter writer = null;
            String line;
            StringBuilder sb = new StringBuilder("");
            File file;

            while (reader.ready()) {
                line = reader.readLine().trim();
                if (line.startsWith(ID)) {
                    mode = "id";

                    try {
                        writer.write(sb.toString());
                        writer.close();

                        sb = new StringBuilder("");
                    } catch (Exception ex) {}

                    String fileName = line.split(" ")[1];
                    file = new File(dirPath + fileName + ".txt");
                    file.createNewFile();
                    writer = new BufferedWriter(new FileWriter(file));
                    continue;
                }
                if (line.startsWith(TITLE)) {
                    mode = "title";
                    continue;
                }
                if (line.startsWith(NOTHING_1) || line.startsWith(NOTHING_2)) {
                    mode = "nothing";
                    continue;
                }
                if (line.startsWith(CONTENT)) {
                    mode = "content";
                    continue;
                }
                if (mode.equals("nothing")) continue;

                sb.append(line);
                sb.append("\n");

            }

            writer.write(sb.toString());

            writer.close();
            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     *
     * @param filePath the file with the queries
     * @return an <code>ArrayList</code> with the queries.
     */
    public static ArrayList<String> ParsQueryFile(String filePath) {
        ArrayList<String> queries = new ArrayList();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            StringBuilder sb = new StringBuilder("");
            int counter = 0;

            while (reader.ready()) {
                line = reader.readLine().trim();
                if (line.startsWith(ID)) {

                    if (counter > 0) {
                        queries.set(counter-1, sb.toString().trim());
                        sb = new StringBuilder("");
                    }

                    queries.add("");
                    counter++;

                    continue;
                }
                if (line.startsWith(CONTENT))  continue;

                sb.append(line);
                sb.append(" ");

            }

            String query = sb.toString().replaceAll("[^A-Za-z0-9]", " ");
            query = query.replaceAll(" {2,}", " ");
            queries.set(counter-1, query.trim());

            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return queries;
    }

    /**
     * Parses the file that contains the results of each query of the collection.
     * @param filePath The system's directory where the file is found.
     * @return A list  of each query and its relevant documents.
     */
   public static ArrayList<ArrayList<String>> ParsRelevant(String filePath) {
        ArrayList<ArrayList<String>> relevants = new ArrayList();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            int queryNumber = 0, previusQueryNumber = 0;

            while (reader.ready()) {
                line = reader.readLine().trim();

                String[] args = line.split(" ");
                queryNumber = Integer.parseInt(args[0]);
                String docsName = args[1] + ".txt";

                if(previusQueryNumber != queryNumber){
                    relevants.add(new ArrayList());
                    previusQueryNumber = queryNumber;
                }

                relevants.get(queryNumber-1).add(docsName);
            }

            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return relevants;
    }

}
