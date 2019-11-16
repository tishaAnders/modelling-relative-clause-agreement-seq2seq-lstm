# Modelling Agreement in English Relative Clauses with an LSTM

## Update info

:triangular_flag_on_post: **16-11-19** Added Readme & possible set of LSTM generations
:triangular_flag_on_post: **15-11-19** Uploaded software & possible training dataset for LSTM

# Introduction
To demonstrate the efficacy of LSTMs in learning long-term dependencies which confuse even humans, I designed a toy language to model relative clause constructions in English. Sentences with multiple, nested, relative clauses require the reader to keep track of the sentence subject's grammatical attributes until the end of the sentence is reached, where the sentence is artificially prolonged by insertion of relative clauses in the middle. 


## How agreement works in English relative clause constructions 
> The old **book**, *which* was written by John Doe, who loves children, *deals* with numbers greater than zero.

Fundamentally, the proposition of this sentence is:

>*The old book deals with numbers greater than zero.*

Notice, that since **book** is not animate (i.e. a thing), *which* is the relative pronoun that refers back to it (instead of *who*). Additionally, **book** is in third person singular, so it requires an *s* to be attached to *deal*, which is at the end of the sentence. 
It is important to mention that even to humans, relative clauses pose a hurdle for information retrieval, because the long distances between the agreeing entities (subject and verb, not so much subject and relative pronoun). Furthermore, there can be a possible, competing subject closer to the final verb: *children*, which obviously does not agree with *deals* as the *s* is redundant ([Agreement Checking in Comprehension: Evidence from Relative Clauses](https://link.springer.com/article/10.1023/A:1005124523808)). 

Of course we also have agreement between *John Doe* and *who*, as well as *John Doe* and *loves*. 

To summarize: 
A subject assigns +s to its verb if it is in third person singular. Otherwise,
it assigns −s.
A subject assigns +a to its relative pronoun if it is animate. If inanimate,
it assigns −a (This is not strictly true for all cases, e.g. animals, but
for the purpose of simplicity, leave that aside). Notice that a relative
pronoun which has been assigned +a & −a, appears as *who* and *which*
respectively.

## Modelling Agreement in a formal language

Below you can find the language used to model sentences with embedded relative clauses, including agreement (in Backus-Naur Form): 
```
<s> ::= <u><x><y>
<u> ::= <d><p><n> | <d><n> | <p><n> | <n>
<x> ::= <q><qq><qqq><qqqq>
<q> ::= <c><r><v><p><n><p> | <c><r><v><p><n> | <c><r><v><n> |
<c><r><v><n>
<y> ::= <z><v><d><p><n> | <z><v><d><n> | <z><v><p><n> | <z><v><n> |
<c> ::= "C"
<d> ::= D | DD | DDD
<p> ::= P | PP
<n> ::= "Ng" | "NNg" | "NNNg" | "NNNNg" | "NNNNNg" | "NNNNNNg" |
"NNNNNNNg" | "NNNNNNNNg" | "NNNNNNNNNNg"
<g> ::= "AS" | "AT" | "BS" | "BT"
<v> ::= "VS" | "VVS" | "VVVS" | "VVVVS" | "VVVVVS" | "VT" | "VVT" |
"VVVT" | "VVVVT" | "VVVVVT"
<z> ::= "Z"
<r> ::= "RA" | "RB"
```
Some context meaning is assigned to the tokens in the following: 

| syntactic unit | corresponds to |
|--------------|--------------|
| `<s>` | sentence with relative clause(s) |
| `<u>` | intro (e.g. *The old book*) |
|`<x>`|RC part of sentence (e.g. *, who like flannel jackets, which were expensive*)|
|`<q>`|one relative clause (e.g. *who likes spaghetti*)|
|`<y>`|outro (e.g. *, wears a red jacket*)|
|`<c>`|comma (every relative clause has a comma to its left and right)|
|`<d>`|determiner (e.g. *the* or *three out of two*)|
|`<p>`|adjective / other noise|
|`<e>`|the empty string|
|`<n>`|noun (e.g. *men*)|
|`<g>`|ending of noun that imposes agreement on relative pronoun & verb|
|`<v>`|verb (e.g. *wears*), also bears agreement from noun (S if noun assigns [+s], T if noun assigns [−s])|
|`<z>`|final comma (before outro starts)|
|`<r>`|relative pronoun, which bears agreement from noun (A if noun assigns [+a], B if noun assigns [−a])|

## Creating Examples
Use the method `toFile(String file_name, int m)` in *ExampleBuilder.java* to generate a .txt file with *m* training examples. The file name you enter should not contain ".txt". 
You can generate your own examples with the Java file or just use the examples provided in *dataset.txt*. 

## Generating strings with LSTM

The LSTM architecture is described in detail in [Sequence to Sequence Learning with Neural Networks (Sutskever et al.)](https://papers.nips.cc/paper/5346-sequence-to-sequence-learning-with-neural-networks.pdf).
To make the LSTM generate strings in the toy language, based on examples from a file, run *string_generator_gpu.py* to train your encoder-decoder LSTM. Change the datapath (in line 16) of *string_generator_gpu.py* to your dataset generated with *ExampleBuilder.java*. If you leave the datapath unchanged, the LSTM will be trained on the default dataset provided in this repository (*dataset.txt*).
The encoder LSTM takes in the "intro" to the sentence (e.g. *The old book*) and stores a numerical summary of important features in the last hidden state *v*. The decoder LSTM learns how to sample the rest of the sentence, given *v* as input.
Notice that the decoder LSTM has to deal with a long long-term dependency in the subject-verb agreement between intro and outro, but also minor long-term dependencies to ensure agreement within the relative clauses and maintain the basic word-order.

It returns a .txt file that contains (hopefully) valid strings in the toy language. 
You can try run your own LSTM, or use the generations provided in this repository (*2019-11-16-14-23-45_gen.txt*).
Unfortunately, the *.py* file only allows GPU computation, since this is much faster. A CPU version may be provided at some point. 

## Testing the accuracy of the LSTM (Performance Analysis)

Basic word-order of the generations can be checked with *BNFParser.java*. Use
* `BNFparse(String s)` to check the word order of a single string, 
* `testFromFile(String filename)` to see, given a file, the percentage of strings with correct word order 

Agreement of a single string can be checked with *AgreementandBNFParser.java*:
* `checkAgreement(String s)`

Percentage of strings with correct word order & agreement (from file) can be computed with *AgreementandBNFParser.java*:
* `testFromFile(String filename)`

## A few notes on training
### Epochs
The LSTM will generate correct examples with less than 200 epochs, but they will be very lengthy and not differ much from each other. 
Training the LSTM on 1800 epochs will lower the validation accuracy slightly, which would indicate overfitting. However, the sentences will vary a little bit more, which seems more natural. 
### Accuracy
*2019-11-16-14-23-45_gen.txt* provides an example of what *string_generator_gpu.py* could have created. It returns 96.6% accuracy for both word order and agreement. 
This result, together with some of my analyses of bad generations, indicates that agreement (i.e. long-term dependencies) is not a problem at all for the LSTM. However, for instance, it struggles with constraining itself to the correct number of `V`s allowed in a verb, though only on a very minor basis. 

## Final note
Technical details beyond this short exposé can be found in my Bachelor's thesis, e.g.
* how an LSTM learns
* the architecture of an Encoder-Decoder LSTM
* the Backus-Naur Form
* Context-Free Grammars
* Agreement in Relative Clauses
