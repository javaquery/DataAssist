/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hibernateassist.database;

/**
 *
 * @author 0Signals
 */
public enum SQLServerOperationImageEnum {

    ComputeScalar, TableSpool, Sequence, TableValuedFunction, TableInsert,
    Top, Filter, SequenceProject, Segment, RIDLookup,
    Concatenation, TableDelete, TableUpdate, StreamAggregate, TableScan,
    Sort, NestedLoops, Merge, HashMatch, ClusteredIndexScan,
    ClusteredIndexSeek, IndexSeek,
    DistributeStreams, RepartitionStreams, GatherStreams;

    @Override
    public String toString() {
        if (this == ComputeScalar) {
            return "ComputeScalar.gif";
        } else if (this == TableSpool) {
            return "TableSpool.gif";
        } else if (this == Sequence) {
            return "Sequence.gif";
        } else if (this == TableValuedFunction) {
            return "TableValuedFunction.gif";
        } else if (this == TableInsert) {
            return "TableInsert.gif";
        } else if (this == Top) {
            return "Top.gif";
        } else if (this == Filter) {
            return "Filter.gif";
        } else if (this == SequenceProject) {
            return "SequenceProject.gif";
        } else if (this == Segment) {
            return "Segment.gif";
        } else if (this == RIDLookup) {
            return "RIDLookup.gif";
        } else if (this == Concatenation) {
            return "Concatenation.gif";
        } else if (this == TableDelete) {
            return "TableDelete.gif";
        } else if (this == TableUpdate) {
            return "TableUpdate.gif";
        } else if (this == StreamAggregate) {
            return "StreamAggregate.gif";
        } else if (this == TableScan) {
            return "TableScan.gif";
        } else if (this == Sort) {
            return "Sort.gif";
        } else if (this == NestedLoops) {
            return "NestedLoops.gif";
        } else if (this == Merge) {
            return "Merge.gif";
        } else if (this == HashMatch) {
            return "HashMatch.gif";
        } else if (this == ClusteredIndexScan) {
            return "ClusteredIndexScan.gif";
        } else if (this == ClusteredIndexSeek) {
            return "ClusteredIndexSeek.gif";
        } else if (this == IndexSeek) {
            return "IndexSeek.gif";
        } else if (this == DistributeStreams) {
            return "DistributeStreams.gif";
        } else if (this == RepartitionStreams) {
            return "RepartitionStreams.gif";
        } else if (this == GatherStreams) {
            return "GatherStreams.gif";
        }
        return "";
    }

    public String getTitle() {
        if (this == ComputeScalar) {
            return "Compute Scalar";
        } else if (this == TableSpool) {
            return "Table Spool";
        } else if (this == Sequence) {
            return "Sequence";
        } else if (this == TableValuedFunction) {
            return "Table Valued Function";
        } else if (this == TableInsert) {
            return "Table Insert";
        } else if (this == Top) {
            return "Top";
        } else if (this == Filter) {
            return "Filter";
        } else if (this == SequenceProject) {
            return "Sequence Project";
        } else if (this == Segment) {
            return "Segment";
        } else if (this == RIDLookup) {
            return "RID Lookup";
        } else if (this == Concatenation) {
            return "Concatenation";
        } else if (this == TableDelete) {
            return "Table Delete";
        } else if (this == TableUpdate) {
            return "Table Update";
        } else if (this == StreamAggregate) {
            return "Stream Aggregate";
        } else if (this == TableScan) {
            return "Table Scan";
        } else if (this == Sort) {
            return "Sort";
        } else if (this == NestedLoops) {
            return "Nested Loops";
        } else if (this == Merge) {
            return "Merge";
        } else if (this == HashMatch) {
            return "Hash Match";
        } else if (this == ClusteredIndexScan) {
            return "Clustered Index Scan";
        } else if (this == ClusteredIndexSeek) {
            return "Clustered Index Seek";
        } else if (this == IndexSeek) {
            return "Index Seek";
        } else if (this == DistributeStreams) {
            return "Distribute Streams";
        } else if (this == RepartitionStreams) {
            return "Repartition Streams";
        } else if (this == GatherStreams) {
            return "Gather Streams";
        }
        return "";
    }
}
