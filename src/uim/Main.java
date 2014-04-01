package uim;

import java.io.Serializable;

/**
 * <p>
 * The main class of the program. It makes a new User Inderface by creating an
 * <code>UserInderfaceManager</code>.
 * </p>
 *
 * @see UserInderfaceManager
 *
 * @author Evripidis Gkanias
 * @author Sterios Giannouloudis
 *
 * @version 1.0
 */
public class Main implements Serializable {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UserInderfaceManager uim = new UserInderfaceManager();
    }

}
