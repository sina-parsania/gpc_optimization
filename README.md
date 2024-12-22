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
│   └── job_scheduling_dataset.json   # Input dataset for tasks
├── lib/                         # Required .jar files (CloudSim, Jackson)
├── README.md                    # Documentation
```

---

## **Input**

The input dataset is a JSON file (`job_scheduling_dataset.json`) located in the `resources` directory. Each task in the dataset has the following fields:

```json
{
  "Job Number": 1,
  "Submit Time": 102,
  "Wait Time": 276,
  "Run Time": 940,
  "Number of Allocated Processors": 54,
  "Average CPU Time Used": 176.4205293593,
  "Used Memory": 7934,
  "Requested Number of Processors": 7,
  "Requested Time": 732,
  "Requested Memory": 6196,
  "Status": "Completed",
  "User ID": 1011,
  "Group ID": 279,
  "Executable Number": 30,
  "Queue Number": 7,
  "Partition Number": 4,
  "Preceding Job Number": 14,
  "Think Time from Preceding Job": 79.5186194769
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
   - Place the `job_scheduling_dataset.json` file in the `resources` folder (outside `src`).
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
