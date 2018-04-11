import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class GUI extends JFrame {

    //private String query;
    private String cache;
    private String search = "author";
    private JTextArea textArea;
    private JTextArea cacheText;
    private JList list;
    final DefaultListModel listModel = new DefaultListModel();
    private HashMap<String, String> urlMap = new HashMap<>();

    public GUI() {
        initUI();
    }

    private void initUI() {

        setTitle("Journal Search");
        setSize(500, 500);

        JPanel topPanel = new JPanel();

        JPanel bottomPanel = new JPanel();

        textArea = new JTextArea();
        cacheText = new JTextArea();

        JScrollPane scroll = new JScrollPane();
        scroll.setVisible(true);

        list = new JList(listModel);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {

                    String selection = (String) list.getSelectedValue();

                    try {

                        String url = urlMap.get(selection);

                        if (!url.endsWith(".xml")) {
                            url += ".xml";
                        }

                        cache = cacheText.getText();

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

                            System.out.println(url);

                            document = builder.parse(xmlUrl.openStream());
                            DOMSource source = new DOMSource(document);
                            StreamResult streamResult = new StreamResult(new File(cache + urlEncode));
                            transformer.transform(source, streamResult);
                        }

                        String authorURLEncoded = URLEncoder.encode(url, "UTF-8");

                        File authorCache = new File(cache + authorURLEncoded);

                        document = null;

                        transformerFactory = TransformerFactory.newInstance();
                        transformer = transformerFactory.newTransformer();

                        factory = DocumentBuilderFactory.newInstance();

                        builder = factory.newDocumentBuilder();

                        if (authorCache.exists()) {

                            document = builder.parse(authorCache);

                        } else {

                            URL newURL = new URL(url);
                            DOMSource source = new DOMSource(document);
                            document = builder.parse(newURL.openStream());
                            source = new DOMSource(document);
                            StreamResult streamResult = new StreamResult(new File(cache + authorURLEncoded));
                            transformer.transform(source, streamResult);
                        }

                        NodeList articles = document.getElementsByTagName("r");
                        NodeList coAuthors = document.getElementsByTagName("co");

                        listModel.removeAllElements();

                        if (articles.getLength() > 0) {
                            for (int i = 0; i > articles.getLength(); i++) {
                                Node temp = articles.item(i);
                                listModel.addElement(temp.getTextContent());
                            }
                        }
                    } catch (IOException q) {
                        q.printStackTrace();
                    } catch (TransformerException q) {
                        q.printStackTrace();
                    } catch (ParserConfigurationException q) {
                        q.printStackTrace();
                    } catch (SAXException q) {
                        q.printStackTrace();
                    }
                }
            }
        });

        JButton select = new JButton();
        select.setText("Select");
        select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent q) {
                update(textArea.getText());
            }
        });

        textArea.setSize(100,10);
        textArea.setColumns(10);
        cacheText.setSize(100,10);
        cacheText.setColumns(10);
        scroll.setSize(200,400);
        scroll.setBounds(20,50,200,400);

        topPanel.setSize(200,50);
        bottomPanel.setSize(200,50);
        bottomPanel.setBounds(0,80,200,50);

        topPanel.add(select);
        topPanel.add(textArea);
        bottomPanel.add(list);
        list.setVisible(true);
        //scroll.add(list);
        bottomPanel.add(cacheText);

        add(scroll);
        add(topPanel);
        add(bottomPanel);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void update(String query) {
        query = query.replaceAll(" ", "+");
        String url = "http://dblp.org/search/" + "author" + "/api?q=" + textArea.getText() + "&format=xml&h=40&c=0";

        try {

            cache = cacheText.getText();

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

            listModel.removeAllElements();
            urlMap.clear();

            for (int i = 0, n = 0; i < nodeList.getLength(); i++, n++) {
                Node node = nodeList.item(i);

                /*if (search.equals("author")) {

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

                    if (articles.getLength() > 0) {
                        for (int i = 0; i > articles.getLength(); i++) {
                            Node temp = articles.item(i);
                            listModel.add(temp.getTextContent());
                        }
                    }

                    System.out.println(node.getTextContent() + " - " + articles.getLength() + " publications with " + coAuthors.getLength() + " co-authors.");
*/
                //} else {
                /*if (search.equals("publ")) {
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
                    } else {*/
                listModel.addElement(node.getTextContent());
                urlMap.put(node.getTextContent(), nodeAuthor.item(n++).getTextContent());
                System.out.println(node.getTextContent());
            }
            //}
            //}
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
