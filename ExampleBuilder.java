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
 * In this class, a given number of training examples in my language are written to a .txt file. Use toFile(String file_name, int m) for this.  
 *
 * @author Tisha Anders
 * @version 1
 */
public class ExampleBuilder
{
    /**
     * This method generates an n_n randomly, between 1 and 9
     *
     * @return int n_n
     */
    static int generate_n_n()
    {
        Random rand = new Random();
        return rand.nextInt(9) + 1;
    }

    /**
     * This method generates an n_v randomly, between 1 and 5
     *
     * @return int n_v
     */
    static int generate_n_v()
    {
        Random rand = new Random();
        return rand.nextInt(5) + 1;
    }

    /**
     * This method generates an n_d randomly, between 0 and 3
     *
     * @return int n_d
     */
    static int generate_n_d()
    {
        Random rand = new Random();
        return rand.nextInt(4);
    }

    /**
     * This method generates an n_p randomly, between 0 and 2
     *
     * @return int n_p
     */
    static int generate_n_p()
    {
        Random rand = new Random();
        return rand.nextInt(3);
    }

    /**
     * This method generates a String of n_d Ds, i.e. a determiner.
     *
     * @return String the string of Ds, i.e. the determiner
     */
    static String determiner()
    {
        String det = "";
        int n_d = generate_n_d();
        for (int i = 0; i < n_d ; i++)
        {
            det = det + "D";
        }
        return det;
    }

    /**
     * This method generates a String of n_n Ns, i.e. a noun.
     *
     * @return String the string of Ns, i.e. the noun
     */
    static String nn()
    {
        String noun = "";
        int n_n = generate_n_n();
        for (int i = 0; i < n_n ; i++)
        {
            noun = noun + "N";
        }
        return noun;
    }

    /**
     * This method generates a String of n_v Vs, i.e. a verb.
     *
     * @return String the string of Vs, i.e. the verb
     */
    static String vb()
    {
        String verb = "";
        int n_v = generate_n_v();
        for (int i = 0; i < n_v ; i++)
        {
            verb = verb + "V";
        }
        return verb;
    }

    /**
     * This method gives a verb with its ending adjusted to the preceding noun, i.e. a string consisting of VV..V + (S or T).
     * @param Boolean person: whether the noun that this verb belongs to is in 3rd person singular or not 
     * @return String VV..V + (S or T)
     */
    static String verb(Boolean third_p_sgl)
    {
        if (third_p_sgl == true)
        {
            return vb() + "S";
        }
        else
        {
            return vb() + "T";
        }
    }

    /**
     * This method generates a String of n_p Ps, i.e. an adjective/preposition/etc.
     *
     * @return String the string of Ps, i.e. the adjective or another filler
     */
    static String adjective()
    {
        String adj = "";
        int n_p = generate_n_p();
        for (int i = 0; i < n_p ; i++)
        {
            adj = adj + "P";
        }
        return adj;
    }

    /**
     * This method generates a noun with ending, i.e. a sequence of n_n Ns, plus a random ending indicating animation & person. The following relative pronoun's & verb's agreement will depend on the ending of this string.
     *
     * @return String a noun String, i.e. NN...N + (A or B) + (S or T)
     */
    static String noun()
    {
        String noun = nn();
        Random rand2 = new Random();
        int rand_intA = rand2.nextInt(2);
        Random rand3 = new Random();
        int rand_intS = rand3.nextInt(2);
        if (rand_intA == 0)
        {
            noun = noun + "A";
        }
        else
        {
            noun = noun + "B";
        }
        if (rand_intS == 0)
        {
            noun = noun + "S";
        }
        else
        {
            noun = noun + "T";
        }
        return noun;
    }

    /**
     * This method generates a comma, i.e. a string consisting of exactly one C.
     *
     * @return String C
     */
    static String comma()
    {
        return "C";
    }

    /**
     * This method generates the last comma, i.e. a string consisting of exactly one Z. It marks the end of the relative clause section.
     *
     * @return String C
     */
    static String final_comma()
    {
        return "Z";
    }

    /**
     * This method gives a relative pronoun with its ending adjusted to the preceding noun, i.e. a string consisting of R + (A or B).
     * @param Boolean animation: whether the noun that this relative pronoun refers to, is animate or not. 
     * @return String R + (A or B)
     */
    static String rel_pro(Boolean animate)
    {
        if (animate == true)
        {
            return "RA";
        }
        else
        {
            return "RB";
        }
    }

    /**
     * This method generates an intro for the sentence, e.g. "The blue hat", which imposes agreement constraints on the rest of the sentence.
     *
     * @return String intro
     */
    static String intro()
    {
        String intro = determiner() + adjective() + noun();
        return intro;
    }

    /**
     * This method generates the relative clauses part for the sentence, agreeing with what has been generated as a subject, e.g. "The blue hat, which Jacob said, who is ill".
     *
     * @return String rel_clauses
     */
    static String rel_clauses()
    {
        String sentence_so_far = intro() + " " + "\t";
        Boolean last_noun_animate;
        Boolean last_noun_s;
        Random rand5 = new Random();
        int n_rel_clauses = rand5.nextInt(4); // between 1 and 4 relative clauses in 1 sentence, because of the for-loop below
        for (int i = 0; i <= n_rel_clauses; i++)
        {
            int lastN = sentence_so_far.lastIndexOf("N");
            if (sentence_so_far.charAt(lastN + 1) == 'A')
            {
                last_noun_animate = true;
            }
            else
            {
                last_noun_animate = false;
            }
            if (sentence_so_far.charAt(lastN + 2) == 'S')
            {
                last_noun_s = true;
            }
            else
            {
                last_noun_s = false;
            }
            //sentence_so_far = sentence_so_far + rel_pro(last_noun_animate) + verb(last_noun_s) + adjective() + noun() + adjective(); //the last adj might be left out

            sentence_so_far = sentence_so_far + comma() + rel_pro(last_noun_animate) + verb(last_noun_s) + adjective() + noun() + adjective();
        }
        return sentence_so_far;
    }

    /**
     * This method puts together a sentence in my mickey-mouse language, using the above methods.
     *
     * @return the whole sentence (intro+space+tab+rel_clauses+outro)
     */
    static String sentence()
    {
        String sentence_so_far = rel_clauses();
        Boolean subject_animate;
        Boolean subject_s;
        int firstComma = sentence_so_far.indexOf("C");
        if (sentence_so_far.charAt(firstComma -4) == 'A')
        {
            subject_animate = true;
        }
        else
        {
            subject_animate = false;
        }
        if (sentence_so_far.charAt(firstComma -3) == 'S')
        {
            subject_s = true;
        }
        else
        {
            subject_s = false;
        }
        sentence_so_far = sentence_so_far + final_comma() + verb(subject_s) + determiner() + adjective() + noun() + " ";
        return sentence_so_far;
    }

    /**
     * This method appends generated sentences to a .txt file.
     * @param file_name the file we want to write to, excluding .txt
     * @param m the number of sentences we want to generate
     */
    static void toFile(String file_name, int m) throws FileNotFoundException
    {
        File myFile = new File(file_name + ".txt");
        PrintStream p = new PrintStream(myFile);
        for (int i = 1; i <= m; i++)
        {
            String sentence = sentence();
            p.println(sentence);
        }
        p.close();
    }
}
