from nltk.corpus import wordnet as wn
from nltk.stem import WordNetLemmatizer
import json

# 동의어
def get_synonyms(word):
    synsets = wn.synsets(word)
    synonyms = []
    for synset in synsets:
        synonyms.extend([lemma.name() for lemma in synset.lemmas()])
    return list(set(synonyms))

# 예문
def get_examples(word):
    synsets = wn.synsets(word)
    examples = []
    for synset in synsets:
        examples.extend(synset.examples())
    return list(set(examples))

# 정의
def get_definition(word):
    synsets = wn.synsets(word)
    definitions = []
    for synset in synsets:
        definitions.append(synset.definition())
    return list(set(definitions))

# 단어의 품사(동사, 명사 등)
def get_pos(word):
    synsets = wn.synsets(word)
    pos_tags = []
    for synset in synsets:
        pos_tags.append(synset.pos())
    return list(set(pos_tags))

# 품사 알기
def get_lemmatizer(word, pos):
    lemmatizer = WordNetLemmatizer()
    return list(lemmatizer.lemmatize(word, pos))

def nltk_command(command, word, pos=None):
    global response
    if command == "synonyms":
        response = get_synonyms(word)
    elif command == "examples":
        response = get_examples(word)
    elif command == "definition":
        response = get_definition(word)
    elif command == "pos":
        response = get_pos(word)
    elif command == "lemmatizer" and pos:
        response = get_lemmatizer(word, pos)
    return json.dumps(response)