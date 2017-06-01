package server.database;

import java.io.*;

/**
 * Created 24.05.17.
 */
public class FileParser {

    private static final String ordersPath = "/home/simon/Dropbox/STUDIA_WIET/SEMESTR_6/SR/AKKA/akka/AkkaLibrary/src/main/resources/orders.txt";
    private int foundPrice;  // found price container

    public boolean searchForTitle(String title, DBnumber type) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(type.getPath()))) {

            while ((line = br.readLine()) != null) {
                String[] lineTitle = line.split(",");
                if (lineTitle[0].equals(title)) {
                    foundPrice = Integer.parseInt(lineTitle[1]);
                    return true;
                }
            }
        } catch (IOException e) {e.printStackTrace();}
        return false;
    }

    public static synchronized boolean orderBook(String title) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ordersPath, true))) {
            FileParser searcher = new FileParser();
            if(searcher.searchForTitle(title, DBnumber.FIRST) || searcher.searchForTitle(title, DBnumber.SECOND) ) {
                bw.append("\n").append(title);
                return true;
            }
        } catch (IOException e) {e.printStackTrace();}
        return false;
    }

    public int getFoundPrice() {
        return foundPrice;
    }

    public enum DBnumber {
        FIRST ("/home/simon/Dropbox/STUDIA_WIET/SEMESTR_6/SR/AKKA/akka/AkkaLibrary/src/main/resources/books1.txt"),
        SECOND ("/home/simon/Dropbox/STUDIA_WIET/SEMESTR_6/SR/AKKA/akka/AkkaLibrary/src/main/resources/books2.txt");

        private String path;

        DBnumber(String s) {
            path = s;
        }

        public String getPath() {
            return path;
        }
    }




    public static void main(String[] args) {

        FileParser search = new FileParser();
        if( search.searchForTitle("OnlyHereBook", DBnumber.FIRST) )
            System.out.println(search.getFoundPrice());

        FileParser.orderBook("Blalala");

    }



}


