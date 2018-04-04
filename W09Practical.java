import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class W09Practical {

    private static String search;
    private static String query;
    private static String cache;

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) { //THIS LOOKS DUMB
            if (args[i].startsWith("--")) {
                switch (args[i]) {
                    case "--search":
                        if (args[i++].equals("author") ||
                                args[i++].equals("venue")) {
                            search = args[i++];
                        } else if (args[i++].equals("publication")) {
                            search = "publ";
                        } else {
                            System.out.println("ERROR MESSAGE GOES HERE"); //FILL THIS IN
                        }
                        break;
                    case "--query":
                        query = args[i++];
                        break;
                    case "--cache":
                        cache = args[i++];
                        break;
                    default:
                        System.out.println("ERROR MESSAGE GOES HERE"); //FILL THIS IN
                }
            }
        }

        String url = "http://dblp.org/search/" + search + "/api?q=" + query + "&format=xml&h=30&c=0";

        try {
            URL XMLurl = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("Error: " + e);
        }

        String urlEncode = URLEncoder.encode(url);

        File xmlFile = new File(url);

        //DOM XML java !!!!!!!!!!!!!!!!!!
    }
}