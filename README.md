# MagicSortBenchmarks

[MagicSort](https://github.com/karliszigurs/MagicSort) library [JMH](http://openjdk.java.net/projects/code-tools/jmh/) benchmarks (comparisons against various JDK sort implementations).

Stores results achieved at the time of the benchmark run, useful for regression checking and comparison between different sorting options in Java 8 (at the time) JDK. Especially useful if you like tables with lots of numbers.

```
$ mvn clean package
$ java -jar target/benchmarks.jar
```

## Results available

* [04 November 2016 - MagicSort 0.2-SNAPSHOT](https://github.com/karliszigurs/MagicSortBenchmarks/blob/master/results/20161104-0.2-SNAPSHOT/summary.pdf)

### Credits

```
                              //
(C) 2016 Karlis Zigurs (http://zigurs.com)
                            //
```
