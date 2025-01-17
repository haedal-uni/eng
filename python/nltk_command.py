from nltk.corpus import wordnet as wn
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

def nltk_command(command, word):
    global response
    if command == "synonyms":
        response = get_synonyms(word)
    elif command == "examples":
        response = get_examples(word)
    elif command == "definition":
        response = get_definition(word)
    elif command == "pos":
        response = get_pos(word)
    return json.dumps(response)