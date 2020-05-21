### This code was originally taken from https://keras.io/examples/lstm_seq2seq/
### Has been annotated & modified to read from input file and write to output file

from __future__ import print_function
import tensorflow
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input, CuDNNLSTM, LSTM, Dense
import numpy as np
import datetime
import os


batch_size = 512  # No. of training examples per batch
epochs = 1500  # Number of epochs to train for
latent_dim = 256  # Latent dimensionality of the encoding space # default value
num_samples = 10000  # Number of samples to train on

# Path to the data txt file
data_path = 'dataset.txt' ##dataset has 10k examples

## Get training examples ready to be processed
input_texts = [] # will contain all inputs (i.e. intros), which is what is before the tab
target_texts = [] # will contain all outputs (i.e. rest of sentence), which is what comes after the tab
input_characters = set() # input alphabet
target_characters = set() # output alphabet
with open(data_path, 'r', encoding='utf-8') as f: # read through training data
    lines = f.read().split('\n')  # every line from training set becomes a list entry in "lines"
for line in lines[: min(num_samples, len(lines) - 1)]:
    input_text, target_text = line.split('\t') # split line into input text and target text, at tab
    target_text = '\t' + target_text + '\n' # use "tab" as the "start sequence" character for the targets, and "\n" as "end sequence" character.
    input_texts.append(input_text)
    target_texts.append(target_text)
    for char in input_text: # collect all input & output characters
        if char not in input_characters:
            input_characters.add(char)
    for char in target_text:
        if char not in target_characters:
            target_characters.add(char)

# put input- & output characters into alphabetical order
input_characters = sorted(list(input_characters))
target_characters = sorted(list(target_characters))

# dimensionality of vectors encoding letters
num_encoder_tokens = len(input_characters) # length of x^{(t)} in Encoder LSTM
num_decoder_tokens = len(target_characters) # length of x^{(t)} & \hat{y}^{(t)} in Decoder LSTM

# max length of input & output
max_encoder_seq_length = max([len(txt) for txt in input_texts]) # longest intro length
max_decoder_seq_length = max([len(txt) for txt in target_texts]) # longest rest of sentence length

print('Number of samples:', len(input_texts))
print('Number of unique input tokens:', num_encoder_tokens)
print('Number of unique output tokens:', num_decoder_tokens)
print('Max sequence length for inputs:', max_encoder_seq_length)
print('Max sequence length for outputs:', max_decoder_seq_length)

# assign index to every letter
input_token_index = dict(
    [(char, i) for i, char in enumerate(input_characters)])
target_token_index = dict(
    [(char, i) for i, char in enumerate(target_characters)])

print('Input token index:', input_token_index)
print('Target token index:', target_token_index)

## encode input- & output sequences into one-hot vectors
encoder_input_data = np.zeros(
    (len(input_texts), max_encoder_seq_length, num_encoder_tokens), #(m, tau_enc, A_enc)
    dtype='float32') # empty input matrix
decoder_input_data = np.zeros(
    (len(input_texts), max_decoder_seq_length, num_decoder_tokens), #(m, tau_dec, A_dec)
    dtype='float32') # empty decoder input matrix
decoder_target_data = np.zeros(
    (len(input_texts), max_decoder_seq_length, num_decoder_tokens), #(m, tau_dec, A_dec)
    dtype='float32') # empty output matrix

## produce one-hot vectors to encode letters
for i, (input_text, target_text) in enumerate(zip(input_texts, target_texts)): # loop through all elements [ number: (input, target) ]
        encoder_input_data[i, t, input_token_index[char]] = 1. # set feature_no'th entry of (x, t, feature_no) 1 if char is the feature_no'th entry in the alphabet
    encoder_input_data[i, t + 1:, input_token_index[' ']] = 1. # mark the ' ' feature of final timestep with 1
    for t, char in enumerate(target_text):
        decoder_input_data[i, t, target_token_index[char]] = 1. # set feature_no'th entry of (x, t, feature_no) 1 if char is the feature_no'th entry in the alphabet
        if t > 0:
            # decoder_target_data will be ahead of decoder_input_data by one timestep and will not include the start character.
            decoder_target_data[i, t - 1, target_token_index[char]] = 1. # set feature_no'th entry of (x, t, feature_no) 1 if char is the feature_no'th entry in the alphabet
    decoder_input_data[i, t + 1:, target_token_index[' ']] = 1. # mark the ' ' feature of final timestep with 1
    decoder_target_data[i, t:, target_token_index[' ']] = 1. # mark the ' ' feature of final timestep with 1

