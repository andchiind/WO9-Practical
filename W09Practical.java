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

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //query = query.replaceAll(" ", "");
        String url = "http://dblp.org/search/" + search + "/api?q=" + query + "&format=xml&h=30&c=0";
        URL XMLurl;

        try {

            XMLurl = new URL(url);

            String urlEncode = URLEncoder.encode(url);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(XMLurl.openStream());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            //StreamResult streamResult =  new StreamResult(new File(cache + "\\" + urlEncode));
            //transformer.transform(source, streamResult);

            NodeList nodeList = null;
            NodeList nodeAuthor = null;

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

            for (int i = 0, n = 0; i < nodeList.getLength(); i++, n++) {
                Node node = nodeList.item(i);

                if (search.equals("author")) {

                    Node node1 = nodeAuthor.item(n++);

                    String authorURL = node1.getTextContent() + ".xml";
                    URL newURL = new URL(authorURL);
                    Document authorDocument = builder.parse(newURL.openStream());

                    if (authorDocument == null) System.out.println("fuckity fuck fuck");

                    //CHANGE URL, THE RESULTANT PAGE HAS A DIFFERENT URL

                    NodeList articles = authorDocument.getElementsByTagName("r");
                    NodeList coAuthors = authorDocument.getElementsByTagName("co");

                    Node art = articles.item(0);

                    System.out.print(node.getTextContent() + " - " + articles.getLength() + " publications with " + coAuthors.getLength() + " co-authors. \n");

                } else {
                    System.out.println(node.getTextContent());
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