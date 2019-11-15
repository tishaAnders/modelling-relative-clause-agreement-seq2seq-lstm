import java.util.Scanner;
import java.util.*;
import java.io.File;
import java.lang.Math;
import java.lang.Character;
/**
 * // Check rules again
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
 *
 *
 *
 *
 *
 * @author Tisha Anders
 * @version 1
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
            line_no++;
            no_of_tested_gens++;
            String line = scanner.nextLine();
            line = line.substring(0, line.length()-1);
            int judgement = BNFParser.BNFparse(line);
            results[line_no] = judgement;
            no_of_good_gens = no_of_good_gens + judgement; 
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
            line_no++;
            no_of_tested_gens++;
            String line = scanner.nextLine();
            line = line.substring(0, line.length()-1);
            int bnf_judgement = BNFParser.BNFparse(line);
            int agr_judgement = checkAgreement(line);
            results[line_no] = (bnf_judgement + agr_judgement)/2;
            if (bnf_judgement + agr_judgement ==2)
            {
                no_of_good_gens++;
            }
        }
        double percentage = no_of_good_gens * Math.pow(no_of_tested_gens, -1);
        return percentage*100;
    }
    }