This repository contains a prototypical implementation of declarative lifecycles management as described in the submission `Declarative Lifecycle Management in Digital Twins`.

To reproduce the data for the plots, run first `./generateDecl.sh >> decl.csv` (for the non-semantic stages) and then `./generateSem.sh >> sem.csv` (for the semantic stages). Each script will take a few minutes to run. To generate the 3D plot, run `python3 plot.py`.

## Mapping
Operation uri(.) retrieves the URI of an asset, components, asset kind or other parameter.

* ADD(ast,A) is mapped to
```
            INSERT DATA { uri(ast) rdf:type uri(A) }
```
* ADD(ast,c,C) is mapped to
```
            INSERT DATA { uri(c) rdf:type uri(C). uri(ast) assignedTo uri(c) }
```
* UPDATE(ast,p,v) is mapped to
```
            DELETE WHERE { uri(ast) uri(p) ?a }
            INSERT DATA { uri(ast) uri(p) v }
```
* REMOVE(ast) is mapped to the following, and then followed by CLEAN(ast,C)
```
            DELETE WHERE { uri(ast) rdf:type ?a }
```
* CLEAN(ast,C) is mapped to
```
            DELETE WHERE { uri(ast)  ?x  ?y }\\
            DELETE WHERE { ?a  ?b  uri(ast) }\\
            DELETE DATA { uri(C)  rdf:type  Component }\\
```