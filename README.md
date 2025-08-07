# DMO-VNFP - Algoritmos de Decisión Multicriterio

Este proyecto implementa un conjunto de algoritmos de decisión multicriterio para la optimización y selección de soluciones en problemas complejos. Los algoritmos están diseñados para evaluar alternativas basándose en múltiples criterios con diferentes pesos e importancias.

## Tabla de Contenidos

1. [Algoritmos Implementados](#algoritmos-implementados)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Guía de Uso](#guía-de-uso)
4. [Ejemplos Prácticos](#ejemplos-prácticos)
5. [Comparación de Algoritmos](#comparación-de-algoritmos)
6. [Datos de Ejemplo](#datos-de-ejemplo)

## Algoritmos Implementados

### 1. TOPSIS (Technique for Order of Preference by Similarity to Ideal Solution)

**Descripción**: TOPSIS evalúa alternativas calculando su distancia tanto de la solución ideal positiva como de la solución ideal negativa. La mejor alternativa es aquella que está más cerca de la solución ideal positiva y más lejos de la solución ideal negativa.

**Ventajas**:
- Fácil de entender e implementar
- Utiliza información de todas las alternativas
- Proporciona un ranking completo de alternativas
- No requiere información adicional sobre las preferencias del decisor

**Desventajas**:
- Sensible a la normalización utilizada
- Puede dar resultados contraintuitivos en algunos casos
- Asume que todos los criterios son independientes

**Cuándo usar**: Ideal para problemas donde se necesita un ranking completo de alternativas y los criterios son independientes.

### 2. SAW (Simple Additive Weighting)

**Descripción**: SAW es el método más simple de decisión multicriterio. Calcula una puntuación ponderada para cada alternativa sumando los valores normalizados multiplicados por los pesos de los criterios.

**Ventajas**:
- Muy simple de entender y calcular
- Computacionalmente eficiente
- Transparente en el proceso de decisión
- Fácil de comunicar a los stakeholders

**Desventajas**:
- Asume compensación perfecta entre criterios
- Sensible a la escala de los criterios
- No considera interdependencias entre criterios
- Puede ser demasiado simplista para problemas complejos

**Cuándo usar**: Apropiado para problemas simples donde la compensación entre criterios es aceptable y se requiere una solución rápida.

### 3. AHP (Analytic Hierarchy Process)

**Descripción**: AHP utiliza comparaciones por pares para determinar la importancia relativa de los criterios y evaluar las alternativas. Incluye un mecanismo para verificar la consistencia de las comparaciones.

**Ventajas**:
- Maneja tanto aspectos cuantitativos como cualitativos
- Verifica la consistencia de las preferencias
- Permite estructurar problemas complejos jerárquicamente
- Ampliamente aceptado y utilizado

**Desventajas**:
- Requiere muchas comparaciones por pares (n²-n)/2
- Puede ser subjetivo en las comparaciones
- Sensible a cambios en la estructura jerárquica
- Complejidad aumenta rápidamente con el número de criterios

**Cuándo usar**: Excelente para problemas complejos con múltiples niveles de criterios y cuando se necesita validar la consistencia de las preferencias.

### 4. PROMETHEE (Preference Ranking Organization Method for Enrichment Evaluations)

**Descripción**: PROMETHEE utiliza funciones de preferencia para cada criterio y calcula flujos de entrada y salida para determinar el ranking de alternativas.

**Ventajas**:
- Flexible en el manejo de diferentes tipos de criterios
- Proporciona información detallada sobre las preferencias
- Maneja bien la incomparabilidad entre alternativas
- Permite diferentes funciones de preferencia

**Desventajas**:
- Más complejo de entender que otros métodos
- Requiere definir funciones de preferencia
- Puede requerir parámetros adicionales
- La selección de funciones de preferencia puede ser subjetiva

**Cuándo usar**: Ideal para problemas complejos donde las relaciones de preferencia entre criterios son importantes y se necesita un análisis detallado.

## Estructura del Proyecto

```
Code/DMOP/src/main/java/py/edu/fiuni/dmop/decision/
├── DecisionMaker.java              # Clase base abstracta
├── DecisionMakerFactory.java       # Factory para crear instancias
├── DecisionMakerException.java     # Excepciones personalizadas
├── ahp/
│   └── AHP.java                   # Implementación AHP
├── saw/
│   └── SAW.java                   # Implementación SAW
├── topsis/
│   ├── Topsis.java                # Implementación TOPSIS
│   ├── Alternative.java           # Clase para alternativas
│   ├── Criteria.java              # Clase para criterios
│   └── CriteriaValue.java         # Clase para valores de criterios
├── promethee/
│   └── PROMETHEE.java             # Implementación PROMETHEE
└── test/
    ├── AHPTest.java               # Ejemplo de uso AHP
    ├── SAWTest.java               # Ejemplo de uso SAW
    ├── TopsisTest.java            # Ejemplo de uso TOPSIS
    └── PROMETHEETest.java         # Ejemplo de uso PROMETHEE
```

## Guía de Uso

### Estructura de Datos Básica

#### 1. Definir Criterios

```java
// Para criterios donde mayor valor es mejor (ej: calidad)
Criteria quality = new Criteria("Quality", 0.5, false);

// Para criterios donde menor valor es mejor (ej: costo)
Criteria cost = new Criteria("Cost", 0.3, true);

// Criterio con peso automático (se calculará igual importancia)
Criteria time = new Criteria("Time", 0.0);
```

#### 2. Crear Alternativas

```java
Alternative supplier1 = new Alternative("Supplier A");
supplier1.addCriteriaValue(cost, 500.0);
supplier1.addCriteriaValue(quality, 8.0);
supplier1.addCriteriaValue(time, 3.0);
```

#### 3. Usar el Factory Pattern

```java
// Usando el factory para crear instancias
DecisionMaker dm = DecisionMakerFactory.getDecisionMaker("topsis", alternatives, criteria);
Alternative best = dm.calculateOptimalSolution();
```

### Uso Individual de Cada Algoritmo

#### TOPSIS
```java
Topsis topsis = new Topsis();
for (Alternative alt : alternatives) {
    topsis.addAlternative(alt);
}
Alternative best = topsis.calculateOptimalSolution();
```

#### SAW
```java
SAW saw = new SAW(alternatives, criteria);
Alternative best = saw.calculateOptimalSolution();
```

#### AHP
```java
AHP ahp = new AHP(alternatives, criteria);

// Definir matriz de comparación por pares
double[][] comparisonMatrix = {
    {1.0, 2.0, 0.5},  // Cost vs otros
    {0.5, 1.0, 0.33}, // Quality vs otros  
    {2.0, 3.0, 1.0}   // Time vs otros
};
ahp.setPairwiseComparisonMatrix(comparisonMatrix);

Alternative best = ahp.calculateOptimalSolution();

// Verificar consistencia
if (ahp.isConsistent()) {
    System.out.println("CR: " + ahp.getConsistencyRatio());
}
```

#### PROMETHEE
```java
double[] weights = {0.3, 0.5, 0.2}; // Pesos para cada criterio
PROMETHEE promethee = new PROMETHEE(alternatives, criteria, weights);
Alternative best = promethee.calculateOptimalSolution();
```

## Ejemplos Prácticos

### Ejemplo 1: Selección de Proveedores con TOPSIS

```java
import py.edu.fiuni.dmop.decision.topsis.*;
import java.util.*;

public class SupplierSelectionExample {
    public static void main(String[] args) {
        // Definir criterios
        List<Criteria> criteria = Arrays.asList(
            new Criteria("Costo", 0.3, true),      // Menor es mejor
            new Criteria("Calidad", 0.4, false),   // Mayor es mejor  
            new Criteria("Tiempo", 0.2, true),     // Menor es mejor
            new Criteria("Servicio", 0.1, false)   // Mayor es mejor
        );

        // Crear alternativas
        List<Alternative> suppliers = new ArrayList<>();
        
        Alternative supplier1 = new Alternative("Proveedor A");
        supplier1.addCriteriaValue(criteria.get(0), 1000); // Costo
        supplier1.addCriteriaValue(criteria.get(1), 85);   // Calidad
        supplier1.addCriteriaValue(criteria.get(2), 5);    // Tiempo
        supplier1.addCriteriaValue(criteria.get(3), 90);   // Servicio
        suppliers.add(supplier1);

        Alternative supplier2 = new Alternative("Proveedor B");
        supplier2.addCriteriaValue(criteria.get(0), 1200);
        supplier2.addCriteriaValue(criteria.get(1), 90);
        supplier2.addCriteriaValue(criteria.get(2), 3);
        supplier2.addCriteriaValue(criteria.get(3), 85);
        suppliers.add(supplier2);

        // Ejecutar TOPSIS
        Topsis topsis = new Topsis(suppliers);
        try {
            Alternative best = topsis.calculateOptimalSolution();
            System.out.println("Mejor proveedor: " + best.getName());
            System.out.println("Puntuación: " + best.getCalculatedPerformanceScore());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Ejemplo 2: Evaluación de Tecnologías con AHP

```java
public class TechnologyEvaluationExample {
    public static void main(String[] args) {
        // Criterios para evaluación de tecnologías
        List<Criteria> criteria = Arrays.asList(
            new Criteria("Costo", 0.0),        // Peso se calculará automáticamente
            new Criteria("Rendimiento", 0.0),
            new Criteria("Escalabilidad", 0.0),
            new Criteria("Mantenimiento", 0.0)
        );

        // Alternativas tecnológicas
        List<Alternative> technologies = new ArrayList<>();
        
        Alternative tech1 = new Alternative("Tecnología A");
        tech1.addCriteriaValue(criteria.get(0), 50000);  // Costo
        tech1.addCriteriaValue(criteria.get(1), 85);     // Rendimiento
        tech1.addCriteriaValue(criteria.get(2), 7);      // Escalabilidad
        tech1.addCriteriaValue(criteria.get(3), 6);      // Mantenimiento
        technologies.add(tech1);

        // ... agregar más tecnologías

        AHP ahp = new AHP(technologies, criteria);
        
        // Matriz de comparación (ejemplo)
        double[][] matrix = {
            {1.0, 0.5, 2.0, 3.0},   // Costo
            {2.0, 1.0, 3.0, 4.0},   // Rendimiento
            {0.5, 0.33, 1.0, 2.0},  // Escalabilidad  
            {0.33, 0.25, 0.5, 1.0}  // Mantenimiento
        };
        ahp.setPairwiseComparisonMatrix(matrix);

        try {
            Alternative best = ahp.calculateOptimalSolution();
            
            if (ahp.isConsistent()) {
                System.out.println("Mejor tecnología: " + best.getName());
                System.out.println("Ratio de Consistencia: " + 
                    String.format("%.4f", ahp.getConsistencyRatio()));
            } else {
                System.out.println("Advertencia: Comparaciones inconsistentes!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

## Comparación de Algoritmos

| Aspecto | TOPSIS | SAW | AHP | PROMETHEE |
|---------|--------|-----|-----|-----------|
| **Complejidad Conceptual** | Media | Baja | Alta | Alta |
| **Complejidad Computacional** | O(mn) | O(mn) | O(n³) | O(mn²) |
| **Datos Requeridos** | Matriz + Pesos | Matriz + Pesos | Matriz + Comparaciones | Matriz + Pesos + Funciones |
| **Manejo de Inconsistencias** | No | No | Sí | Parcial |
| **Ranking Completo** | Sí | Sí | Sí | Sí |
| **Facilidad de Implementación** | Media | Alta | Baja | Media |
| **Transparencia** | Media | Alta | Media | Baja |
| **Escalabilidad** | Buena | Excelente | Limitada | Buena |

**Leyenda**: n = número de alternativas, m = número de criterios

### Recomendaciones de Uso

- **SAW**: Para problemas simples, rápidos, con compensación total entre criterios
- **TOPSIS**: Para problemas generales donde se necesita balance entre solución ideal y anti-ideal
- **AHP**: Para problemas complejos con jerarquías y necesidad de validar consistencia
- **PROMETHEE**: Para problemas complejos con relaciones de preferencia específicas

## Datos de Ejemplo

El proyecto incluye datos de simulación en la carpeta `simulation/results/` que contienen resultados de diferentes algoritmos evolutivos para múltiples ventanas de tiempo. Estos pueden ser utilizados como alternativas en los algoritmos de decisión multicriterio.

### Estructura de los Datos de Simulación

Los archivos CSV contienen resultados para diferentes criterios:

- **Latencia**: Tiempo de respuesta del sistema (menor es mejor)
- **Máxima Carga de Enlace**: Utilización máxima de enlaces de red (menor es mejor)  
- **Número de VNFs**: Cantidad de funciones de red virtuales (menor es mejor)
- **Costo de Energía**: Consumo energético (menor es mejor)
- **Costo de Recursos**: Utilización de recursos computacionales (menor es mejor)
- **Distancia**: Distancia total en la topología (menor es mejor)

### Ejemplo de Uso con Datos Reales

```java
public class SimulationDataExample {
    public static void main(String[] args) {
        // Criterios basados en los datos de simulación
        List<Criteria> criteria = Arrays.asList(
            new Criteria("Latencia", 0.25, true),           // ms - menor es mejor
            new Criteria("Carga_Enlace", 0.20, true),       // % - menor es mejor
            new Criteria("Num_VNFs", 0.15, true),           // count - menor es mejor
            new Criteria("Costo_Energia", 0.20, true),      // kWh - menor es mejor
            new Criteria("Costo_Recursos", 0.15, true),     // CPU/RAM - menor es mejor
            new Criteria("Distancia", 0.05, true)           // km - menor es mejor
        );

        // Alternativas basadas en algoritmos evolutivos (datos de Ventana #1)
        List<Alternative> algorithms = new ArrayList<>();
        
        Alternative dnsgaii_a = new Alternative("DNSGAII-A");
        dnsgaii_a.addCriteriaValue(criteria.get(0), 1167.0);    // Latencia
        dnsgaii_a.addCriteriaValue(criteria.get(1), 0.8);       // Carga estimada
        dnsgaii_a.addCriteriaValue(criteria.get(2), 45);        // VNFs estimadas
        dnsgaii_a.addCriteriaValue(criteria.get(3), 0.7906);    // Energía
        dnsgaii_a.addCriteriaValue(criteria.get(4), 1.2);       // Recursos estimados
        dnsgaii_a.addCriteriaValue(criteria.get(5), 1450.0);    // Distancia estimada
        algorithms.add(dnsgaii_a);

        Alternative moead = new Alternative("MOEAD");
        moead.addCriteriaValue(criteria.get(0), 1090.0);    // Mejor latencia
        moead.addCriteriaValue(criteria.get(1), 0.75);      
        moead.addCriteriaValue(criteria.get(2), 42);        
        moead.addCriteriaValue(criteria.get(3), 0.8075);    
        moead.addCriteriaValue(criteria.get(4), 1.1);       
        moead.addCriteriaValue(criteria.get(5), 1380.0);    
        algorithms.add(moead);

        // Ejecutar diferentes algoritmos de decisión
        System.out.println("=== Comparación de Algoritmos de Decisión ===");
        
        // SAW
        SAW saw = new SAW(algorithms, criteria);
        Alternative sawBest = saw.calculateOptimalSolution();
        System.out.println("SAW - Mejor: " + sawBest.getName() + 
            " (Score: " + String.format("%.4f", sawBest.getCalculatedPerformanceScore()) + ")");

        // TOPSIS
        Topsis topsis = new Topsis(new ArrayList<>(algorithms));
        Alternative topsisBest = topsis.calculateOptimalSolution();
        System.out.println("TOPSIS - Mejor: " + topsisBest.getName() + 
            " (Score: " + String.format("%.4f", topsisBest.getCalculatedPerformanceScore()) + ")");
    }
}
```

## Validación y Consistencia

### Verificación de Consistencia en AHP

```java
// Verificar si las comparaciones por pares son consistentes
if (ahp.getConsistencyRatio() < 0.1) {
    System.out.println("Comparaciones consistentes (CR = " + 
        String.format("%.4f", ahp.getConsistencyRatio()) + ")");
} else {
    System.out.println("ADVERTENCIA: Comparaciones inconsistentes (CR = " + 
        String.format("%.4f", ahp.getConsistencyRatio()) + ")");
    System.out.println("Considere revisar las comparaciones por pares.");
}
```

### Validación de Pesos

```java
// Los pesos se validan automáticamente
// Si todos los pesos son 0, se asignan pesos iguales automáticamente
// Si los pesos no suman 1.0, se lanza una excepción
```

## Notas de Implementación

1. **Normalización**: Todos los algoritmos normalizan automáticamente los valores de los criterios
2. **Pesos**: Si se asignan pesos de 0 a todos los criterios, se calculan automáticamente como iguales
3. **Criterios Negativos**: Use `true` para criterios donde menor es mejor (costo, tiempo, etc.)
4. **Manejo de Errores**: Todas las implementaciones incluyen validación de datos y manejo de excepciones

## Compilación y Ejecución

```bash
# Navegar al directorio del código
cd Code/DMOP

# Compilar
javac -cp ".:lib/*" src/main/java/py/edu/fiuni/dmop/decision/test/*.java

# Ejecutar ejemplos
java -cp ".:src/main/java:lib/*" py.edu.fiuni.dmop.decision.test.TopsisTest
java -cp ".:src/main/java:lib/*" py.edu.fiuni.dmop.decision.test.SAWTest
java -cp ".:src/main/java:lib/*" py.edu.fiuni.dmop.decision.test.AHPTest
```

## Referencias

- Saaty, T.L. (1980). The Analytic Hierarchy Process. McGraw-Hill.
- Hwang, C.L., & Yoon, K. (1981). Multiple Attribute Decision Making. Springer-Verlag.
- Brans, J.P., & Vincke, P. (1985). PROMETHEE: A new family of outranking methods. Operations Research, 3, 477-490.
- Fishburn, P.C. (1967). Additive utilities with incomplete product set. Operations Research, 15, 537-542.

---

**Autores**: Arnaldo Ocampo, Néstor Tapia, Marcelo Ferreira  
**Institución**: Universidad Nacional de Itapúa - Paraguay  
**Proyecto**: DMO-VNFP (Decision Making Optimization for Virtual Network Function Placement)


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

