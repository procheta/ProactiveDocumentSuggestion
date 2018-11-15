/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BaselineRetriever;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author procheta
 */
class QueryTerm implements Comparable<QueryTerm> {

    String term;
    double weight;

    public QueryTerm(String term, double weight) {
        this.term = term;
        this.weight = weight;
    }

    @Override
    public int compareTo(QueryTerm o) {
        return (int) (weight - o.weight);
    }

}

public class CluewebRetriever {

    String queryFile;
    int topK;
    IndexSearcher searcher;
    String resultDocFile;
    IndexReader[] readers;
    IndexReader multiReader;
    Properties prop;

    public CluewebRetriever(String propFileName) throws IOException, Exception {

        prop = new Properties();
        prop.load(new FileReader(new File(propFileName)));
        String indexType = prop.getProperty("indexType");
        int numIndex = Integer.parseInt(prop.getProperty("indexNum"));
        initIndexes(numIndex, indexType);
        queryFile = prop.getProperty("query");
        topK = Integer.parseInt(prop.getProperty("topK"));
        resultDocFile = prop.getProperty("resultFile");
        searcher = initSearcher();
    }

    
     void initIndexes(int numIndexes, String indexType) throws Exception {
         
        readers = new IndexReader[numIndexes];
        System.out.println("#readers = " + readers.length);

        if (numIndexes > 1) {
            for (int i = 0; i < numIndexes; i++) {
                String indexDirPath = prop.getProperty("subindex." + i);
                System.out.println("Initializing index " + i + " from " + indexDirPath);
                File indexDir = new File(indexDirPath);
                readers[i] = DirectoryReader.open(FSDirectory.open(indexDir));
                System.out.println("#docs in index " + i + ": " + readers[i].numDocs());
            }
            if (!indexType.equals("single")) {
                // baaler camilla
                System.out.println("Initializing multi-reader");
                multiReader = new MultiReader(readers, true);
                System.out.println("#docs in index: " + multiReader.numDocs());
            }
        } else {
            String indexDirPath = prop.getProperty("index");
            File indexDir = new File(indexDirPath);
            readers[0] = DirectoryReader.open(FSDirectory.open(indexDir));
        }
    }
    
    IndexSearcher initSearcher() throws Exception {
        IndexSearcher searcher = new IndexSearcher(multiReader);
        float lambda = (float) 0.6;
        searcher.setSimilarity(new LMJelinekMercerSimilarity(lambda));
        return searcher;
    }

    public void retrieveCluewebDocs() throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(new File(queryFile));
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();

        FileWriter fw = new FileWriter(new File(resultDocFile));
        BufferedWriter bw = new BufferedWriter(fw);

        while (line != null) {
            if (!line.startsWith("###")) {
                if(line.equals("Not Found"))
                {
                   bw.write("Not Found");                
                }
                Query q = buildQuery(line);
                TopDocs tdocs = searcher.search(q, 100);
                for (int i = 0; i < tdocs.scoreDocs.length; i++) {
                    bw.write(String.valueOf(tdocs.scoreDocs[i].doc) + ",");
                }
            } else {
                bw.write("###");
            }
            bw.newLine();
            line = br.readLine();
        }
        bw.close();
    }

    public Query buildQuery(String qryText) {
        String st[] = qryText.split(" ");
        ArrayList<QueryTerm> qTermList = new ArrayList<>();

        for (String s : st) {
            String words[] = s.split(":");
            QueryTerm qt = new QueryTerm(words[0], Double.parseDouble(words[1]));
            qTermList.add(qt);
        }
        Collections.sort(qTermList);

        BooleanQuery bq = new BooleanQuery();
        Term thisTerm = null;
        Query tq = null;
        if (topK > qTermList.size()) {
            topK = qTermList.size();
        }
        for (int i = 0; i < topK; i++) {
            thisTerm = new Term("words", qTermList.get(i).term);
            tq = new TermQuery(thisTerm);
            tq.setBoost((float) qTermList.get(i).weight);
            bq.add(tq, BooleanClause.Occur.SHOULD);
        }
        return bq;
    }
    
    public static void main(String[]args) throws Exception{
        CluewebRetriever clr = new CluewebRetriever(args[0]);
        clr.retrieveCluewebDocs();    
    }

}
