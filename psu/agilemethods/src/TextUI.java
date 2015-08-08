package psu.agilemethods.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jfortier on 7/14/15.
 */
public class TextUI {

    private static final String WELCOME = "Welcome to Team Niner's FTP Client";

    public TextUI() {
    }

    /**
     * Initialize console interactions with the user
     */
    public static void start() {
        /*Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }*/

        display(WELCOME);

        /*String login = c.readLine("Enter your login: ");
        char[] password = c.readPassword("Enter your password: ");*/

    }

    /**
     * Shortcut method for printing a message to console.
     * Messages are printed as-is with no line breaks added.
     *
     * @param msg The String to be output to console.
     */
    private static void display(String msg) {
        System.out.println(msg);
        //System.console().writer().print(msg);
    }

    public static String getUsername() throws IOException {
        System.out.print("Enter user name: ");
        return (new BufferedReader(new InputStreamReader(System.in))).readLine();
    }

    public static String getPassword() throws IOException {
        System.out.print("Enter password: ");
        return (new BufferedReader(new InputStreamReader(System.in))).readLine();
    }

    public static String getCommand(String path) throws IOException {
        System.out.print(path + ">");
        return (new BufferedReader(new InputStreamReader(System.in))).readLine();
    }

}
