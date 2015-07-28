package psu.agilemethods.src;

/**
 * Created by jfortier on 7/14/15.
 */
public class TextUI {

    private static final String WELCOME = "Welcome to Team Niner's FTP Client";

    public TextUI () {}

    /**
     * Initialize console interactions with the user
     */
    public static void start() {

        display(WELCOME);

    }

    /**
     * Shortcut method for printing a message to console.
     * Messages are printed as-is with no line breaks added.
     * @param msg The String to be output to console.
     */
    private static void display(String msg) {
        System.out.println(msg);
        //System.console().writer().print(msg);
    }
}
