# CloudSim Scheduling with Hybrid Algorithms

This project provides implementations for **task scheduling in a cloud computing environment** using the CloudSim framework. Two different optimization algorithms are implemented for scheduling tasks to virtual machines (VMs):

1. **GPCCloudSim**: Based on the Giza Pyramids Construction (GPC) algorithm.
2. **GAGWOCloudSim**: A hybrid algorithm combining Genetic Algorithm (GA) and Grey Wolf Optimizer (GWO).

---

## **Features**

### **GPCCloudSim**

- Implements the GPC algorithm for task scheduling.
- Evaluates performance using:
  - **Makespan**: Total time to complete all tasks.
  - **Resource Utilization**: Efficiency of VM resource usage.
  - **Load Balancing**: Distribution of tasks across VMs.

### **GAGWOCloudSim**

- Combines **GA** (mutation and crossover) with **GWO** (leader-follower dynamics).
- Optimizes task allocation to reduce makespan and improve resource utilization and load balancing.

---

## **Prerequisites**

Before running the project, ensure the following:

1. **Java Development Kit (JDK)**: Version 8 or later.
2. **CloudSim**: Version 3.0.3 or compatible `.jar` files included in the project.
3. **Jackson Library**: For JSON parsing:
   - `jackson-core`
   - `jackson-databind`
   - `jackson-annotations`

---

## **Project Structure**

```
.
├── src/
│   ├── GPCCloudSim.java         # GPC-based scheduling
│   ├── GAGWOCloudSim.java       # Hybrid GA-GWO scheduling
├── resources/
│   └── NASA-iPSC-1993-3.1-cln-sample.json   # Input dataset for tasks
├── lib/                         # Required .jar files (CloudSim, Jackson)
├── README.md                    # Documentation
```

---

## **Input**

The input dataset is a JSON file (`NASA-iPSC-1993-3.1-cln-sample.json`) located in the `resources` directory. Each task in the dataset has the following fields:

```json
{
  "Job Number": 1,
  "Submit Time": 0,
  "Wait Time": -1,
  "Run Time": 1451,
  "Number of Allocated Processors": 128,
  "Average CPU Time Used": -1,
  "Used Memory": -1,
  "Requested Number of Processors": -1,
  "Requested Time": -1,
  "Requested Memory": -1,
  "Status": -1,
  "User ID": 1,
  "Group ID": 1,
  "Executable Number": -1,
  "Queue Number": -1,
  "Partition Number": -1,
  "Preceding Job Number": -1,
  "Think Time from Preceding Job": -1
}
```

---

## **Output**

Both programs output three key metrics to the console:

- **Makespan**: Total execution time for all tasks.
- **Utilization**: Efficiency of resource usage.
- **Load Balancing**: Degree of balanced task distribution.

Example Output:

```
Makespan: 3693.00
Utilization: 0.83
Load Balancing: 0.32
```

---

## **How to Run**

1. **Compile the Code**:

   ```bash
   javac -cp "lib/*:src/" -d bin src/<FileName>.java
   ```

2. **Run the Program**:

   ```bash
   java -cp "bin:lib/*:resources/" <MainClass>
   ```

   Replace `<FileName>` and `<MainClass>` with:

   - For **GPCCloudSim**:
     ```bash
     javac -cp "lib/*:src/" -d bin src/GPCCloudSim.java
     java -cp "bin:lib/*:resources/" GPCCloudSim
     ```
   - For **GAGWOCloudSim**:
     ```bash
     javac -cp "lib/*:src/" -d bin src/GAGWOCloudSim.java
     java -cp "bin:lib/*:resources/" GAGWOCloudSim
     ```

3. **Ensure Proper Paths**:
   - Place the `NASA-iPSC-1993-3.1-cln-sample.json` file in the `resources` folder (outside `src`).
   - Include the required `.jar` files in the `lib` folder.

---

## **Key Methods**

### **GPCCloudSim**

- **`runGPC(Task[] tasks)`**:
  Implements the GPC algorithm.
- **`calculateMakespan(int[] assignment, Task[] tasks)`**:
  Computes the total time to complete all tasks.

- **`calculateUtilization(int[] assignment, Task[] tasks, int numVMs)`**:
  Calculates how efficiently the resources are utilized.

- **`calculateLoadBalancing(int[] assignment, Task[] tasks, int numVMs)`**:
  Measures the load balance across VMs.

### **GAGWOCloudSim**

- **`runGAGWO(Task[] tasks)`**:
  Combines GWO’s position updates with GA's crossover and mutation for task allocation.
- **`calculateMakespan(int[] assignment, Task[] tasks)`**:
  Computes the total time to complete all tasks.

- **`calculateUtilization(int[] assignment, Task[] tasks, int numVMs)`**:
  Calculates how efficiently the resources are utilized.

- **`calculateLoadBalancing(int[] assignment, Task[] tasks, int numVMs)`**:
  Measures the load balance across VMs.

---

## **Libraries Used**

- **CloudSim**: For simulating cloud environments.
- **Jackson**: For JSON parsing.

---

## **License**

This project is licensed under the MIT License. See the `LICENSE` file for details.
"""
