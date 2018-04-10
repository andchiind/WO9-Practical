import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class W09Practical {

    private static String search;
    private static String query;
    private static String cache;
    private static boolean searchB = false;
    private static boolean queryB = false;
    private static boolean cacheB = false;
    private static File cacheFile;


    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) { //THIS LOOKS DUMB
            if (args[i].startsWith("--")) {

                String current = args[i];
                i++;
                //System.out.println(current);
                //System.out.println(args[i]);
                //String next = args[i];
                switch (current) {
                    case "--search":
                        searchB = true;
                        if (args.length <= i || args[i].startsWith("--") || args[i] == null || args[i].equals("")) {
                            System.out.println("Missing value for " + current + "\nMalformed command line arguments.");
                            System.exit(0);
                        } else {
                            if (args[i].equals("author") ||
                                    args[i].equals("venue")) {
                                search = args[i];
                            } else if (args[i].equals("publication")) {
                                search = "publ";
                            } else {
                                System.out.println("Invalid value for --search: " + args[i]); //FILL THIS IN
                                System.exit(0);
                            }
                        }
                        break;
                    case "--query":
                        queryB = true;
                        if (args.length <= i || args[i] == null || args[i].startsWith("--") || args[i].equals("") || args[i].equals(" ")) {
                            System.out.println("Missing value for " + current + "\nMalformed command line arguments.");
                            System.exit(0);
                        } else {
                            query = args[i];
                        }
                        break;
                    case "--cache":
                        cacheB = true;
                        if (args.length <= i) {
                            cacheFile = new File(args[i]);
                            if (!cacheFile.isDirectory()) {
                                System.out.println("Cache directory doesn't exist: noSuchDirectory");
                            }
                        }
                        if (args.length <= i || args[i] == null || args[i].startsWith("--") || args[i].equals("")) {
                            System.out.println("Missing value for " + current + "\nMalformed command line arguments.");
                            System.exit(0);
                        } else {
                            cache = args[i];
                        }
                        break;
                    default:
                        System.out.println("ERROR MESSAGE GOES HERE 2"); //FILL THIS IN
                        System.exit(0);
                }
            }
        }
        if (!queryB) {
            System.out.println("Missing value for --query" + "\nMalformed command line arguments.");
            System.exit(0);
        }
        if (!cacheB) {
            System.out.println("Missing value for --cache" + "\nMalformed command line arguments.");
            System.exit(0);
        }
        if (!searchB) {
            System.out.println("Missing value for -search" + "\nMalformed command line arguments.");
            System.exit(0);
        }

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        query = query.replaceAll(" ", "+");
        String url = "http://dblp.org/search/" + search + "/api?q=" + query + "&format=xml&h=40&c=0";
        //System.out.println(url);
        URL XMLurl;

        try {

            XMLurl = new URL(url);

            String urlEncode = URLEncoder.encode(url) + ".xml";

            //System.out.println(urlEncode);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(XMLurl.openStream());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(cache + "\\\\" + urlEncode));
            transformer.transform(source, streamResult);

            NodeList nodeList = null;
            NodeList nodeAuthor = null;

            switch (search) {
                case "publ":
                    nodeList = document.getElementsByTagName("info");
                    break;
                case "venue":
                    nodeList = document.getElementsByTagName("venue");
                    break;
                case "author":
                    nodeList = document.getElementsByTagName("author");
                    nodeAuthor = document.getElementsByTagName("url");
                    break;
                default:
                    return;
            }

            for (int i = 0, n = 0; i < nodeList.getLength(); i++, n++) {
                Node node = nodeList.item(i);

                if (search.equals("author")) {

                    Node node1 = nodeAuthor.item(n++);

                    String authorURL = node1.getTextContent() + ".xml";
                    URL newURL = new URL(authorURL);
                    Document authorDocument = builder.parse(newURL.openStream());

                    if (authorDocument == null)
                        System.out.println("fuckity fuck fuck");

                    //CHANGE URL, THE RESULTANT PAGE HAS A DIFFERENT URL

                    NodeList articles = authorDocument.getElementsByTagName("r");
                    NodeList coAuthors = authorDocument.getElementsByTagName("co");

                    Node art = articles.item(0);

                    System.out.println(node.getTextContent() + " - " + articles.getLength() + " publications with " + coAuthors.getLength() + " co-authors.");

                } else {
                    if (search.equals("publ")) {
                        Node publication = nodeList.item(i).getFirstChild();
                        NodeList authorList = publication.getChildNodes();
                        int nAuthors = 0;
                        if (authorList.item(0).getNodeName().equals("author")) {
                            nAuthors = authorList.getLength();
                        }
                        System.out.println(node.getFirstChild().getNextSibling().getTextContent() + " (number of authors: " + nAuthors + ")");
                    } else {
                        System.out.println(node.getTextContent());
                    }
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
        } catch (TransformerConfigurationException e) {
            System.out.println("Error: " + e);
        } catch (TransformerException e) {
            System.out.println("Error: " + e);
        }
    }
}