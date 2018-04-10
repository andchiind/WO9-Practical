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


    public static void main(String[] args) {

        String next = null;
        boolean fault = false;
        String search = null;
        String query = null;
        String cache = null;
        boolean searchB = false;
        boolean queryB = false;
        boolean cacheB = false;
        File cacheFile = null;
        boolean searchInvalid = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {

                String current = args[i];
                if (i + 1 < args.length) {
                    next = args[i + 1];
                }
                if (!next.startsWith("--")) {
                    i++;
                }

                switch (current) {
                    case "--search":
                        searchB = true;
                        if (args.length <= i
                                || next.startsWith("--")
                                || next == null
                                || next.equals("")) {
                            searchB = false;
                        } else {
                            if (next.equals("author")
                                    || args[i].equals("venue")) {
                                search = next;
                            } else if (next.equals("publication")) {
                                search = "publ";
                            } else {
                                searchInvalid = true;
                                search = next;
                            }
                        }
                        break;
                    case "--query":
                        queryB = true;
                        if (args.length <= i
                                || next == null
                                || next.startsWith("--")
                                || next.equals("")
                                || next.equals(" ")) {
                            queryB = false;
                        } else {
                            query = next;
                        }
                        break;
                    case "--cache":
                        cacheB = true;
                        cacheFile = new File(next);
                        cache = next;
                        if (args.length <= i
                                || next == null
                                || next.startsWith("--")
                                || next.equals("")) {
                            cacheB = false;
                        } else {
                            cacheFile = new File(next);
                        }
                        break;
                    default:
                        System.out.println("ERROR MESSAGE GOES HERE 2"); //FILL THIS IN
                        System.exit(0);
                }
            }
        }

        if (searchInvalid) {
            fault = true;
            System.out.println("Invalid value for --search: " + search + "\nMalformed command line arguments.");
            System.exit(0);
        }

        if (!cacheFile.isDirectory() || !cacheFile.exists()) {
            fault = true;
            System.out.println("Cache directory doesn't exist: " + cache);
            System.exit(0);
        }

        if (!queryB) {
            fault = true;
            System.out.println("Missing value for --query");
        }
        if (!cacheB) {
            fault = true;
            System.out.println("Missing value for --cache");
        }
        if (!searchB) {
            fault = true;
            System.out.println("Missing value for --search");
        }
        if (fault) {
            System.out.println("Malformed command line arguments.");
            System.exit(0);
        }

        query = query.replaceAll(" ", "+");
        String url = "http://dblp.org/search/" + search + "/api?q=" + query + "&format=xml&h=40&c=0";
        URL xmlUrl;

        try {

            xmlUrl = new URL(url);

            String urlEncode = URLEncoder.encode(url, "UTF-8") + ".xml";

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlUrl.openStream());

            DOMSource source = new DOMSource(document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamResult streamResult = new StreamResult(new File(cache + "/" + urlEncode));

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

                    String authorURLencoded = URLEncoder.encode(authorURL, "UTF-8");

                    URL newURL = new URL(authorURL);
                    Document authorDocument = builder.parse(newURL.openStream());

                    DOMSource authorSource = new DOMSource(document);

                    StreamResult authorStreamResult = new StreamResult(new File(cache + "/" + authorURLencoded));

                    transformer.transform(authorSource, authorStreamResult);

                    NodeList articles = authorDocument.getElementsByTagName("r");
                    NodeList coAuthors = authorDocument.getElementsByTagName("co");

                    System.out.println(node.getTextContent() + " - " + articles.getLength() + " publications with " + coAuthors.getLength() + " co-authors.");

                } else {
                    if (search.equals("publ")) {
                        String title = null;

                        Node publication = nodeList.item(i).getFirstChild();
                        NodeList authorList = publication.getChildNodes();

                        int nAuthors = 0;
                        if (authorList.item(0).getNodeName().equals("author")) {
                            nAuthors = authorList.getLength();
                        }

                        NodeList titleList = document.getElementsByTagName("title");
                        title = titleList.item(i).getTextContent();

                        System.out.println(title + " (number of authors: " + nAuthors + ")");
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
