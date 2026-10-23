package com.allst.es.scenario.common;

import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;

import java.util.ArrayList;
import java.util.List;

public final class AggregationReads {
    private AggregationReads() {
    }

    public static List<NamedCount> terms(Aggregations aggregations, String name) {
        List<NamedCount> result = new ArrayList<NamedCount>();
        if (aggregations == null) {
            return result;
        }
        Terms terms = aggregations.get(name);
        if (terms == null) {
            return result;
        }
        for (Terms.Bucket bucket : terms.getBuckets()) {
            NamedCount item = new NamedCount();
            item.setKey(bucket.getKeyAsString());
            item.setCount(bucket.getDocCount());
            item.setMetric(metric(bucket.getAggregations().get("metric")));
            result.add(item);
        }
        return result;
    }

    public static List<NamedCount> nestedTerms(Aggregations aggregations, String filterName, String termsName) {
        if (aggregations == null) {
            return new ArrayList<NamedCount>();
        }
        Filter filter = aggregations.get(filterName);
        if (filter == null) {
            return new ArrayList<NamedCount>();
        }
        return terms(filter.getAggregations(), termsName);
    }

    public static List<TrendPoint> histogram(Aggregations aggregations, String name) {
        List<TrendPoint> result = new ArrayList<TrendPoint>();
        if (aggregations == null) {
            return result;
        }
        Histogram histogram = aggregations.get(name);
        if (histogram == null) {
            return result;
        }
        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            TrendPoint point = new TrendPoint();
            point.setTime(bucket.getKeyAsString());
            point.setCount(bucket.getDocCount());
            point.setMetric(metric(bucket.getAggregations().get("metric")));
            result.add(point);
        }
        return result;
    }

    public static double value(Aggregations aggregations, String name) {
        if (aggregations == null) {
            return 0D;
        }
        Double metric = metric(aggregations.get(name));
        return metric == null ? 0D : metric;
    }

    public static long filterCount(Aggregations aggregations, String name) {
        if (aggregations == null) {
            return 0L;
        }
        Filter filter = aggregations.get(name);
        return filter == null ? 0L : filter.getDocCount();
    }

    private static Double metric(Aggregation aggregation) {
        if (!(aggregation instanceof NumericMetricsAggregation.SingleValue)) {
            return null;
        }
        double value = ((NumericMetricsAggregation.SingleValue) aggregation).value();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return value;
    }
}
