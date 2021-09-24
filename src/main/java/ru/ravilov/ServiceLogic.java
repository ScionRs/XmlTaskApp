package ru.ravilov;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Класс с основной бизнес-логикой
 */
public class ServiceLogic {

    //инициализируем переменные
    private final String XML_ONE = "1.xml";
    private final String XML_TWO = "2.xml";
    private final String XSL_SCHEME_FILE_NAME = "/scheme.xsl";
    private Integer number;
    private Connection connection;

    public Integer getNumber() {
        return number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public Connection getConnection() {
        return connection;
    }

    /**
     * Очистка таблицы
     * @throws SQLException
     */
    private void clearTable() throws SQLException {
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("TRUNCATE TABLE test");
            Main.log.info("Очистка таблицы...");
        }
    }

    public ServiceLogic() throws IOException, SQLException {
        Properties properties = new Properties();
        try (final InputStream stream = Main.class.getClassLoader().getResourceAsStream("database.properties")) {
            properties.load(stream);
        }

        String url = properties.getProperty("database.url");
        String username = properties.getProperty("database.username");
        String password = properties.getProperty("database.password");
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            clearTable();
        } finally {
            if(statement != null)
                statement.close();
        }
    }

    /**
     * Добавляем в базу случайные значения
     * @throws SQLException
     */
    public void insertData() throws SQLException {
        String sql = "INSERT INTO test (field) values(?)";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 1; i <= number; i++) {
                int randomNumber = (int) (10 + Math.random() * 25);
                ps.setInt(1, randomNumber);
                ps.addBatch();
            }
            ps.executeBatch();
            Main.log.info("Добавление случайных записей");
        }
    }

    /**
     * Выгрузка файла из базы данных
     * и преобразование в xml
     * @return
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     * @throws SQLException
     */
    public String createXmlOne() throws ParserConfigurationException, TransformerException, IOException, SQLException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("entries");

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT field FROM test");
        while (resultSet.next()) {
            Element entry = doc.createElement("entry");
            Element field = doc.createElement("field");
            field.setTextContent(resultSet.getString(1));
            entry.appendChild(field);
            root.appendChild(entry);
        }
        resultSet.close();
        statement.close();
        doc.appendChild(root);
        String res = documentToString(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(root), new StreamResult(writer));
        writeFile(res, XML_ONE);
        Main.log.info("Запись первого файла...");
        return res;
    }

    private String documentToString(Node root) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(root), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    private  void writeFile(String str, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        try(BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(str);
        }
    }

    /**
     * Трансформируем файл 1.xml в 2.xml
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    public Path createXmlTwo() throws TransformerException, IOException {
        Path path1 = Paths.get(XML_ONE);
        Path path2 = Paths.get(XML_TWO);
        InputStream inputXSL = getClass().getResourceAsStream(XSL_SCHEME_FILE_NAME);

        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xslStream = new StreamSource(inputXSL);
        Transformer transformer = factory.newTransformer(xslStream);

        StreamSource in = new StreamSource(path1.toFile());
        StreamResult out = new StreamResult(path2.toFile());
        transformer.transform(in, out);
        Main.log.info("Запись второго файла...");
        return path2;
    }

    /**
     * Подсчет общей суммы
     * @param path
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public long parseXml(Path path) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document doc = db.parse(path.toFile());

        NodeList list = doc.getElementsByTagName("entry");
        long sum = 0;

        for(int i = 0; i < list.getLength(); i++) {
            String fieldValue = list.item(i).getAttributes().getNamedItem("field").getNodeValue();
            sum += Integer.parseInt(fieldValue);
        }
        return sum;
    }
}
