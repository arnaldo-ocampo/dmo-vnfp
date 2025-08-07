# DMO-VNFP: Dynamic Multi-Objective Virtual Network Function Placement

## Project Overview

This is an academic/research project titled **DMO-VNFP (Dynamic Multi-Objective Virtual Network Function Placement)**, developed at FIUNI (Universidad Nacional de Itapúa - Facultad de Ingeniería) as part of a CONACYT research grant (PINV18-1719).

## Core Purpose

The project addresses the complex problem of optimally placing Virtual Network Functions (VNFs) in network infrastructures while considering multiple conflicting objectives and dynamic network conditions.

## Key Components

### 1. Multi-Objective Optimization
The system uses several metaheuristic algorithms to solve placement problems:
- **NSGA-II variants** (DNSGAII-A, DNSGAII-B)
- **NSGA-III**
- **MOEA/D** (Multi-Objective Evolutionary Algorithm based on Decomposition)
- **RVEA** (Reference Vector guided Evolutionary Algorithm)

These are implemented using the MOEAFramework library.

### 2. Objective Functions
The system optimizes multiple conflicting objectives simultaneously, defined in `SceneObjectiveFunctions`:

**Normal Network Conditions:**
- **Delay** - Network latency
- **Link Maximum Usage** - Network congestion
- **VNF Instances** - Resource efficiency
- **Distance** - Physical path length
- **Energy Cost** - Power consumption
- **Resources Cost** - Computing resource usage

**Different objective sets** are used for various network conditions (Normal, Overloaded, Error, ErrorOverloaded).

### 3. Dynamic Network Management
The system handles changing network conditions through:
- **Traffic variation** - Dynamic traffic patterns
- **Network condition changes** - Normal, overloaded, error states
- **Real-time adaptation** - Algorithms adapt to environmental changes

### 4. Decision Making System
The `MCDMService` implements **TOPSIS (Technique for Order Preference by Similarity to Ideal Solution)** for selecting optimal solutions from Pareto fronts.

### 5. Main Services

- **`DMOPService`** - Core dynamic placement service
- **`SMOPService`** - Static placement service
- **`SolutionService`** - Solution management and CSV generation
- **`ObjectiveFunctionService`** - Objective function calculations
- **`TrafficService`** - Traffic pattern management

### 6. Visualization and Analysis
- **`GraphPlottingService`** - Network topology visualization using GraphStream
- **Performance analysis** - Statistical analysis of algorithm performance
- **CSV generation** - Data export for external analysis tools

## Technical Architecture

### Entry Points:
- **`ManualDynamicPlacement`** - Main execution class for dynamic scenarios
- **`GraphPlottingTest`** - Network visualization testing
- **`Testing`** - Algorithm testing and experimentation

### Data Management:
- **Network topology** - Nodes, links, and server configurations
- **Traffic patterns** - SFC (Service Function Chain) requests
- **VNF specifications** - Virtual function requirements
- **Solution storage** - Results persistence and analysis

## Research Context

This appears to be a **PhD/Master's thesis project or research paper** focusing on:

1. **Network Functions Virtualization (NFV)** optimization
2. **Multi-objective evolutionary algorithms** application
3. **Dynamic network management** strategies
4. **Performance comparison** of different metaheuristic approaches

## Simulation and Results

The Simulation folder contains:
- **Python plotting tools** (`ploter.py`, `group_box.py`)
- **Experimental results** in compressed CSV files
- **Chart generation** capabilities for research publication

## Technologies Used

- **Java 8+** - Core implementation
- **Maven** - Build management (`pom.xml`)
- **MOEAFramework 2.13** - Multi-objective optimization
- **GraphStream 2.0** - Graph visualization
- **Apache Log4j** - Logging
- **Gson** - JSON processing
- **JGraphT** - Graph algorithms
- **Python** - Results analysis and plotting

## Conclusion

This is a sophisticated research project that contributes to the field of **network optimization** and **evolutionary computation**, specifically addressing real-world challenges in modern virtualized network infrastructures.
