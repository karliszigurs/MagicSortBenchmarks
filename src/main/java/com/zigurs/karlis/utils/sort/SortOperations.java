package com.zigurs.karlis.utils.sort;

import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Threads(1) /* leave a few cores unused for parallel sort to have free space */
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
    public boolean partialSortInsertion(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = MagicSort.sortAndLimitWithArray(
                wrapper.testList,
                wrapper.maxItems,
                (e1, e2) -> -e1.getValue().compareTo(e2.getValue())
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean partialSortBSearch(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = MagicSort.sortAndLimitWithArray(
                wrapper.testList,
                wrapper.maxItems,
                (e1, e2) -> -e1.getValue().compareTo(e2.getValue())
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean partialSortStream(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .collect(MagicSort.toList(wrapper.maxItems, (e1, e2) -> -e1.getValue().compareTo(e2.getValue())));

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean partialSortParallelStream(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .collect(MagicSort.toList(wrapper.maxItems, (e1, e2) -> -e1.getValue().compareTo(e2.getValue())));

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .filter(i -> i != null)
                .sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean parallelSort(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .filter(i -> i != null)
                .sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))
                .limit(wrapper.maxItems)
                .collect(Collectors.toList());

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean collectionsSort(ListWrapper wrapper) {
        Collections.sort(wrapper.testList, (e1, e2) -> -e1.getValue().compareTo(e2.getValue()));

        if (wrapper.testList.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + wrapper.testList.get(0).getValue());

        return true;
    }
}
