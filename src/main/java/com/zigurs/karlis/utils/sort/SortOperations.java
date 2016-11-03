package com.zigurs.karlis.utils.sort;

import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Threads(1) /* leave a few cores unused for parallel sorts to have free space to play */
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class SortOperations {

    @State(Scope.Benchmark)
    public static class ListWrapper {

        private static final String ARRAY = "ArrayShuffled";
        private static final String LINKED = "LinkedListShuffled";
        private static final String LINKED_SORTED = "LinkedListSorted";

        private final Comparator<Map.Entry<String, Double>> comparator = (e1, e2) -> -e1.getValue().compareTo(e2.getValue());

        private List<Map.Entry<String, Double>> testList = null;

        @Param({ARRAY, LINKED, LINKED_SORTED})
        private String listType;

        @Param({"100", "1000", "10000", "100000", "1000000"})
        private int listSize;

        @Param({"1", "5", "10", "100", "250"})
        private int maxItems;

        @Setup(Level.Invocation)
        public void setup() {
            switch (listType) {
                case ARRAY:
                    testList = new ArrayList<>();
                    resetList(testList);
                    Collections.shuffle(testList);
                    break;

                case LINKED:
                    testList = new LinkedList<>();
                    resetList(testList);
                    Collections.shuffle(testList);
                    break;

                case LINKED_SORTED:
                    testList = new LinkedList<>();
                    resetList(testList);
                    break;
            }
        }

        private void resetList(List<Map.Entry<String, Double>> list) {
            list.clear();
            for (int i = 1; i <= listSize; i++) {
                final String key = String.format("Item-%d", i);
                final Double value = (double) i;

                list.add(new Map.Entry<String, Double>() {
                    @Override
                    public String getKey() {
                        return key;
                    }

                    @Override
                    public Double getValue() {
                        return value;
                    }

                    @Override
                    public Double setValue(Double value) {
                        throw new UnsupportedOperationException("Not allowed");
                    }
                });
            }
        }

    }

    @Benchmark
    public boolean magicSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = MagicSort.sortAndLimit(
                wrapper.testList,
                wrapper.maxItems,
                wrapper.comparator
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean magicSortStream(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .collect(
                        MagicSort.toList(
                                wrapper.maxItems,
                                wrapper.comparator
                        )
                );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean magicSortParallelStream(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .collect(
                        MagicSort.toList(
                                wrapper.maxItems,
                                wrapper.comparator
                        )
                );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .filter(i -> i != null)
                .sorted(wrapper.comparator)
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamParallelSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .filter(i -> i != null)
                .sorted(wrapper.comparator)
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean collectionsSort(ListWrapper wrapper) {
        Collections.sort(
                wrapper.testList,
                wrapper.comparator
        );

        if (wrapper.testList.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + wrapper.testList.get(0).getValue());

        return true;
    }
}
