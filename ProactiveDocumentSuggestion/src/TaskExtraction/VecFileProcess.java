/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TaskExtraction;

/**
 *
 * @author procheta
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Procheta
 */
public class VecFileProcess {

    public void process() throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(new File("/media/procheta/OS/AolQueryVecNew.txt"));
        BufferedReader br = new BufferedReader(fr);

        FileWriter fw = new FileWriter(new File("/home/procheta/test.txt"));
        BufferedWriter bw = new BufferedWriter(fw);

        String line = br.readLine();
        int count = 0;
        while (line != null) {
            if (!line.equals("")) {
                bw.write(line);
            } else {
                for (int i = 0; i < 200; i++) {
                    bw.write("0 ");
                }
            }
            bw.newLine();
            line = br.readLine();

        }
        bw.close();
    }

    public void createTestFile(String fileName, String writeFile) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(new File(fileName));
        BufferedReader br = new BufferedReader(fr);

        FileWriter fw = new FileWriter(new File(writeFile));
        BufferedWriter bw = new BufferedWriter(fw);

        String line = br.readLine();
        String st[] = line.split("\t");
        String prevSessId = st[2];
        int sessCount = 0;
        ArrayList<String> sess = new ArrayList<>();
        int totalCount = 0;
        String prevQuery = st[1];
        while (line != null) {
            line = line.replaceAll("\\.", " ");
            st = line.split("\t");
            String words[] = st[1].split(" ");
            if (prevSessId.equals(st[2])) {
                if (words.length > 3) {
                    if (!st[1].equals(prevQuery)) {
                        sessCount++;
                        sess.add(line);
                    } else {
                        //System.out.println("jjjj");
                    }
                }
            } else {
                if (sessCount >= 20) {
                    bw.write("######");
                    bw.newLine();
                    totalCount++;
                    String prevuserId = sess.get(0).split("\t")[0];
                    String prevLine = "";
                    for (String s : sess) {
                        if (!prevLine.equals(s.split("\t")[1])) {
                            if (prevuserId.equals(s.split("\t")[0])) {
                                System.out.println(s);
                                bw.write(s);
                                bw.newLine();
                            } else {
                                bw.write("######");
                                bw.newLine();
                                bw.write(s);
                                bw.newLine();
                                prevuserId = s.split("\t")[0];
                            }
                        }
                        prevLine = s.split("\t")[1];
                    }
                }
                sessCount = 1;
                sess = new ArrayList<>();
                sess.add(line);
            }
            if (words.length <= 1 && !(st[1].endsWith("com") || st[1].endsWith("com") || st[1].endsWith("gov") || st[1].endsWith("org") || st[1].endsWith("edu") || st[1].endsWith("net"))) {
                //System.out.println(st[1]);
            }

            prevSessId = st[2];
            prevQuery = st[1];
            line = br.readLine();
        }
        System.out.println(totalCount);
        bw.close();
    }

    public static void main(String[] args) throws IOException {

        VecFileProcess vfp = new VecFileProcess();
        // vfp.process();
        vfp.createTestFile("/media/procheta/OS/testSet.txt", "/media/procheta/OS/testSetNew.txt");
    }

}
