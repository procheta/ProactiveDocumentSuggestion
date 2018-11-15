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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author procheta
 */
public class RetrieveQueries {

    String testLog;
    String indexPath;
    IndexReader reader;
    IndexSearcher searcher;
    int numQueries;
    String weightedQueryPath;

    public RetrieveQueries(String propFileName) throws FileNotFoundException, IOException, Exception {
        Properties prop = new Properties();
        prop.load(new FileReader(new File(propFileName)));

        testLog = prop.getProperty("testLog");
        indexPath = prop.getProperty("index");
        File indexDir = new File(indexPath);
        reader = DirectoryReader.open(FSDirectory.open(indexDir));
        searcher = initSearcher(reader);
        numQueries = Integer.parseInt(prop.getProperty("numQ"));
        weightedQueryPath = prop.getProperty("finalQuery");
    }

    public void readTestQueries() throws FileNotFoundException, IOException, Exception {

        FileReader fr = new FileReader(new File(testLog));
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();

        int count = 0;
        
        FileWriter fw = new FileWriter(new File(weightedQueryPath));
        BufferedWriter bw = new BufferedWriter(fw);

        while (line != null) {
            if (!line.startsWith("###")) {
                String st[] = line.split("\t");
                ArrayList<String> retrievedQueries = getSimilarQueries(st[1]);
                HashMap<String, Double> wMap = getDistribution(retrievedQueries);
                count++;
                Iterator it = wMap.keySet().iterator();
                
                while(it.hasNext()){
                    String key = (String) it.next();
                    bw.write(key+":"+String.valueOf(wMap.get(key))+" ");                
                }
                bw.newLine();
                /*if(count == 2)
                    break;*/
            }else{
                bw.write("###");
                bw.newLine();
            
            }
            line = br.readLine();
        }
        bw.close();
    }

    IndexSearcher initSearcher(IndexReader reader) throws Exception {
        IndexSearcher searcher = new IndexSearcher(reader);
        float lambda = (float) 0.6;
        searcher.setSimilarity(new LMJelinekMercerSimilarity(lambda));
        return searcher;
    }

    Query buildQuery(String queryStr) throws Exception {
        BooleanQuery q = new BooleanQuery();
        Term thisTerm = null;
        Query tq = null;
        String[] queryWords = queryStr.split("\\s+");

        for (String term : queryWords) {
            thisTerm = new Term("query", term);
            tq = new TermQuery(thisTerm);
            q.add(tq, BooleanClause.Occur.SHOULD);
        }
        return q;
    }

    String analyze(String query) throws Exception {
        StringBuffer buff = new StringBuffer();
        Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_4_9);
        TokenStream stream = analyzer.tokenStream("dummy", new StringReader(query));
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            String term = termAtt.toString();
            term = term.toLowerCase();
            buff.append(term).append(" ");
        }
        stream.end();
        stream.close();
        return buff.toString();
    }

    public HashMap<String, Double> getDistribution(ArrayList<String> queries) {

        HashMap<String, Integer> wordDistribution = new HashMap<>();
        double maxCount = 0;
        for (String query : queries) {
            String st[] = query.split("\\s+");
            for (String s : st) {
                if (wordDistribution.containsKey(s)) {
                    wordDistribution.put(s, wordDistribution.get(s) + 1);
                } else {
                    wordDistribution.put(s, 1);
                }
                if (maxCount < wordDistribution.get(s)) {
                    maxCount = wordDistribution.get(s);
                }
            }
        }
        HashMap<String, Double> normalizedWordMap = new HashMap<>();
        
        Iterator it = wordDistribution.keySet().iterator();
        while(it.hasNext()){
            String key = (String) it.next();
             normalizedWordMap.put(key, wordDistribution.get(key)/maxCount);        
        }
        return normalizedWordMap;
    }

    public ArrayList<String> getSimilarQueries(String qryText) throws IOException, Exception {
        Query q = buildQuery(analyze(qryText));
        TopDocs tdocs = searcher.search(q, numQueries);

        ArrayList<String> qryList = new ArrayList<>();

        for (int i = 0; i < tdocs.scoreDocs.length; i++) {
            Document doc = reader.document(tdocs.scoreDocs[i].doc);
            String query = doc.get("query");
            qryList.add(query);
        }
         System.out.println(qryList.size());
         if(tdocs.scoreDocs.length == 0)
         { System.out.println(qryText);  System.out.println("hhhhhhhhhhhhhhh");}
        return qryList;
    }

    public static void main(String[] args) throws Exception {
        RetrieveQueries rq = new RetrieveQueries("/home/procheta/NetBeansProjects/ProactiveDocumentSuggestion/src/BaselineRetriever/init.properties");
        rq.readTestQueries();

    }

}
