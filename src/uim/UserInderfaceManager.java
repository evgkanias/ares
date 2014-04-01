package uim;

import bm.BooleanModelManager;
import java.io.*;
import javax.swing.event.ListSelectionEvent;
import vm.VectorModelManager;
import ixm.IndexHandle;
import ixm.IndexManager;
import ixm.Parser;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import vm.Metrics;

/**
 * <p>
 * This class is responsible for the User Interface (UI). It builts an interface<br />
 * where the user handles all the program's features.<br />
 * <br />
 * This interface has the menu bar,<br />
 * where the user can create, drop, open, save, close an index and add or remove one<br />
 * or more files to the index. The user can also choose between bollean or vector model<br />
 * and among euclidean, inner product, cosine, dive and jaccard method for the vector model.<br />
 * <br />
 * It has a panel where the user can type the query he wants or choose a file with<br />
 * queries (in a specific format) and search all of them to take the results.<br />
 * <br />
 * It also has a panel to show the results of the query-search where the user can<br />
 * see some information about the search and he is able to click on the results and see<br />
 * the content of the document.
 * </p>
 *
 * @see ButtonTabComponent
 * @see IndexManager
 * @see IndexManager
 *
 * @author Evripidis Gkanias
 * @author Stergios Giannouloudis
 *
 * @version 1.0
 */
public class UserInderfaceManager extends JFrame {
    private final int width = 800;              // the window's width
    private final int hight = 600;              // the window's hight
    private ImageIcon LOGO;
    private URL imageURL;
    
    private boolean isOpen;                     // shows if there is an open Index
    private boolean bModel;                     // shows if the Boolean model is turned on
    private int numOfResults;                   // shows how many results the user wants (-1 means all of them)
    private boolean fileResults;                // shows if the results that the program return are from a file search
    private String currentQuery;                // the query that is active
    private String resultInfo;                  // the information of the results
    private String selectedMethod = "cosine";   // the current selected method
    private final JLabel statusLabel;           // the status JLabel
    private JPanel contentPane;                 // the content panel
    private JMenu methodsMenu;                  // the JMenu that contains the methods
    private JMenuItem dropIndex;                // the "Drop Index" JMenuItem
    private JMenuItem saveIndex;                // the "Save Index" JMenuItem
    private JMenuItem closeIndex;               // the "Close Index" JMenuItem
    private JMenuItem addFile;                  // the "Add File" JMenuItem
    private JMenuItem removeFile;               // the "Remove File" JMenuItem
    private JRadioButtonMenuItem[] models;      // the "Choose Model" JMenuItem
    private JRadioButtonMenuItem[] methods;     // the "Choose Method" JMenuItem
    private JTextField queryField;              // the text field that the user types the query
    private JTextField fileField;               // the text field that the user types the path of the file with the queries
    private JTextField numOfResultsfield;       // the text field that the user types the number of the results he wants to show
    private JButton searchQueryButton;          // the button that searches for documents using the query
    private JButton searchFileButton;           // the button that searches for documents using the file path
    private JButton openButton;                 // the button that opens the file to search for its queries
    private JTabbedPane resultsPane;            // the results' list panel
    private JTabbedPane desktopPane;            // the preview panel
    private IndexManager iManager;              // manages the indexes
    private IndexHandle iHandler;               // handles the current index
    private BooleanModelManager bManager;       // manages the results when the Boolean Model is turned on
    private VectorModelManager vManager;        // manages the results when the Vector Model is turned on
    private Metrics metrics;                    // computes the metrics
    private DecimalFormat df;                   // converts the doubles to a prefered format

    /**
     * <p>It sets the initial values and starts the program.</p>
     */
    public UserInderfaceManager() {
        super("Information Retrieval System (ARES)");

        ClassLoader cldr = this.getClass().getClassLoader();
        imageURL = cldr.getResource("uim\\ares.png");
        if (imageURL == null)
            imageURL = cldr.getResource("uim/ares.png");
        
        if (imageURL != null) {
            LOGO = new ImageIcon(imageURL, "ARES logo");
            this.setIconImage(LOGO.getImage());
        }
        df = new DecimalFormat("#.###");
        isOpen = false;
        bModel = false;
        iManager = new IndexManager();
        vManager = new VectorModelManager();
        numOfResults = -1;
        this.statusLabel = new JLabel("");
        updateStatus("Done.");

        makeFrame();

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
    }

