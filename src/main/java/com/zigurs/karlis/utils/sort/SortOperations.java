package com.zigurs.karlis.utils.sort;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Threads(1) /* leave a few cores unused for parallel sort to have free space */
@Fork(CommonParams.FORKS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = CommonParams.WARMUP_ITERATIONS, time = CommonParams.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = CommonParams.BENCHMARK_ITERATIONS, time = CommonParams.BENCHMARK_TIME, timeUnit = TimeUnit.SECONDS)
public class SortOperations {

    @State(Scope.Benchmark)
    public static class ListWrapper {

        private final List<Map.Entry<String, Double>> testList = new ArrayList<>();

        @Param({"100", "1000", "10000", "100000", "1000000"})
        private int listSize;

        @Param({"1", "10", "100", "1000"})
        private int maxItems;

        @Setup
        public void setup() {
            /*
             * Workaround for listSize/maxItems variables not being
             * ready at the call to constructor time.
             *
             * It does make sense, one I think about it. They are sorted out
             * once the instance exists.
             *
             * Yes, it did confuse me for a second.
             */
            if (testList.isEmpty()) {
                populateList();
                Collections.shuffle(testList);
            }
        }

        private void populateList() {
            for (int i = 1; i <= listSize; i++) {
                final String key = String.format("Item-%d", i);
                final Double value = (double) i;

                testList.add(new Map.Entry<String, Double>() {
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
    public boolean partialSortArray(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = MagicSort.sortAndLimitWithArray(
                wrapper.testList,
                wrapper.maxItems,
                (o1, o2) -> -o1.getValue().compareTo(o2.getValue())
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean partialSortArrayBinarySearch(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = MagicSort.sortAndLimitWithArrayAndBinarySearch(
                wrapper.testList,
                wrapper.maxItems,
                (o1, o2) -> -o1.getValue().compareTo(o2.getValue())
        );

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean parallelSortArrayBinarySearch(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.parallelStream()
                .collect(MagicSort.toList(wrapper.maxItems, (o1, o2) -> -o1.getValue().compareTo(o2.getValue())));

        if (list.get(0).getValue() != wrapper.listSize)
            throw new IllegalStateException("Unexpected sort result: " + list.get(0).getValue());

        return true;
    }

    @Benchmark
    public boolean streamArrayBinarySearch(ListWrapper wrapper) {
        List<Map.Entry<String, Double>> list = wrapper.testList.stream()
                .collect(MagicSort.toList(wrapper.maxItems, (o1, o2) -> -o1.getValue().compareTo(o2.getValue())));

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
}