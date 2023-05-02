# Rseslib
Rough set and machine learning data structures, algorithms and tools in Java. The project includes algorithms for discernibility matrix, reducts, decision rules and wide range of discretization and classification algorithms, including optimized for large data KNN classifier implementing analogy-based reasoning and dedicated to imbalanced data RIONIDA with multidimensional optimization. The project includes also QMAK - a tool for interacting with machine learning models and visualizing classification process, and Simple Grid Manager for running experiments on many computers or cores. For more information visit https://rseslib.mimuw.edu.pl.

The project is built with Java Development Kit version 8 and Maven. Rseslib can be used in the following ways:

###  1. Java library
[Rseslib 3 algorithms](https://rseslib.mimuw.edu.pl/algorithms.html) provide a brief list of algorithms available in the project and [Rseslib User Guide](https://rseslib.mimuw.edu.pl/rseslib.pdf) is the main source of information how to use Rseslib components within Java code.

###  2. Command line
Rseslib includes command-line programs computing reducts or rules or running experiments with Rseslib classifiers. See the chapter *Command line programs* in [Rseslib User Guide](https://rseslib.mimuw.edu.pl/rseslib.pdf) for information how to run them. If you run a program from the source without Maven, add weka.jar version 3.8.x to classpath to make it work on ARFF data files, for example:
```
java -cp weka.jar rseslib.example.ComputeReducts iris.arff iris.reducts
```

### 3. WEKA platform
4 selected classifiers (Rough Set based, K Nearest Neighbors, K Nearest Neighbors with Local Metric Induction, and RIONIDA) are available in WEKA. See the chapter *WEKA* in [Rseslib User Guide](https://rseslib.mimuw.edu.pl/rseslib.pdf) for information how to install the Rseslib package using WEKA package manager and where to find Rseslib classifiers in WEKA catalog.

### 4. QMAK
QMAK is a GUI tool included in Rseslib. 5-minute video demonstrating QMAK is available at http://rseslib.mimuw.edu.pl/qmak. See the chapter *QMAK: Interaction wit classifers and their visualization* in [Rseslib User Guide](https://rseslib.mimuw.edu.pl/rseslib.pdf) and *Help* in the main menu of the appliction for information how to run and use the tool. If you run QMAK from the source without Maven, add weka.jar version 3.8.x to classpath to make it work on ARFF data files:
```
java -cp weka.jar rseslib.qmak.QmakMain
```

### 5. Simple Grid Manager
Simple Grid Manager is a tool included in Rseslib for running experiments with Rseslib classifiers on many computers or cores. See the chapter *SGM: Computing many experiments on many computers/cores* in [Rseslib User Guide](https://rseslib.mimuw.edu.pl/rseslib.pdf) for information how to configure experiments and run the tool.