    /**
     * <p>
     * It constructs the window. Sets the parameters to the content's panel and
     * puts every panel to its position.
     * </p>
     */
    protected void makeFrame() {
        contentPane = (JPanel) getContentPane();
        contentPane.setBorder(new EmptyBorder(5,5,5,5));

        contentPane.setLayout(new BorderLayout(1,1));

        makeMenuBar();
        fillContentPane();
        contentPane.add(statusLabel, BorderLayout.SOUTH);

        pack();
        setSize(width,hight);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(d.width/2 - getWidth()/2, d.height/2 - getHeight()/2);
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * <p>It makes the menu bar and puts it to the window.</p>
     */
    protected void makeMenuBar() {
        final int SHORTCUT_MASK =
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        JMenu indexMenu = new JMenu("Index");
        indexMenu.setMnemonic('I');
        menubar.add(indexMenu);

        JMenu modelMenu = new JMenu("Model");
        modelMenu.setMnemonic('M');
        menubar.add(modelMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        menubar.add(helpMenu);
        
        indexMenu.add(this.getCreateIdextItem(SHORTCUT_MASK));
        indexMenu.add(this.getDropIndexItem(SHORTCUT_MASK));
        indexMenu.addSeparator();
        indexMenu.add(this.getOpenIndexItem(SHORTCUT_MASK));
        indexMenu.add(this.getSaveIndexItem(SHORTCUT_MASK));
        indexMenu.add(this.getCloseIndexItem(SHORTCUT_MASK));
        indexMenu.addSeparator();
        indexMenu.add(this.getAddFileItem(SHORTCUT_MASK));
        indexMenu.add(this.getRemoveFileItem(SHORTCUT_MASK));
        indexMenu.addSeparator();
        indexMenu.add(this.getExitItem(SHORTCUT_MASK));

        modelMenu.add(this.getModelsMenu());
        modelMenu.add(this.getMethodsMenu());

        helpMenu.add(this.getAboutItem(SHORTCUT_MASK));
    }

    /**
     * <p>Creates the "Create Index" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Create Index" menu item
     */
    protected JMenuItem getCreateIdextItem(int mask) {
        JMenuItem item = new JMenuItem("Create New Index...");
        item.setMnemonic('N');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select a collection file to create the Index");
                fc.setCurrentDirectory(new File("Collections"));
                FileFilter filter = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith("_docs.txt") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Index Document Source File";
                    }
                };
                fc.setFileFilter(filter);
                if (fc.showDialog(rootPane,"Select") == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.getName().endsWith("_docs.txt")) {
                        Parser.ParsFile(file.getPath());
                        String indexName = file.getParent();
                        indexName = indexName.split("\\\\")[indexName.split("\\\\").length-1];

                        updateStatus("Creating " + indexName + "'s Index...");
                        long startTime = System.nanoTime();
                        if (iManager.CreateIndex(indexName)) {
                            long endTime = System.nanoTime();
                            double elapsedTime = (double) (endTime - startTime)/1000000000.0;
                            updateStatus(indexName + "'s Index has been created. Elapsed time: " + df.format(elapsedTime) + " sec.");
                        } else
                            updateStatus(indexName + "'s could not be created. An unknown error has occured.");
                    } else updateStatus("Wrong type of file. Index has not been created.");
                } else updateStatus("Process canceled. Index has not been created.");
            }
        });
        return item;
    }

    /**
     * <p>Creates the "Drop Index" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Drop Index" menu item
     */
    protected JMenuItem getDropIndexItem(int mask) {
        JMenuItem item = new JMenuItem("Drop Opened Index");
        item.setMnemonic('D');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, mask));
        item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            setOpened(false);
                            updateStatus("Preparing to drop Index...");
                            if (iManager.DropIndex(iHandler.getIndexName())) {
                                updateStatus("Index " + iHandler.getIndexName() + " has successfully been destroied.");
                            } else
                                updateStatus("Couldn't destroy Index files. The rocess has been canceled.");
                        }
                    });
        item.setEnabled(isOpen);
        return (this.dropIndex = item);
    }

    /**
     * <p>Creates the "Open Index" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Open Index" menu item
     */
    protected JMenuItem getOpenIndexItem(int mask) {
        JMenuItem item = new JMenuItem("Open Index...");
        item.setMnemonic('O');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open Index File");
                fc.setCurrentDirectory(new File("Collections\\INDEXES"));
                FileFilter filter = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".idx") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Index File";
                    }
                };
                fc.setFileFilter(filter);
                if (fc.showDialog(rootPane,"Open") == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.getName().endsWith(".idx")) {
                        String indexName = file.getName();
                        indexName = indexName.split("\\.")[0];
                        updateStatus("Openning " + indexName + " Index...");
                        if (iHandler != null) iManager.CloseIndex(iHandler);
                        if ((iHandler = iManager.OpenIndex(indexName)) != null) {
                            setOpened(true);
                            updateStatus("Succeed to open Index " + indexName + ".");
                        }else updateStatus("Could not open Index " + indexName + ".");
                    } else updateStatus("Wrong type of file. Index is not opened.");
                } else
                    updateStatus("Process canceled. Index is not opened.");
            }
        });
        return item;
    }

    /**
     * <p>Creates the "Save Index" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Save Index" menu item
     */
    protected JMenuItem getSaveIndexItem(int mask) {
        JMenuItem item = new JMenuItem("Save Index");
        item.setMnemonic('S');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
        item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iHandler != null || !iHandler.isOpen()) {
                                updateStatus("Saving " + iHandler.getIndexName() + "'s Index...");
                                if (iManager.SaveIndex(iHandler)) {
                                    updateStatus("Index saved.");
                                } else
                                    updateStatus("Index could not be saved. An unknown error has occured.");
                            } else updateStatus("There is no opened Index to save.");
                        }
                    });
        item.setEnabled(isOpen);
        return (this.saveIndex = item);
    }

    /**
     * <p>Creates the "Close Index" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Close Index" menu item
     */
    protected JMenuItem getCloseIndexItem (int mask) {
        JMenuItem item = new JMenuItem("Close Index");
        item.setMnemonic('C');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, mask));
        item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iHandler != null || !iHandler.isOpen()) {
                                updateStatus("Closing " + iHandler.getIndexName() + "'s Index...");
                                if (iManager.CloseIndex(iHandler)) {
                                    updateStatus("Done.");
                                    setOpened(false);
                                }
                            } else updateStatus("There is no opened Index to close.");
                        }
                    });
        item.setEnabled(isOpen);
        return (this.closeIndex = item);
    }

    /**
     * <p>Creates the "Add File" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Add File" menu item
     */
    protected JMenuItem getAddFileItem(int mask) {
        JMenuItem item = new JMenuItem("Add File(s)...");
        item.setMnemonic('A');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select File(s) to Add");
                fc.setCurrentDirectory(new File("Collections"));
                FileFilter filter = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".txt") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Text File";
                    }
                };
                fc.setFileFilter(filter);
                fc.setMultiSelectionEnabled(true);
                if (fc.showDialog(rootPane,"Add") == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();
                    int count = 0;
                    if (iHandler != null) {
                        long startTime = System.nanoTime();
                        for (File file : files) {
                            if (file.getName().endsWith(".txt")) {
                                updateStatus("Adding " + file.getName() + " ...");
                                if (!iHandler.InsertDocument(file.getAbsolutePath())) {
                                    count++;
                                    updateStatus("Could not add file. An unknown error has occured.");
                                }
                            } else {
                                updateStatus("Wrong type of file. File has not been added.");
                                count++;
                            }
                        }
                        if (files.length > count) {
                            long endTime = System.nanoTime();
                            double elapsedTime = (double) (endTime - startTime) / 100000000.0;
                            if (files.length == 1) updateStatus("Succeed to add " + files[0].getName() + ". Elapsed time: " + df.format(elapsedTime) + " sec.");
                            else updateStatus("Succeed to add " + count + " files. Elapsed time: " + df.format(elapsedTime) + " sec.");
                        }
                    }else updateStatus("Process canceled. There is no opened index.");
                } else
                    updateStatus("Process canceled.");
            }
        });
        item.setEnabled(isOpen);
        return (this.addFile = item);
    }

    /**
     * <p>Creates the "Remove File" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Remove File" menu item
     */
    protected JMenuItem getRemoveFileItem(int mask) {
        JMenuItem item = new JMenuItem("Remove File(s)...");
        item.setMnemonic('R');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select File(s) to Remove");
                fc.setCurrentDirectory(new File("Collections\\" + iHandler.getIndexName() + "\\DOCS"));
                FileFilter filter = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        boolean accepted = false;
                        for (String fileName : iHandler.getIndex().getDocNames())
                            accepted = accepted || f.getName().equals(fileName);

                        return accepted || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Included File";
                    }
                };
                fc.setFileFilter(filter);
                fc.setMultiSelectionEnabled(true);
                if (fc.showDialog(rootPane,"Remove") == JFileChooser.APPROVE_OPTION) {
                    File[] files = fc.getSelectedFiles();
                    int count = 0;
                    if (iHandler != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".txt")) {
                                updateStatus("Removing " + file.getName() + " ...");
                                if (!iHandler.DeleteDocument(file.getAbsolutePath())) {
                                    count++;
                                    updateStatus("Could not remove file. An unknown error has occured.");
                                }
                            } else {
                                updateStatus("Wrong type of file. File has not been removed.");
                                count++;
                            }
                        }
                        if (files.length > count) {
                            if (files.length == 1) updateStatus("Succeed to remove " + files[0].getName() + ".");
                            else updateStatus("Succeed to remove " + count + " files.");
                        }
                    }else updateStatus("Process canceled. There is no opened index.");
                } else
                    updateStatus("Process canceled.");
            }
        });
        item.setEnabled(isOpen);
        return (this.removeFile = item);
    }

    /**
     * <p>Creates the "Save and Exit" menu item for the "Index" menu.</p>
     *
     * @param mask the shortcut mask
     * @return the "Save and Exit" menu item
     */
    protected JMenuItem getExitItem(int mask) {
        JMenuItem item = new JMenuItem("Save and Exit");
        item.setMnemonic('x');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
        item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iHandler != null) iManager.CloseIndex(iHandler);
                            System.exit(1);
                        }
                    });
        return item;
    }

    /**
     * <p>
     * Creates the "About" menu item for the "Help" menu. It contains information
     * about the authors and the version of the program.
     * </p>
     *
     * @param mask the shortcut mask
     * @return the "About" menu item
     */
    protected JMenuItem getAboutItem(int mask) {
        JMenuItem item = new JMenuItem("About");
        item.setMnemonic('A');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, mask));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message;
                message = "InformAtion REtrieval System (ARES)\n\n" +
                          "Version:    v1.0\n\n" +
                          "Authors:\n" +
                          "    Gkanias Evripidis\n" +
                          "    Giannouloudis Stergios\n\n" +
                          "Copyright 2012\n\n";
                JOptionPane.showMessageDialog(null, message, "About ARES",
                        JOptionPane.INFORMATION_MESSAGE, LOGO);
                
            }
        });
        return item;
    }

    /**
     * <p>
     * Creates the "Choose Model" menu for the "Model" menu from where the user
     * can change the model.
     * </p>
     *
     * @return the "Choose Model" menu
     */
    protected JMenu getModelsMenu() {
        JMenu menu = new JMenu("Choose Model");
        menu.setMnemonic('M');

        String[] modelNames = {"Vector Model", "Boolean Model"};
        models = new JRadioButtonMenuItem[modelNames.length];
        ButtonGroup modelButtonGroup = new ButtonGroup();

        for (int i = 0; i < models.length; i++) {
            models[i] = new JRadioButtonMenuItem(modelNames[i]);
            models[i].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (e.getActionCommand().equals("Vector Model")) {
                                bModel = false;
                                updateStatus("Vector Model has turned on.");
                                methodsMenu.setEnabled(true);
                                if (isOpen) setQueryPaneEnabled(true);
                            } else {
                                bModel = true;
                                updateStatus("Boolean Model has turned on.");
                                if (bManager == null) bManager = new BooleanModelManager();
                                methodsMenu.setEnabled(false);
                                if (isOpen) setQueryPaneEnabled(true);
                            }
                        }
                    });
            menu.add(models[i]);
            modelButtonGroup.add(models[i]);
        }
        models[0].setSelected(true);

        return menu;
    }

    /**
     * <p>
     * Creates the "Choose Method" menu for the "Model" menu from where the user
     * can change the vector's method.
     * </p>
     *
     * @return the "Choose Method" menu
     */
    protected JMenu getMethodsMenu() {
       JMenu menu = new JMenu("Choose Method");
        menu.setMnemonic('d');

        String[] methodNames = {"Euclidean Distance", "Inner Product", "Cosine", "Dice", "Jaccard"};
        methods = new JRadioButtonMenuItem[methodNames.length];
        ButtonGroup modelButtonGroup = new ButtonGroup();

        for (int i = 0; i < methods.length; i++) {
            methods[i] = new JRadioButtonMenuItem(methodNames[i]);
            methods[i].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (e.getActionCommand().equals("Euclidean Distance")) {
                                selectedMethod = "euclidean";
                                updateStatus("\"Euclidean Distance\" method has been selected.");
                            } else if (e.getActionCommand().equals("Inner Product")) {
                                selectedMethod = "inner product";
                                updateStatus("\"Inner Product\" method has been selected.");
                            } else if (e.getActionCommand().equals("Cosine")) {
                                selectedMethod = "cosine";
                                updateStatus("\"Cosine\" method has been selected.");
                            } else if (e.getActionCommand().equals("Dice")) {
                                selectedMethod = "dice";
                                updateStatus("\"Dice\" method has been selected.");
                            } else {
                                selectedMethod = "jaccard";
                                updateStatus("\"Jaccard\" method has been selected.");
                            }
                        }
                    });
            menu.add(methods[i]);
            modelButtonGroup.add(methods[i]);
        }
        methods[2].setSelected(true);
        methodsMenu = menu;
        
        return menu;
    }

    /**
     * <p>
     * Fills the content Panel. It splits the panel in three subpanels:
     * <ul>
     * <li>the <code>DesktopPane</code>, which shows the selected file's preview</li>
     * <li>the <code>ResultsPane</code>, which contains a <code>JList</code> with the search-results</li>
     * <li>the <code>QueryPane</code>, where the user can fill the parametres for the search</li>
     * </ul>
     * </p>
     */
    protected void fillContentPane() {

        JSplitPane vsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane hsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        hsp.setContinuousLayout(true);
        hsp.setOneTouchExpandable(false);
        hsp.setResizeWeight(1);

        vsp.setDividerLocation(450);
        hsp.setDividerLocation(240);

        desktopPane = getDesktopPane();
        JScrollPane results = getResultsPane();
        JTabbedPane queryTabbedPane = getQueryPane();

        hsp.add(vsp,"right");
        hsp.setLeftComponent(results);
        vsp.setTopComponent(desktopPane);
        vsp.setBottomComponent(queryTabbedPane);

        contentPane.add(hsp);
    }

    /**
     * <p>Creates the <code>DesktopPane</code> where the files' preview will be shown.</p>
     *
     * @return the <code>DesktopPane</code> panel
     */
    protected JTabbedPane getDesktopPane() {
        JTabbedPane desktop = new JTabbedPane() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(LOGO.getImage(), 250, 100, this);
                g2d.finalize();
            }
        };
        desktop.setName("Desktop");

        return desktop;
    }

    /**
     * <p>
     * Creates the <code>ResultsPane</code> panel. In that panel the results'
     * <code>JList</code> is placed and the user can see matched
     * documents and choose them to see the preview in the <code>DesktopPane</code>.
     * </p>
     *
     * @return the <code>ResultsPane</code>
     */
    protected JScrollPane getResultsPane() {

        resultsPane = new JTabbedPane();
        resultsPane.setName("Results");
        resultsPane.setTabPlacement(JTabbedPane.LEFT);
        resultsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JScrollPane results = new JScrollPane(resultsPane);
        results.setName("Results");

        return results;

    }

    /**
     * <p>
     * Creates the <code>QueryPane</code> where there are three Tabs:
     * <ul>
     * <li>the <code>ExecuteQuery</code> tab, where the user can type a query to take some results</li>
     * <li>the <code>ExecuteFromQueryFile</code> tab, where the user can choose a file with queries and take its queries' results</li>
     * <li>the <code>VectorOptions</code> tab, where the user can choose if he wants to have all or some results</li>
     * </ul>
     * </p>
     *
     * @return the <code>QueryPane</code>
     */
    protected JTabbedPane getQueryPane() {
        JTabbedPane query = new JTabbedPane();
        query.setName("Query");

        JPanel queryPane = new JPanel(new BorderLayout());
        queryPane.setName("Enter query to execute");
        queryPane.setEnabled(query.isEnabled());
        searchQueryButton = getQueryButton();
        queryPane.add(searchQueryButton,BorderLayout.EAST);
        queryField = new JTextField();
        queryField.addActionListener(searchQueryButton.getActionListeners()[0]);
        queryPane.add(queryField,BorderLayout.CENTER);
        query.add(queryPane);

        JPanel filePane = new JPanel(new BorderLayout());
        filePane.setName("Select a file to execute its queries");
        openButton = getOpenFileButton();
        filePane.add(openButton,BorderLayout.CENTER);
        searchFileButton = getSearchFileButton();
        filePane.add(searchFileButton,BorderLayout.EAST);
        fileField = new JTextField(34);
        fileField.addActionListener(searchFileButton.getActionListeners()[0]);
        filePane.add(fileField,BorderLayout.WEST);
        query.add(filePane);

        JPanel optionPane = new JPanel();
        optionPane.setName("Options");
        optionPane.setToolTipText("This Tab is only for Vector Mode");
        JRadioButton all = new JRadioButton("All");
        JRadioButton kTop = new JRadioButton("k-top");
        numOfResultsfield = new JTextField(3);
        numOfResultsfield.setName("k");
        numOfResultsfield.setText("10");
        numOfResultsfield.selectAll();
        numOfResultsfield.setEnabled(false);
        ButtonGroup group = new ButtonGroup();
        all.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                numOfResults = -1;
                numOfResultsfield.setEnabled(false);
            }
        });
        all.setSelected(true);
        kTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                numOfResultsfield.setEnabled(true);
                try {
                    numOfResults = Integer.parseInt(numOfResultsfield.getText());
                } catch (Exception ex) {
                    numOfResults = 10;
                }
                numOfResultsfield.selectAll();
            }
        });
        
        group.add(all);
        group.add(kTop);
        optionPane.add(all,BorderLayout.WEST);
        optionPane.add(kTop,BorderLayout.CENTER);
        optionPane.add(numOfResultsfield,BorderLayout.CENTER);

        query.add(optionPane);

        this.setQueryPaneEnabled(false);
        return query;
    }

    /**
     * <p>This button searches for results, using the query that the user typed.</p>
     *
     * @return the <code>QueryButton</code>
     */
    protected JButton getQueryButton() {
        JButton button = new JButton("Search");
        button.setName("Search Query");
        button.setToolTipText("Click to execute the query");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                fileResults = false;
                double time = redirectToResultsTab(currentQuery = queryField.getText());

                queryField.selectAll();
                if (!statusLabel.getText().equals("") && !statusLabel.getText().equals("No results found."))
                    updateStatus("Done. Elapsed time: " + df.format(time) + " sec.");
                else
                    updateStatus("No results found.");
            }
        });

        return button;
    }

    /**
     * <p>
     * This button searches for every query's results, using the file path that
     * the user typed or the file that the user chose with the <code>OpenFileButton</code>.
     * </p>
     *
     * @return the <code>SearchFileButton</code>
     */
    protected JButton getSearchFileButton() {
        JButton button = new JButton("Search");
        button.setName("Search File");
        button.setToolTipText("Click to execute the file's queries");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!bModel && !fileField.getText().equals(""))  {
                    vManager.setIndexHandle(iHandler);
                    ArrayList<String> results = Parser.ParsQueryFile(fileField.getText());
                    double time = 0;
                    fileResults = true;
                    metrics = new Metrics(iHandler.getIndexName());
                    for (int i = 0; i < results.size(); i++) {
                        metrics.setQueryNumber(i+1);
                        time += redirectToResultsTab(currentQuery = results.get(i));
                    }

                    DecimalFormat df = new DecimalFormat("#.###");
                    fileField.selectAll();
                    updateStatus("Done. Elapsed time: " + df.format(time) + " sec.");

                }

            }
        });

        return button;
    }

    /**
     * <p>
     * This button pops up a window and allows the user to choose a "Query File"
     * to open. When he will choose a file, he has to push the "Search" button to
     * execute the file's queries.
     * </p>
     *
     * @return the <code>OpenFileButton</code>
     */
    protected JButton getOpenFileButton() {
        JButton button = new JButton("Open");
        button.setName("Open File");
        button.setToolTipText("Click to open a file");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open a file to load the queries");
                fc.setCurrentDirectory(new File("Collections"));
                FileFilter filter = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith("_queries.txt") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Queries Source File";
                    }
                };
                fc.setFileFilter(filter);
                if (fc.showDialog(rootPane,"Open") == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.getName().endsWith("_queries.txt")) {
                        fileField.setText(file.getPath());
                        updateStatus("Query file path has been chosen.");
                    } else updateStatus("Wrong type of file. Queries file is not opened.");
                } else
                    updateStatus("Process canceled. Queries file is not opened.");
            }
        });

        return button;
    }

    /**
     * <p>
     * Adds a tab to the <code>ResultsPane</code> with the results of the executed
     * query. It prints on the top of the results tab some information about the
     * results and some statistics in case they are available.
     * </p>
     *
     * @param content the names of the matched files
     */
    protected void addResultsTab(Object[] content) {
        final JList list = new JList();
        try {
            list.setListData(content);
        } catch (Exception ex ) {}

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellWidth(60);

        list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                String path = "Collections\\" + iHandler.getIndexName() + "\\DOCS\\";
                if (e.getValueIsAdjusting() && list.getSelectedIndex() > -1) {

                    boolean done = false;
                    String result = (String) list.getSelectedValue();
                    for (int i = 0; i < desktopPane.getTabCount(); i++) {
                        String tabName = desktopPane.getTabComponentAt(i).getName();
                        if (tabName.equals(result)) {
                            desktopPane.setSelectedIndex(i);
                            done = true;
                            break;
                        }
                    }
                    if (done) {
                        return;
                    }
                    BufferedReader reader = null;
                    try {
                        path += result;
                        File file = new File(path);
                        reader = new BufferedReader(new FileReader(file));
                        JPanel tab = new JPanel(new BorderLayout());
                        JTextArea text = new JTextArea() {

                            @Override
                            public void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                ClassLoader cldr = this.getClass().getClassLoader();
                                java.net.URL imageURL = cldr.getResource("uim\\ares_bgr.png");
                                if (imageURL == null)
                                    imageURL = cldr.getResource("uim/ares_bgr.png");
                                if (imageURL == null) return;
                                
                                ImageIcon bgr = new ImageIcon(imageURL, "Tab background");
                                Graphics2D g2d = (Graphics2D) g;
                                g2d.drawImage(bgr.getImage(), 250, 75, this);
                                g2d.finalize();
                            }
                        };
                        text.read(reader, null);
                        text.setEditable(false);
                        tab.setName(result);
                        tab.add(text);
                        desktopPane.addTab(result, null, tab, result);
                        int lastElement = desktopPane.getTabCount() - 1;
                        desktopPane.setTabComponentAt(lastElement, new ButtonTabComponent(desktopPane));
                        desktopPane.getTabComponentAt(lastElement).setName(result);
                        desktopPane.setSelectedIndex(lastElement);

                    } catch (IOException ex) {} finally {
                        try {
                            reader.close();
                        } catch (IOException ex) {}
                    }
                }
            }
        });
        JPanel panel = new JPanel(new BorderLayout());
        resultInfo = "<html>";
        try {
            if (fileResults) {
                resultInfo +=
                    "Recall: " + df.format(metrics.Recall()) + "<br />" +
                    "Precision: " + df.format(metrics.Prescision()) + "<br />";
            }
            resultInfo += content.length + " results<br /></html>";
        } catch (Exception ex) {
            resultInfo += "No results found.<br /></html>";
        }
        JLabel resInfo = new JLabel(resultInfo);
        resInfo.setBorder(BorderFactory.createTitledBorder("Result Info"));
        panel.add(resInfo, BorderLayout.NORTH);
        panel.add(list,BorderLayout.CENTER);
        int lastElement = resultsPane.getTabCount();
        String front;
        if (bModel)
            front = "Boolean";
        else
            front = selectedMethod;
        resultsPane.addTab(
                "Result #" + (lastElement + 1),
                null,
                panel,
                front + ": " + currentQuery);
        resultsPane.setTabComponentAt(lastElement, new ButtonTabComponent(resultsPane));
        resultsPane.getTabComponentAt(lastElement).setName("Result #" + (lastElement + 1));
        resultsPane.setSelectedIndex(lastElement);
    }

    /**
     * <p>
     * Executes the query and adds the results to a new tab using the <code>addResultsTab</code>
     * function.
     * </p>
     *
     * @param query the query that is to be executed
     * @return the elapsed time in seconds
     */
    protected double redirectToResultsTab(String query) {

        long startTime = System.nanoTime();
        boolean done = false;
        for (int i = 0; i < resultsPane.getTabCount(); i++) {
            String toolTip = resultsPane.getToolTipTextAt(i);
            if (toolTip.equals(selectedMethod + ": " + query) || toolTip.equals("Boolean: " + query)) {
                resultsPane.setSelectedIndex(i);
                done = true;
                break;
            }
        }
        if (done) {
            long endTime = System.nanoTime();
            return (double) (endTime - startTime) / 1000000000.0;
        }
        ArrayList<String> result;
        if (bModel) {
            bManager.setIndexHandle(iHandler);
            result = bManager.excecuteQuery(query);
        } else {
            vManager.setIndexHandle(iHandler);
            if (numOfResults < 0) {
                result = vManager.excecuteQuery(query,selectedMethod);
            } else {
                numOfResults = Integer.parseInt(numOfResultsfield.getText());
                result = vManager.getTopK(query, selectedMethod, numOfResults);
            }
        }
        try {
            if (result == null)
                updateStatus("");
            else if (result.size() == 0)
                updateStatus("No results found.");
            else {
                if (fileResults && !bModel)
                    metrics.setResults(result);
                addResultsTab(result.toArray(new String[result.size()]));
            }
        } catch (Exception ex) {}
        long endTime = System.nanoTime();
        
        return (double) (endTime - startTime) / 1000000000.0;
    }

    /**
     * <p>Sets the panels of the <code>QueryPane</code> enabled or disabled.</p>
     *
     * @param b <code>true</code> for enable or <code>false</code> for disable
     */
    protected void setQueryPaneEnabled(boolean b) {
        queryField.setEnabled(b);
        fileField.setEnabled(b && !bModel);
        searchQueryButton.setEnabled(b);
        searchFileButton.setEnabled(b && !bModel);
        openButton.setEnabled(b && !bModel);
    }

    /**
     * <p>
     * Sets the <code>IndexHandle</code> opened or closed and updates the buttons'
     * and items' status.
     * </p>
     *
     * @param b <code>true</code> for enable or <code>false</code> for disable
     */
    protected void setOpened(boolean b) {
        setQueryPaneEnabled(b);
        dropIndex.setEnabled(b);
        saveIndex.setEnabled(b);
        closeIndex.setEnabled(b);
        addFile.setEnabled(b);
        removeFile.setEnabled(b);
        isOpen = b;

        if (!b) {
            resultsPane.removeAll();
            desktopPane.removeAll();
            iManager.CloseIndex(iHandler);
            iHandler = null;
        }
    }

    /**
     * <p>Updates the status-bar's message</p>
     *
     * @param message the new message
     */
    protected void updateStatus(String message) {
        this.statusLabel.setText(message);
    }
}