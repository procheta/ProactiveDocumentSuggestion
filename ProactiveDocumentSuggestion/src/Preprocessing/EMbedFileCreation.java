/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 *
 * @author Procheta
 */
public class EMbedFileCreation {

    ArrayList<String> stopWords;

    public static String stem(String word) {
        PorterStemmer porterStemmer = new PorterStemmer();
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }

    public EMbedFileCreation(String stopFile) throws IOException {
        stopWords = getStopWords(stopFile);
    }

    public ArrayList<String> getStopWords(String StopFile) throws FileNotFoundException, IOException {

        FileReader fr = new FileReader(new File(StopFile));
        BufferedReader br = new BufferedReader(fr);

        ArrayList<String> stopWords = new ArrayList<>();
        String line = br.readLine();

        while (line != null) {
            stopWords.add(line);
            line = br.readLine();
        }
        return stopWords;
    }

    public String processQuery(String query) {
        query = query.replaceAll(".com", "");
        query = query.replaceAll(".gov", "");
        query = query.replaceAll(".net", "");
        query = query.replaceAll(".org", "");
        query = query.replaceAll(".in", "");
        query = query.replaceAll("www", "");
        query = query.replaceAll(";", "");
        query = query.replaceAll("-", " ");
        query = query.replaceAll("'", "");
        query = query.replaceAll("\\.", " ");
        query = query.replaceAll("<==", "");
        query = query.replaceAll("http", "");

        String finalContent = "";

        String st[] = query.split("\\s+");
        for (String s : st) {
            if (!stopWords.contains(s) && s.length() >= 2 && s.length() < 30) {
                try {
                    double i = Double.parseDouble(s);
                } catch (Exception e) {
                    finalContent += stem(s) + " ";
                }
            }
        }
        return finalContent;
    }

    public void createInputForTemporalEmbedding(String logFile, String writeFile) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(new File(logFile));
        BufferedReader br = new BufferedReader(fr);

        FileWriter fw = new FileWriter(new File(writeFile));
        BufferedWriter bw = new BufferedWriter(fw);

        String line = br.readLine();
        line = br.readLine();
        String st[] = line.split("\t");
        String prevSessId = st[2];

        while (line != null) {
            st = line.split("\t");
            if (!st[2].equals(prevSessId)) {
                bw.newLine();
                prevSessId = st[2];
            }
            bw.write(processQuery(st[1]) + " ");
            line = br.readLine();
        }
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        EMbedFileCreation embc = new EMbedFileCreation("C:\\Users\\Procheta\\Downloads\\FeedbackAnnotationSystem(1)\\FeedbackAnnotationSystem/stop.txt");
        embc.createInputForTemporalEmbedding("C:\\Users\\Procheta\\Downloads/logTrain.txt", "C:\\Users\\Procheta\\Documents/TemporalEmbedInput.txt");
    }

}
