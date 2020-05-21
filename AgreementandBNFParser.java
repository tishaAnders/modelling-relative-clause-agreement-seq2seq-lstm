import java.util.Scanner;
import java.util.*;
import java.io.File;
import java.lang.Math;
import java.lang.Character;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Random;
import java.io.PrintStream;
import java.io.FileNotFoundException;
/**
 * A class that tests word-order (using the BNFParser class) and Agreement, so that it tests the overall grammaticality of the sentences in a file.  
 *s -> u x y 
 *u -> d p n | d n | p n | n
 *x -> q | q q | q q q | q q q q
 *q -> c r v p n p | c r v p n | c r v n | c r v n p
 *y -> z v d p n | z v d n | z v p n | z v n
 *c -> C
 *d -> D | D D | D D D
 *p -> P | P P
 *n -> N g | N N g | N N N g | N N N N g | N N N N N g …. until 9 Ns
 *g -> A S | A T | B S | B T
 *v -> V S | V V S | V V V S | V V V V S | V V V V V S | V T | V V T | V V V T | V V V V T | V V V V V T
 *z -> Z
 *r -> R A | R B 
 *
 *
 * @author Tisha Anders
 */
public class AgreementandBNFParser
{
    /**
     * This function checks a string for agreement patterns. It extracts all As,Bs,Ss & Ts from a given string and turns them into a substring.
     * The last two letters in the substring will be discarded since the last noun in the sentence does not impose agreement on anything.
     * Similarly for the penultimate noun.
     * It accepts combinations ASAS, ATAT, BSBS, BTBT, ASASBTBT, ... . It will reject any combination that is not of the form XYXY+UWUW+QPQP+...+Y.
     *
     * The first two letters show what kind of agreement the noun has imposed on the relative pronoun & the verb, respectively. 
     * The third letter shows what agreement the relative pronoun thinks has been imposed on it.
     * The fourth is similar, but for the verb. 
     * the last letter in the entire string is the noun in the intro agreeing with the verb in the outro over a long distance
     * @param s The string whose Agreement is to be tested
     * @returns 1 if all Agreement is correct, 0 otherwise
     */
    public static int checkAgreement(String s)
    {
        Scanner sc = new Scanner(s);
        //replace all characters that are not A,B,S,T
        s = s.replaceAll("N", "");
        s = s.replaceAll("P", "");
        s = s.replaceAll("D", "");
        s = s.replaceAll("V", "");
        s = s.replaceAll("C", "");
        s = s.replaceAll("R", "");
        s = s.replaceAll("Z", "");
        s = s.replaceAll(" ", "");
        s = s.substring(0, s.length()-2);//remove ending of last noun

        // remove ending of penultimate noun
        s = s.substring(0, s.length()-3) + s.charAt(s.length()-1); 
        //check for pattern XYXY
        int no_of_xyxy = (s.length()-1)/4;

        Boolean[] xyxy_array = new Boolean[no_of_xyxy];
        Boolean xyxy = true;
        Boolean long_dist_NV = false;

        for (int i = 0; i < no_of_xyxy; i++)
        {
            xyxy_array[i] = false;
            String s1 = s.substring(4*i, 4*(i+1));
            if (s1.equals("ASAS") || s1.equals("ATAT") || s1.equals("BSBS") || s1.equals("BTBT"))
            {
                xyxy_array[i] = true;
            }
            if (xyxy_array[i] == false)
            {
                xyxy = false;
            }
        }

        char subject_s = s.charAt(1);
        char last_verb_s = s.charAt(s.length()-1);
        if (subject_s == last_verb_s)
        {
            long_dist_NV = true;
        }

        if (xyxy && long_dist_NV)
        {
            return 1;
        }
        else
        {
            return 0;
        }

    }

