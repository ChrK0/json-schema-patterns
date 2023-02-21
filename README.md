# JSON Schema Patterns
This is a reproduction package for an experiment to convert regular expressions (patterns) from [ECMAScript](https://json-schema.org/draft/2020-12/json-schema-core.html#rfc.section.6.4) to [brics](https://www.brics.dk/automaton/) syntax.
For all schemas in the [JSON Schema Corpus](https://github.com/sdbs-uni-p/json-schema-corpus) (commit ``79f808b``) the experiment collects all patterns found at the keywords ``"pattern"`` and ``"patternProperties"``.
From this list of patterns in ECMAScript syntax, duplicates are eliminated and then each pattern is converted to brics syntax using the Github project [ECMAScript2Brics](https://github.com/sdbs-uni-p/ECMAScript2Brics).

The results consist of statistics about the collected patterns and its possibility to be converted to brics syntax as well as the conversion result for each pattern.

This reproduction package has been created by Christoph Köhnen.
The package is provided as a Docker container.

The original results are provided in directory [results](results) and are based on commit ``3a44d87`` of [ECMAScript2Brics](https://github.com/sdbs-uni-p/ECMAScript2Brics).

## Reproduce the experiment inside a docker container

### Reproduce the original experiment
1. Clone this project with ``git clone https://github.com/ChrK0/json-schema-patterns.git`` or copy all sources from [Zenodo](https://doi.org/10.5281/zenodo.7586341).
2. Go to the root directory of this repository with ``cd json-schema-patterns``.
3. Build the container with ``docker build -t patterns_img .``.
4. Start the container with ``docker run --name patterns patterns_img`` (if there is another container with name ``patterns`` on your host system, remove it or choose another name).
5. Copy the results to your host system with
```shell
docker cp patterns:/home/repro/json-schema-patterns/results_"$(<ECMAScript2Brics/commithash)" ../results_"$(<ECMAScript2Brics/commithash)"
```

The steps 3 to 5 can also be executed with ``./dispatch.sh``.

### Running experiments manually for arbitrary ECMAScript2Brics versions
1. Clone this project with ``git clone https://github.com/ChrK0/json-schema-patterns.git`` or copy all sources from [Zenodo](https://doi.org/10.5281/zenodo.7586341).
2. Go to the root directory of this repository with ``cd json-schema-patterns``.
3. Build the container with ``docker build -t patterns_img .``.
4. Start the container with ``docker run -it --name patterns patterns_img`` (if there is another container with name ``patterns`` on your host system, remove it or choose another name).
5. Inside the container, check out the commit hash of the currently loaded version in [ECMAScript2Brics/commithash](ECMAScript2Brics/commithash).
6. If necessary, load the source code of another commit with ``./load-ecmascript2brics.sh <hash>`` replacing the optional parameter ``<hash>`` by the hash of the commit to be loaded. 
   If the parameter is omitted, the latest version will be loaded. Then setup dependencies with ``./setup.sh``.
7. Run the experiment with ``./run-experiment.sh "../json-schema-corpus/json_schema_corpus"``.
8. Exit the container with ``[ctrl]+p[ctrl]+q``.
9. Copy the results to your host system with
```shell
docker cp patterns:/home/repro/json-schema-patterns/results_"$(<ECMAScript2Brics/commithash)" ../results_"$(<ECMAScript2Brics/commithash)"
```

### Inspecting results inside the docker container
The results are stored at ``/home/repro/json-schema-patterns/results_<hash>``.
For example, inspect the statistics with ``cat results_<hash>/summary.csv | column -t -s ';'``.
You can compare the results with the original ones with ``diff results results<hash>``.

## Running experiments on the host system
1. Clone this project with ``git clone https://github.com/ChrK0/json-schema-patterns.git`` or copy all sources from [Zenodo](https://doi.org/10.5281/zenodo.7586341).
2. Go to the root directory of this repository with ``cd json-schema-patterns``.
3. If necessary, load the source code of another commit with ``./load-ecmascript2brics.sh <hash>`` replacing the optional parameter ``<hash>`` by the hash of the commit to be loaded.
   If the parameter is omitted, the latest version will be loaded. Then setup dependencies with ``./setup.sh``.
4. Run the experiment with ``./run-experiment.sh "<json_files>"`` where ``<json_files>`` can be the path to the JSON Schema Corpus or an arbitrary path on the host system containing JSON schema files.

### Inspecting results on the host system
The results are stored at ``/home/repro/json-schema-patterns/results_<hash>``.
For example, inspect the statistics with ``cat results_<hash>/summary.csv | column -t -s ';'``.
You can compare the results with the original ones with ``diff results results<hash>``.

## Structure of the results folder
The folder ``results`` (resp. ``results_<hash>`` after running the experiment) consists of the following files and folders:

### The file ``patterns.csv``
This file consists of all patterns found in the JSON Schema files with one unique pattern per line and the names of the files, where this pattern was found.

### The folder ``detailed``
This folder consists of the following files, each of them containing a list of patterns:
#### ``anchoredInsidePatterns.csv``
All patterns containing ``^`` after the beginning or ``$`` before the end.
#### ``anchoredPatterns.csv``
All patterns containing ``^`` or ``$``.
#### ``ìnvalidPatterns.csv``
All patterns which do not fulfill the ECMAScript syntax.
#### ``notConvertablePatterns.csv``
All patterns which cannot be converted for an unknown reason.
#### ``notSupportedPatterns.csv``
All patterns which are not supported by the chosen version of [ECMAScript2Brics](https://github.com/sdbs-uni-p/ECMAScript2Brics).
#### ``nullablePatterns.csv``
All patterns which accept the empty word.
#### ``unanchoredPatterns.csv``
All patterns which do not contain ``^`` or ``$``.

### The file ``conversions.csv``
This file consists of all patterns found in the JSON Schema files with one unique pattern per line and its conversion to brics syntax.

### The file ``summary.csv``
This file contains the numbers gained from the experiment.

It lists the number of total files (``Files total``) and files containing patterns found at the keywords ``"pattern"`` or ``"patternProperties`` (``Files with patterns``).

It compares the number of patterns found at ``"pattern"`` (``"pattern" count total``) and at ``"patternProperties"`` (``"patternProperties" count total``), the total numbers and the numbers of unique patterns, i.e. after eliminating duplicates (``"pattern" resp. "patternProperties" count unique``).

For all unique patterns (``Unique patterns``), without differentiation by the keywords, the numbers of patterns written to the files in ``detailed`` are shown.
