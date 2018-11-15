/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proactivedocumentsuggestion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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
public class ProactiveDocumentSuggestion {

    IndexSearcher searcher;
    IndexReader reader;
    String qryField;

    public ProactiveDocumentSuggestion(String indexPath, String qryString) throws IOException, Exception {
        File indexDir = new File(indexPath);
        reader = DirectoryReader.open(FSDirectory.open(indexDir));
        qryField = qryString;
        searcher = initSearcher(reader);
    }

    public void retrieveRelevantQueries(String fileName) throws FileNotFoundException, IOException, Exception {

        FileReader fr = new FileReader(new File(fileName));
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        String st[] = line.split("\t");
        line = br.readLine();
        int count = 0;
        while (line != null) {
            if (line.equals("######")) {
                continue;
            }
            st = line.split("\t");
            Query q = buildQuery(st[1]);
            String originalQuery = st[1];
            System.out.println("Original Query " + originalQuery);
             System.out.println("Original userId " + st[0]);
            TopDocs tdocs = searcher.search(q, 50);
            System.out.println(tdocs.totalHits);
            if(count == 1)
            for (int i = 0; i < tdocs.scoreDocs.length; i++) {
                int docId = tdocs.scoreDocs[i].doc;
                Document doc = reader.document(docId);
                String query = doc.get(qryField);
                System.out.println("Query " + query);
                System.out.println("userid " + doc.get("uid"));
            }
            line = br.readLine();
            count++;
            if(count == 2)
                break;
        }
    }

    IndexSearcher initSearcher(IndexReader reader) throws Exception {
        IndexSearcher searcher = new IndexSearcher(reader);
        float lambda = Float.parseFloat("0.6");
        searcher.setSimilarity(new LMJelinekMercerSimilarity(lambda));
        return searcher;
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

    Query buildQuery(String queryStr) throws Exception {
        BooleanQuery q = new BooleanQuery();
        Term thisTerm = null;
        Query tq = null;
        String[] queryWords = analyze(queryStr).split("\\s+");

        // search in title and content...
        for (String term : queryWords) {
            thisTerm = new Term(qryField, term);
            tq = new TermQuery(thisTerm);
            q.add(tq, BooleanClause.Occur.SHOULD);
        }
        return q;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        ProactiveDocumentSuggestion prd = new ProactiveDocumentSuggestion("/media/procheta/OS/AOLIndexTrain", "query");
        prd.retrieveRelevantQueries("/media/procheta/OS/testSetNew.txt");
    }

}
