/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Procheta
 */
public class AOLIndex {

    IndexWriter writer;
    String IndexPath;
    String logFilePath;
    String vecFilePath;

    public AOLIndex(String propFilePath) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File(propFilePath)));
        IndexPath = prop.getProperty("indexPath");
        Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
        IndexWriterConfig iwcfg = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
        iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(FSDirectory.open(new File(IndexPath)), iwcfg);
        logFilePath = prop.getProperty("logFile");
        vecFilePath = prop.getProperty("vecFile");

    }

    public Document createLuceneDocument(String line, String vec) {

        String st[] = line.split("\t");
        Document doc = new Document();
        doc.add(new Field("uid", st[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("query", st[1], Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("sessId", st[2], Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("qryVec", vec, Field.Store.YES, Field.Index.NOT_ANALYZED));
        if (st.length > 3) {
            doc.add(new Field("rank", st[3], Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("clickURL", st[4], Field.Store.YES, Field.Index.NOT_ANALYZED));
        } else {
            doc.add(new Field("rank", "", Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("clickURL", "", Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
        return doc;
    }

    public void indexAll() throws FileNotFoundException, IOException {
        FileReader fr1 = new FileReader(new File(logFilePath));
        BufferedReader br1 = new BufferedReader(fr1);

        FileReader fr2 = new FileReader(new File(logFilePath));
        BufferedReader br2 = new BufferedReader(fr2);
        String line1 = br1.readLine();
        line1 = br1.readLine();
        int count = 0;
        String line2 = br2.readLine();
        while (line1 != null) {
            Document doc = createLuceneDocument(line1, line2);
            writer.addDocument(doc);
            line1 = br1.readLine();
            line2 = br2.readLine();
            if (count % 100000 == 0) {
                System.out.println("Processed Document " + count);
            }
            count++;
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {

        AOLIndex aoi = new AOLIndex("/home/procheta/NetBeansProjects/ProactiveDocumentSuggestion/src/Indexer/init.properties");
        aoi.indexAll();

    }

}
