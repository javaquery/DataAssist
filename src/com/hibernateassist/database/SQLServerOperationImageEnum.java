/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hibernateassist.database;

/**
 * @author vicky.thakor
 */
public enum SQLServerOperationImageEnum {

    ComputeScalar, TableSpool, Sequence, TableValuedFunction, TableInsert,
    Top, Filter, SequenceProject, Segment, RIDLookup,
    Concatenation, TableDelete, TableUpdate, StreamAggregate, TableScan,
    Sort, NestedLoops, Merge, HashMatch, ClusteredIndexScan,
    ClusteredIndexSeek, IndexSeek, KeyLookup,
    DistributeStreams, RepartitionStreams, GatherStreams,
    
    Select, Update, Delete, TSQLIcon,
    
    WarningIcon, ParallelIcon, IconNotFound;

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
        } else if(this == Select){
        	return "Select.gif";
        }else if(this == Update){
        	return "Update.gif";
        }else if(this == WarningIcon){
        	return "Warning";
        }else if(this == ParallelIcon){
        	return "Parallel";
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
        } else if(this == Select){
        	return "Select";
        } else if(this == Update){
        	return "Update";
        }else if(this == KeyLookup){
        	return "Key Lookup";
        }else if(this == Delete){
        	return "Delete";
        }else if(this == WarningIcon){
        	return "Warning";
        }else if(this == ParallelIcon){
        	return "Parallel";
        }
        return "";
    }
    
    public String getImagePosition(){
    	if (this == ComputeScalar) {
            return "-104px -8px";
        } else if (this == TableSpool) {
            return "-56px -56px";
        } else if (this == Sequence) {
            return "-728px -8px";
        } else if (this == TableValuedFunction) {
            return "-152px -56px";
        } else if (this == TableInsert) {
            return "-968px -8px";
        } else if (this == Top) {
            return "-200px -56px";
        } else if (this == Filter) {
            return "-248px -8px";
        } else if (this == SequenceProject) {
            return "-776px -8px";
        } else if (this == Segment) {
            return "-632px -8px";
        } else if (this == RIDLookup) {
            return "-584px -8px";
        } else if (this == Concatenation) {
            return "-152px -8px";
        } else if (this == TableDelete) {
            return "-920px -8px";
        } else if (this == TableUpdate) {
            return "-104px -56px";
        } else if (this == StreamAggregate) {
            return "-872px -8px";
        } else if (this == TableScan) {
            return "-8px -56px";
        } else if (this == Sort) {
            return "-824px -8px";
        } else if (this == NestedLoops) {
            return "-488px -8px";
        } else if (this == Merge) {
            return "-440px -8px";
        } else if (this == HashMatch) {
            return "-344px -8px";
        } else if (this == ClusteredIndexScan) {
            return "-8px -8px";
        } else if (this == ClusteredIndexSeek) {
            return "-56px -8px";
        } else if (this == IndexSeek) {
            return "-392px -8px";
        } else if (this == DistributeStreams) {
            return "-200px -8px";
        } else if (this == RepartitionStreams) {
            return "-536px -8px";
        } else if (this == GatherStreams) {
            return "-296px -8px";
        } else if(this == Select){
        	return "-680px -8px";
        }else if(this == Update){
        	return "-248px -56px";
        }else if(this == IconNotFound){
        	return "-296px -56px";
        }else if(this == KeyLookup){
        	return "-344px -56px";
        }else if(this == TSQLIcon){
        	return "-248px -56px";
        }else if(this == WarningIcon){
        	return "-392px -56px";
        }else if(this == ParallelIcon){
        	return "-424px -56px";
        }
    	return "";
    }
}