    /**
     * This function takes in a file and tests whether the order of tokens is possible, using the above created BNF parser
     * @param filename The file to be examined
     * @return percentage of sentences with correct word order
     * 
     */ 
    public static double testBNFFromFile(String filename) throws java.io.FileNotFoundException
    {
        Scanner scanner = new Scanner(new File(filename + ".txt"));
        int no_of_good_gens = 0;
        int no_of_tested_gens = 0;
        int[] results = new int[10000];
        int line_no = 0;
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            line = line.substring(0, line.length()-1);
            int judgement = BNFParser.BNFparse(line);
            results[line_no] = judgement;
            no_of_good_gens = no_of_good_gens + judgement; 
            line_no++;
            no_of_tested_gens++;
        }
        double percentage = no_of_good_gens * Math.pow(no_of_tested_gens, -1);
        return percentage*100;
    }

    /**
     * This function takes in a file and tests whether the sentences are possible (word-order & agreement)
     * @param filename The file to be examined
     * @return percentage of correct sentences
     * 
     */ 
    public static double testFromFile(String filename) throws java.io.FileNotFoundException
    {
        Scanner scanner = new Scanner(new File(filename + ".txt"));
        int no_of_good_gens = 0;
        int no_of_tested_gens = 0;
        int[] results = new int[10000];
        int line_no = 0;
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            line = line.substring(0, line.length()-1);
            int bnf_judgement = BNFParser.BNFparse(line);
            int agr_judgement = checkAgreement(line);
            results[line_no] = (bnf_judgement + agr_judgement)/2;
            if (bnf_judgement + agr_judgement == 2)
            {
                no_of_good_gens++;
            }
            line_no++;
            no_of_tested_gens++;
        }

        double percentage = no_of_good_gens * Math.pow(no_of_tested_gens, -1);
        return percentage*100;
    } 

    /**
     * This method lists all generated sentences from a file together with their judgement (word-order & agreement).
     * judgementArray might be useful in analysing what the LSTM struggles with in particular.
     * @param filename The file to be examined
     * @return judgement array (sentence in first column, judgement in second column)
     * 
     */ 
    public static String[][] judgementArray(String filename) throws java.io.FileNotFoundException
    {
        Scanner scanner_count = new Scanner(new File(filename + ".txt"));
        int no_of_good_gens = 0;
        int no_of_tested_gens = 0;
        int line_cnt = 0;
        while (scanner_count.hasNextLine())
        {
            line_cnt++;
            scanner_count.nextLine();
        }
        scanner_count.close();
        Scanner scanner = new Scanner(new File(filename + ".txt"));
        String[][] judgements = new String[2][line_cnt];
        int line_no = 0;
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            judgements[0][line_no] = line;
            line = line.substring(0, line.length()-1);
            int bnf_judgement = BNFParser.BNFparse(line);
            int agr_judgement = checkAgreement(line);
            if (bnf_judgement + agr_judgement ==2)
            {
                judgements[1][line_no] = "Good";
            }
            else
            {
                judgements[1][line_no] = "Bad";
            }
            line_no++;
            no_of_tested_gens++;
        }
        scanner.close();
        return judgements;
    }

    /**
     * This method lists all bad sentences from a file.
     * badGenerations might be useful in analysing what the LSTM struggles with in particular.
     * @param filename The file to be examined
     * @return judgement array (sentence in first column, judgement in second column)
     * 
     */ 

    public static int badSentencesToFile(String source_file, String goal_file) throws java.io.FileNotFoundException
    {
        String[][] all_judgements = judgementArray(source_file);
        int bad_count = 0;
        File myFile = new File("BAD" + goal_file + ".txt");
        PrintStream p = new PrintStream(myFile);
        for (int i = 0; i < all_judgements[1].length; i++)
        {
            String current_sentence = all_judgements[1][i];
            if (current_sentence.equals("Bad"))
            {
                p.println(all_judgements[0][i]);
                bad_count++;
            }
        }

        p.close();
        return bad_count;
    }

    public static String[] badSentences(String filename) throws java.io.FileNotFoundException
    {
        String[][] all_judgements = judgementArray(filename);
        String[] bad_sentences = new String[500];
        int bad_count = 0;
        for (int i = 0; i < all_judgements.length; i++)
        {
            String current_sentence = all_judgements[1][i];
            if (current_sentence.equals("Bad"))
            {
                bad_sentences[bad_count] = all_judgements[0][i];
                bad_count++;
            }
        }
        return bad_sentences;
    }

}
