include "globals.mzn";

% input 1
int: graph_size = 6;
int: start = 1;
int: n_dests = 1;
array[int] of int: dest = [6];
int: n_edges = 7;
array[int] of int: from = [1,1,2,2,3,4,4];
array[int] of int: to =   [2,3,3,4,5,5,6];
array[int] of int: cost = [4,2,5,10,3,4,11];

set of int: edge = 1..n_edges;
array[edge] of var int: edges;

array[1..n_dests, 1..graph_size] of var 1..graph_size: graph;

% set possible nodes to go to
constraint forall(k in 1..n_dests, i in 1..graph_size)(graph[k,i] in { to[j] | j in 1..n_edges where from[j] = i } union { from[j] | j in 1..n_edges where to[j] = i } union {i});

% start may not go to itself
constraint forall(k in 1..n_dests)(graph[k, start] != start);

% set edge between start and end
constraint forall(i in 1..n_dests)(graph[i, dest[i]] in { graph[i, dest[i]] } union {start} );

% gör subcircuit för varje lösning
constraint forall( i in 1..n_dests )( subcircuit([graph[i,j] | j in 1..graph_size]));

Define cost
array[1..n_dests] of var 0..50: global_cost;
constraint forall( a in 1..n_dests, i in 1..graph_size, j in 1..n_edges )( global_cost[a] = cost[j] );

% (from[e] = i /\ to[e] = j) \/ ( from[e] = j /\ to[e] = i)

% Define cost for edge for every node
array[int, int] of int: edge_cost = array2d(1..graph_size, 1..graph_size, [ if exists(e in 1..n_edges)( (from[e] = i /\ to[e] = j) \/ ( from[e] = j /\ to[e] = i) )  then [ cost[e] | e  in 1..n_edges where (from[e] = i /\ to[e] = j) \/ ( from[e] = j /\ to[e] = i) ][1] else 0 endif | i,j in 1..graph_size] );


% constraint forall(i in 1..graph_size, j in 1..graph_size)( edge_cost[i, j] in { cost[e] | e in 1..n_edges where from[e] = i /\ to[e] = j} );

solve satisfy;