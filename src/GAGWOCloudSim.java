import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;

import java.io.File;
import java.util.*;

public class GAGWOCloudSim {

    private static final int nTasks = 30; // Number of tasks
    private static final int nVMs = 5; // Number of virtual machines
    private static final int MaxIteration = 1000; // Maximum number of iterations
    private static final int nPop = 20; // Population size

    static class Solution {
        int[] position; // Task allocation to VMs
        double fitness;

        Solution(int nTasks) {
            position = new int[nTasks];
            fitness = Double.POSITIVE_INFINITY;
        }
    }

    static class Task {
        @JsonProperty("Job Number")
        public int jobNumber;

        @JsonProperty("Submit Time")
        public int submitTime;

        @JsonProperty("Wait Time")
        public int waitTime;

        @JsonProperty("Run Time")
        public int runTime;

        @JsonProperty("Number of Allocated Processors")
        public int numberOfAllocatedProcessors;

        @JsonProperty("Average CPU Time Used")
        public double averageCpuTimeUsed;

        @JsonProperty("Used Memory")
        public int usedMemory;

        @JsonProperty("Requested Number of Processors")
        public int requestedNumberOfProcessors;

        @JsonProperty("Requested Time")
        public int requestedTime;

        @JsonProperty("Requested Memory")
        public int requestedMemory;

        @JsonProperty("Status")
        public String status;

        @JsonProperty("User ID")
        public int userId;

        @JsonProperty("Group ID")
        public int groupId;

        @JsonProperty("Executable Number")
        public int executableNumber;

        @JsonProperty("Queue Number")
        public int queueNumber;

        @JsonProperty("Partition Number")
        public int partitionNumber;

        @JsonProperty("Preceding Job Number")
        public int precedingJobNumber;

        @JsonProperty("Think Time from Preceding Job")
        public double thinkTimeFromPrecedingJob;
    }

    public static void main(String[] args) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);
            Datacenter datacenter = createDatacenter();
            DatacenterBroker broker = new DatacenterBroker("Broker");
            List<Vm> vmList = createVMs(broker.getId(), nVMs);
            broker.submitVmList(vmList);

            Task[] tasks = loadTasks("resources/job_scheduling_dataset.json");
            List<Cloudlet> cloudletList = createCloudlets(broker.getId(), tasks);
            broker.submitCloudletList(cloudletList);

            Solution bestSolution = runGAGWO(tasks);

            double makespan = calculateMakespan(bestSolution.position, tasks);
            double utilization = calculateUtilization(bestSolution.position, tasks, nVMs);
            double loadBalancing = calculateLoadBalancing(bestSolution.position, tasks, nVMs);

            System.out.printf("Makespan: %.2f%n", makespan);
            System.out.printf("Utilization: %.2f%n", utilization);
            System.out.printf("Load Balancing: %.2f%n", loadBalancing);

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < nVMs; i++) {
            List<Pe> peList = new ArrayList<>();
            peList.add(new Pe(0, new PeProvisionerSimple(1000))); // Number of processors

            hostList.add(new Host(
                    i,
                    new RamProvisionerSimple(20480), // Minimum 20 GB RAM per host
                    new BwProvisionerSimple(10000), // Minimum 10 Gbps Bandwidth
                    1000000, // Minimum 1 TB Storage
                    peList,
                    new VmSchedulerTimeShared(peList)));
        }
        try {
            return new Datacenter(
                    "Datacenter",
                    new DatacenterCharacteristics(
                            "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.1, 0.1),
                    new VmAllocationPolicySimple(hostList),
                    new ArrayList<>(),
                    0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Vm> createVMs(int brokerId, int vmCount) {
        List<Vm> vms = new ArrayList<>();
        for (int i = 0; i < vmCount; i++) {
            vms.add(new Vm(i, brokerId, 1000, 1, 2048, 1000, 10000, "Xen", new CloudletSchedulerTimeShared()));
        }
        return vms;
    }

    private static Task[] loadTasks(String filePath) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), Task[].class);
    }

    private static List<Cloudlet> createCloudlets(int brokerId, Task[] tasks) {
        List<Cloudlet> cloudlets = new ArrayList<>();
        for (Task task : tasks) {
            Cloudlet cloudlet = new Cloudlet(
                    task.jobNumber,
                    task.runTime,
                    task.requestedNumberOfProcessors,
                    300,
                    300,
                    new UtilizationModelFull(),
                    new UtilizationModelFull(),
                    new UtilizationModelFull());
            cloudlet.setUserId(brokerId); // Assign Cloudlet to user
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }

    private static Solution runGAGWO(Task[] tasks) {
        Random rand = new Random();
        Solution[] population = new Solution[nPop];
        Solution bestSolution = new Solution(nTasks);

        // Initialize Population
        for (int i = 0; i < nPop; i++) {
            population[i] = new Solution(nTasks);
            for (int j = 0; j < nTasks; j++) {
                population[i].position[j] = rand.nextInt(nVMs);
            }
            population[i].fitness = calculateMakespan(population[i].position, tasks);
            if (population[i].fitness < bestSolution.fitness) {
                bestSolution = population[i];
            }
        }

        // Main Loop for GA-GWO
        for (int iter = 0; iter < MaxIteration; iter++) {
            Arrays.sort(population, Comparator.comparingDouble(o -> o.fitness));

            for (int i = 0; i < nPop; i++) {
                double a = 2.0 - (2.0 * iter / MaxIteration);

                for (int j = 0; j < nTasks; j++) {
                    // GWO Position Update
                    double A1 = 2 * a * rand.nextDouble() - a;
                    double C1 = 2 * rand.nextDouble();
                    double D_alpha = Math.abs(C1 * population[0].position[j] - population[i].position[j]);
                    double X1 = population[0].position[j] - A1 * D_alpha;

                    // Apply Crossover and Mutation (GA)
                    if (rand.nextDouble() < 0.5) {
                        X1 = (population[i].position[j] + population[1].position[j]) / 2;
                    }
                    if (rand.nextDouble() < 0.1) {
                        X1 += rand.nextGaussian();
                    }

                    // Update Position
                    population[i].position[j] = (int) Math.round(X1);
                    population[i].position[j] = Math.max(0, Math.min(nVMs - 1, population[i].position[j]));
                }

                population[i].fitness = calculateMakespan(population[i].position, tasks);
                if (population[i].fitness < bestSolution.fitness) {
                    bestSolution = population[i];
                }
            }
        }

        return bestSolution;
    }

    private static double calculateMakespan(int[] assignment, Task[] tasks) {
        double[] vmTimes = new double[nVMs];
        for (int i = 0; i < assignment.length; i++) {
            vmTimes[assignment[i]] += tasks[i].runTime;
        }
        return Arrays.stream(vmTimes).max().orElse(0);
    }

    private static double calculateUtilization(int[] assignment, Task[] tasks, int numVMs) {
        double totalRunTime = Arrays.stream(tasks).mapToDouble(task -> task.runTime).sum();
        double makespan = calculateMakespan(assignment, tasks);
        return totalRunTime / (makespan * numVMs);
    }

    private static double calculateLoadBalancing(int[] assignment, Task[] tasks, int numVMs) {
        double[] vmTimes = new double[numVMs];
        for (int i = 0; i < assignment.length; i++) {
            vmTimes[assignment[i]] += tasks[i].runTime;
        }
        double meanLoad = Arrays.stream(vmTimes).average().orElse(0);
        return (Arrays.stream(vmTimes).max().orElse(0) - Arrays.stream(vmTimes).min().orElse(0)) / meanLoad;
    }
}