Exception classifier
-------------------

This document describes basic outline of classification algorithm in 
exception subsystem.

Goal
====

Goal of classifier is, based on exception metadata (class, 
message, stacktrace and cause), to classify exception into category,
that describes in what layer/component of application bug happened.

We consider following categories:

* Database
* Integration
* Web
* Utils
* Network
* File
* JVM
* Unknown

These categories are provided as TicketClass enum.

Algorithm
=========

Algorithm uses idea, that most of Java libraries have well-defined
namespaces, based on package where classes are placed in.
We will use offline database of package names to decide, in which category, we will place bug into.

We use package names of 100 most popular libraries and Wildfly specific packages and its dependencies. Each package name gets labeled with category and weight.

To classify exception we will traverse stacktrace of exception.
For each stacktrace element we will try to match package name of class name to package name in packages database and we will match given label and weight to it.

After we traversed all stacktrace elements, we sum weight of every category and category with maximum weight is category of exception.

Since most important parts of stacktrace are usually at top of it, since thats were exception was thrown, we will also put more value to labels at top of stacktrace, so elements, that occur in nearly every stacktrace (like executors and thread handling classes) are nearly ignored if they are not on top of stacktrace.


Implementation
==============

For efficient search of package, we use trie data structure.
Each node of trie tree containts one package token, label and weight of package, that name contains every token on path from root to given node in this order.

Using this approach, we save space for storing every package name.

Node class
```
public class Node {
  String token;
  TicketClass label;
  double weight;
  List<Node> children;

  boolean isLeaf();

  withChildren(List<Node> newChildren);
}
```
Package trie Builder

```
public class PackageTrieBuilder {
  PackageTrieBuilder addPackage(String packageName, TicketClass label, double weight);

  PackageTrie build();
}
```

Immutable Package Trie implementation
```
public class PackageTrie {
  Node lookup(String className);
}
```

```
public class ExceptionReportClassifier {
  classify(ExceptionReport report);
}
```


Testing and adjusting weights
=============================

*TODO* Collect data set of exceptions from stack overflow and JBoss forums.

References

[1] http://en.wikipedia.org/wiki/Trie
[2] https://docs.google.com/spreadsheet/ccc?key=0Alceaf46X4GPdHhKV0FFSnVpTlA2SkpiaVU0M3BDYXc&usp=sharing#gid=0
