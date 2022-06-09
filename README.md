# Multi-Agent Pathfinding Framework

This framework provides implementations of various planners for solving multi-agent pathfinding problems on directed and undirected graphs.
Furthermore, it includes capabilites to evaluate the solutions based on computation time, makespan and sum of costs. 
It is specifically desgined to evaluate the usefulness of the graph direction concept: directing the undirected graph of a multi-agent pathfinding problem
and solving it on the directed graph where the branching factor of a search may be smaller. Hence, the framework allows to compare the performance
of a planner when solving a given problem on an undirected graph with the performance of the same planner when solving the same problem on a directed graph.



# Planners

The framework supports the following planners.

## Cooperative A*
Reservation based approach by Silver [1].

## Token Passing
Planner for multi-agent-pickup-and-delivery-problems proposed by Ma et al. [2].

## Enhanced Hierarchical Multi-Agent-Path-Planner
Planner implementing regional conflict solving building on a proposal by Zhang et al. [3].

## Runtime Replanner
Algorithm that solves conflicts during the execution of plans. Closely related to Flow Annotated Replanning presented by Wang and Botea [4].

## Alternating Runtime Replanner
Variant of the Runtime Replanner that can handle dynamically changing edge directions in directed graphs.

## Traffic Planner
Decentralized approach with waiting instead of replanning.



# Parameters

The framework requires all of the following parameters in the given order.

## 1. Algorithm
"CA_STAR", "TokenPassing", "EnhancedHierarchicalPlanner", "RuntimeReplanner", "AlternatingRuntimeReplanner" or "TrafficSimulator".

## 2. Input path
Path to a folder of files with multi-agent-pathfinding or multi-agent-pickup-and-delivery problems. Make sure there are no other files in this directory.

## 3. Output path
Path to a folder that will store a solution file for each problem as well as a single evaluation .csv file that compares runtime, makespan and sum of costs.

## 4. Time horizon
Limits the plan length. If no solution with a shorter or equal plan length is found, the process terminates unsuccessfully.

## 5. Runtime limit
Limits the time in seconds a planner is allowed to take to adress a single given problem in a single variant.
Thus, if multiple variants (undirected graph, directed graph, dynamic graph) are requested, the planner is granted the given time multiple times.

## 6. Trial limit
Cooperative A* may succeed or fail depending on the order in which the planner plans for the individual agents.
This parameter defines how often the planner may be executed, each time with a different order.
Once it succeeds, the remaining trials are not executed anymore.

## 7. Direction change rate
Directed edges can change their direction periodically. This parameter defines after how many time steps these changes occur.
Choose 0 for a static graph without any edge direction changes.



# Input File Syntax

Input files contain a header defining whether it's a regular multi-agent-pathfinding (YamlClassicScenario) or multi-agent-pickup-and-delivery problem (YamlDynamicScenario).

## Multi-agent-pathfinding

Check /testCases/Classic_03_Hierarchical.yaml for the syntax of a file with a classic multi-agent-pathfinding problem.

It contains a number of agents each of which has a goal position given by x- and y coordinates, a name and an equally defined start position.

The map has a size given by its x- and y-dimensions. Coordinates start with 0. A map of size [7, 5] has x-coordinates from 0 to 6 and y-coordinates from 0 to 4.
Optionally, obstacles can be defined which are inaccessible positions. These are only relevant if the solution is to be visually simulated and are not relevant
for the planners since they only use existing edges anyway and there can't be edges to inaccessible positions.
Finally, the map which is modelled by a graph has edges each of which is defined as [x1, y1, x2, y2] with the edge being directey from positiong (x1, y1) to (x2, y2).


## Multi-agent-pickup-and-delivery

Check /testCases/Dynamic_07.yaml for the syntax of a file with a multi-agent-pickup-and-delivery problem.

Here, agents only have a name and a start position but not specific goal position.

Tasks have an availability time which is the time when the task comes up
and 4 coordinates (px, py, dx, dy) defining the x and y coordinate of the pickup-location (px, py) and the coordinates of the delivery-location (dx, dy).

The representation of the map is identical with the exception that it may also have  several dedicated parking spots given by x- and y-coordinates.



# Literature

[1] D. Silver, “Cooperative pathfinding,” in Proceedings of the First AAAI Conference
on Artificial Intelligence and Interactive Digital Entertainment, pp. 117 –
122, AAAI Press, 2005.

[2] H. Ma, J. Li, T. K. S. Kumar, and S. Koenig, “Lifelong multi-agent path finding
for online pickup and delivery tasks,” in AAMAS ’17: Proceedings of the 16th
Conference on Autonomous Agents and MultiAgent Systems, pp. 837—-845,
2017.

[3] H. Zhang, M. Yao, Z. Liu, J. Li, L. Terr, S.-H. Chan, T. K. S. Kumar, and
S. Koenig, “A hierarchical approach to multi-agent path finding,” in Proceedings
of the 4th ICAPS Workshop on Hierarchical Planning (HPlan 2021), pp. 1–7,
2021.

[4] K.-H. Wang and A. Botea, “Fast and memory-efficient multi-agent pathfinding,”
in ICAPS 2008 - Proceedings of the 18th International Conference on Automated
Planning and Scheduling, pp. 380–387, 2008.