## Define an input sequence and process it (i.e. architecture)
encoder_inputs = Input(shape=(None, num_encoder_tokens)) # define input format
encoder = CuDNNLSTM(latent_dim, return_state=True) # define the Encoder-LSTM, which will return its final states c^{(tau_enc)} & h^{(tau_enc)}
# CuDNNLSTM is the GPU-compatible LSTM (runs extremely fast)
encoder_outputs, state_ h, state_c = encoder(encoder_inputs) # name quantities that Encoder LSTM outputs
# We discard `encoder_outputs` and only keep the states.
# encoder_states corresponds to vector v:
encoder_states = [state_h, state_c]

# Set up the decoder, using `encoder_states` as initial state
decoder_inputs = Input(shape=(None, num_decoder_tokens))
# We set up our decoder to return full output sequences, and to return internal states as well.
# We don't use the return states in the training model, but we will use them in inference.
decoder_lstm = CuDNNLSTM(latent_dim, return_sequences=True, return_state=True)
decoder_outputs, _, _ = decoder_lstm(decoder_inputs,
                                     initial_state=encoder_states) # feed v into Decoder LSTM
decoder_dense = Dense(num_decoder_tokens, activation='softmax') # put raw outputs through softmax
decoder_outputs = decoder_dense(decoder_outputs)

# Define model architecture that will turn `encoder_input_data` (& `decoder_input_data`) into `decoder_target_data`
model = Model([encoder_inputs, decoder_inputs], decoder_outputs)

## define training process
model.compile(optimizer='Adam', loss='categorical_crossentropy',
              metrics=['accuracy']) # use Adam optimizer, measure loss through categorical cross-entropy, count how often prediction matches target to evaluate performance
model.fit([encoder_input_data, decoder_input_data], decoder_target_data,
          batch_size=batch_size,
          epochs=epochs,
          validation_split=0.2) # fit model to training data with 20% of training data dedicated to validation set

model.save('s2s.h5') # Save model

# Define sampling models
encoder_model = Model(encoder_inputs, encoder_states)

decoder_state_input_h = Input(shape=(latent_dim,))
decoder_state_input_c = Input(shape=(latent_dim,))
decoder_states_inputs = [decoder_state_input_h, decoder_state_input_c] # Decoder LSTM receives v as initial state
decoder_outputs, state_h, state_c = decoder_lstm(
    decoder_inputs, initial_state=decoder_states_inputs)
decoder_states = [state_h, state_c]
decoder_outputs = decoder_dense(decoder_outputs)
decoder_model = Model(
    [decoder_inputs] + decoder_states_inputs,
    [decoder_outputs] + decoder_states)

# Reverse-lookup token index to decode sequences back to something readable.
reverse_input_char_index = dict(
    (i, char) for char, i in input_token_index.items())
reverse_target_char_index = dict(
    (i, char) for char, i in target_token_index.items())

## method to find a possible sentence ending
def decode_sequence(input_seq):
    # Encode the input as state vectors.
    states_value = encoder_model.predict(input_seq)

    # Generate empty target sequence of length 1.
    target_seq = np.zeros((1, 1, num_decoder_tokens))
    # Populate the first character of target sequence with the start character.
    target_seq[0, 0, target_token_index['\t']] = 1.

    # Sampling loop for a batch of sequences
    # (to simplify, here we assume a batch of size 1).
    stop_condition = False
    decoded_sentence = ''
    while not stop_condition:
        output_tokens, h, c = decoder_model.predict(
            [target_seq] + states_value)

        # Sample a token
        sampled_token_index = np.argmax(output_tokens[0, -1, :])
        sampled_char = reverse_target_char_index[sampled_token_index]
        decoded_sentence += sampled_char

        # Exit condition: either hit max length
        # or find stop character.
        if (sampled_char == '\n' or
           len(decoded_sentence) > max_decoder_seq_length):
            stop_condition = True

        # Update the target sequence (of length 1).
        target_seq = np.zeros((1, 1, num_decoder_tokens))
        target_seq[0, 0, sampled_token_index] = 1.

        # Update states
        states_value = [h, c]

    return decoded_sentence

## function to create a unique output file name every time
def timeStamped(fname, fmt='%Y-%m-%d-%H-%M-%S_{fname}'):
    return datetime.datetime.now().strftime(fmt).format(fname=fname)
fname = timeStamped('gen.txt')

## generate ouput sequences and write them to txt file
for seq_index in range(1000): # sample an output sequence for 1000 input sequences
    with open(fname,'a') as outf: # open a file to append input-output pairs to
        input_seq = encoder_input_data[seq_index: seq_index + 1] # pick input sequence from input data
        decoded_sentence = decode_sequence(input_seq) # decode input sequence
        myinput = input_texts[seq_index] # save input sequence (string) to a variable
        myinput = myinput[0:len(myinput)-1] # cut off its last character, which is space
        sentence = myinput + decoded_sentence # a (hopefully) valid sentence is now the input plus the output
        outf.write(sentence) # write full sentence to file to be able to check its accuracy
    print('-')
    print('Input sentence:', input_texts[seq_index])
    print('Decoded sentence:', decoded_sentence)
