package com.hibernateassist.database;

/**
 * @author vicky.thakor
 */
public enum ExecutionPlanEnum {

    AvgRowSize,
    EstimateCPU,
    EstimateIO,
    EstimateOperatorCost,
    EstimateRebinds,
    EstimateRewinds,
    EstimateRows,
    LogicalOp,
    NodeId,
    Parallel,
    PhysicalOp,
    EstimatedTotalSubtreeCost
}
