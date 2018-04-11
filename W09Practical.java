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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;

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

        try {

            URL xmlUrl = new URL(url);

            String urlEncode = URLEncoder.encode(url, "UTF-8") + ".xml";

            File checkCache = new File(cache + urlEncode);

            Document document = null;
            Transformer transformer = null;
            DocumentBuilder builder = null;

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            builder = factory.newDocumentBuilder();

            if (checkCache.exists()) {

                System.out.println("File found in cache. \n");
                document = builder.parse(checkCache);

            } else {

                System.out.println("File not found in cache, creating new file.");

                document = builder.parse(xmlUrl.openStream());
                DOMSource source = new DOMSource(document);
                StreamResult streamResult = new StreamResult(new File(cache + urlEncode));
                transformer.transform(source, streamResult);
            }

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

                    String authorURLEncoded = URLEncoder.encode(authorURL, "UTF-8");

                    File authorCache = new File(cache + authorURLEncoded);

                    document = null;

                    transformerFactory = TransformerFactory.newInstance();
                    transformer = transformerFactory.newTransformer();

                    factory = DocumentBuilderFactory.newInstance();

                    builder = factory.newDocumentBuilder();

                    if (authorCache.exists()) {

                        document = builder.parse(authorCache);

                    } else {

                        URL newURL = new URL(authorURL);
                        DOMSource source = new DOMSource(document);
                        document = builder.parse(newURL.openStream());
                        source = new DOMSource(document);
                        StreamResult streamResult = new StreamResult(new File(cache + authorURLEncoded));
                        transformer.transform(source, streamResult);
                    }

                    NodeList articles = document.getElementsByTagName("r");
                    NodeList coAuthors = document.getElementsByTagName("co");

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}