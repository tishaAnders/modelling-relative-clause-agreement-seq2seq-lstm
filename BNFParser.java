import java.util.Scanner;
import java.util.*;
import java.io.File;
import java.lang.Math;
/**
 * A class that tests the word-order of a string, but not whether the agreement is correct, since this kind of dependency is not captured in context-free grammars and thus not in the BNF.
 * Use to find out what percentage of generations (in a .txt file) have correct word-order.
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
 *Agreement has to be checked separately.
 *
 * @author Tisha Anders

 */
public class BNFParser
{
    /**
     * This function returns a matrix which stores all the rules of the grammar. 
     * The LHS of a formula can be found in the first entry of a column and the possibilities for replacement in the subsequent entries.
     * A column (what looks like a row here) corresponds to a rule. 
     * 
     * @return String[][] parsingmatrix
     * 
     */
    public static String[][] parsingmatrix()
    {
        String[][] parsingmatrix =

            {
                {"s","uxy"},
                {"u","dpn","dn","pn","n"},
                {"x","qqqq","qqq","qq","q"},
                {"q","crvpnp","crvpn","crvnp","crvn"},
                {"y","zvdpn","zvdn","zvpn","zvn"},
                {"c","C"},
                {"d","DDD","DD","D"},
                {"p","PP","P"},
                {"n","NNNNNNNNNg","NNNNNNNNg","NNNNNNNg","NNNNNNg","NNNNNg","NNNNg","NNNg","NNg","Ng"},
                {"g","AS","AT","BS","BT"},
                {"v","VVVVVS","VVVVS","VVVS","VVS","VS","VVVVVT","VVVVT","VVVT","VVT","VT"},
                {"z","Z"},
                {"r","RA","RB"} };

        return parsingmatrix;
    }

    /**
     * This function takes in a string and tests its grammaticality.
     * @param s the string to be checked
     * @return 1 if the string was correct, 0 otherwise
     * 
     */
    public static int BNFparse(String s)
    {
        String[][] rulematrix = parsingmatrix();
        int no_of_rules = rulematrix.length; 
        for (int r = no_of_rules-1; r >= 0; r--)
        {
            Scanner sc = new Scanner(s);
            int no_of_options = rulematrix[r].length-1;
            for (int option = 1; option <= no_of_options; option++)
            {
                int first_rc_pos = s.indexOf("x");
                String current_term_str = rulematrix[r][option];
                String current_syn_unit = rulematrix[r][0]; 
                int starting_pos = s.indexOf(current_term_str);
                //replace in the following cases:
                // substring occurs in string, 
                // if we are about to replace the intro, the string we are replacing must be located before the relative clauses
                if (starting_pos != -1) 
                {
                    s = s.replaceAll(current_term_str, current_syn_unit);
                } 
            }
            sc.close();
        }
        if (s.equals("s"))
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
     * @return percentage of correct sentences
     * 
     */
    public static double testFromFile(String filename) throws java.io.FileNotFoundException
    {
        Scanner scanner = new Scanner(new File(filename + ".txt"));
        int no_of_good_gens = 0;
        int no_of_tested_gens = 0;
        while (scanner.hasNextLine())
        {
            no_of_tested_gens++;
            String line = scanner.nextLine();
            line = line.substring(0, line.length()-1); //crop out the space symbol
            int judgement = BNFparse(line);
            no_of_good_gens = no_of_good_gens + judgement; 
            
        }
        double percentage = no_of_good_gens * Math.pow(no_of_tested_gens, -1);
        return percentage*100;
    }
    
}
