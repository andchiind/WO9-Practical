import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

                String current = args[i];
                i++;
                String next = args[i];
                switch (current) {
                    case "--search":
                        if (next.equals("author") ||
                                next.equals("venue")) {
                            search = next;
                        } else if (next.equals("publication")) {
                            search = "publ";
                        } else {
                            System.out.println("ERROR MESSAGE GOES HERE 1"); //FILL THIS IN
                        }
                        break;
                    case "--query":
                        query = next;
                        break;
                    case "--cache":
                        cache = next;
                        break;
                    default:
                        System.out.println("ERROR MESSAGE GOES HERE 2"); //FILL THIS IN
                }
            }
        }

        query = query.replaceAll(" ", "");
        String url = "http://dblp.org/search/" + search + "/api?q=" + query + "&format=xml&h=30&c=0";

        URL XMLurl;

        try {
            XMLurl = new URL(url);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(XMLurl.openStream());

            NodeList nodeList = null;
            NodeList nodeAuthor = null;

            String urlEncode = URLEncoder.encode(url);

            switch (search) {
                case "publ": nodeList = document.getElementsByTagName("title");
                break;
                case "venue": nodeList = document.getElementsByTagName("venue");
                break;
                case "author": nodeList = document.getElementsByTagName("author");
                nodeAuthor = document.getElementsByTagName("url");
                break;
                default: return;
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                System.out.println(node.getTextContent());
                if (search.equals("author")) {
                    Node node1 = nodeAuthor.item(i);
                    String authorURL = node1.getTextContent() + ".xml";
                    Document authorDocument = builder.parse(XMLurl.openStream());
                    NodeList articles = authorDocument.getElementsByTagName("title");
                    NodeList coAuthors = authorDocument.getElementsByTagName("coauthors");

                    System.out.println(articles.getLength() + " number of publications");
                    System.out.println(coAuthors.getLength() + " number of co-Authors");

                }

            }

        } catch (MalformedURLException e) {
            System.out.println("Error: " + e);
        } catch (ParserConfigurationException e) {
            System.out.println("Error: " + e);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        } catch (SAXException e) {
            System.out.println("Error: " + e);
        }
    }
}