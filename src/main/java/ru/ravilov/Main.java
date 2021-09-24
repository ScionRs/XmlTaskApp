package ru.ravilov;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Главный класс
 */
public class Main {
    public final static Logger log = Logger.getLogger(Main.class);
    public static void main(String[] args) {
        long start =  System.currentTimeMillis();
        Main.log.info("Старт программы");
        ServiceLogic baseClass = null;
        long sum = 0;
        try {
            baseClass = new ServiceLogic();
            baseClass.setNumber(10);

            baseClass.insertData();
            baseClass.createXmlOne();
            Path path = baseClass.createXmlTwo();
            sum = baseClass.parseXml(path);
            if (baseClass.getConnection() != null)
                baseClass.getConnection().close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        long time = (end - start) / 1000;
        System.out.println("Арифметическая сумма всех значений: " + sum );
        Main.log.info("Затраченное время на работу приложения: " + time);
    }
}
