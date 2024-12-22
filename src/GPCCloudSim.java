import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public class GPCCloudSim {

    private static final int nTasks = 30; // Number of tasks
    private static final int nVMs = 5; // Number of virtual machines
    private static final int MaxIteration = 1000; // Maximum number of iterations
    private static final int nPop = 20; // Number of workers (population)

    static class Stone {
        int[] position; // Task allocation to machines
        double cost;

        Stone(int nTasks) {
            position = new int[nTasks];
            cost = Double.POSITIVE_INFINITY;
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
            // 1. Initialize CloudSim
            CloudSim.init(1, Calendar.getInstance(), false);

            // 2. Create Datacenter and VMs
            Datacenter datacenter = createDatacenter();

            // System.out.println("List of Hosts in Datacenter:");
            // for (Host host : datacenter.getHostList()) {
            // System.out.println(
            // "Host #" + host.getId() + " with RAM: " +
            // host.getRamProvisioner().getAvailableRam() + " MB");
            // }

            DatacenterBroker broker = new DatacenterBroker("Broker");
            List<Vm> vmList = createVMs(broker.getId(), nVMs);
            broker.submitVmList(vmList);

            // System.out.println("List of VMs:");
            // for (Vm vm : vmList) {
            // System.out.println("VM #" + vm.getId() + " with RAM: " + vm.getRam() + "
            // MB");
            // }

            // 3. Load dataset and create Cloudlets
            ObjectMapper objectMapper = new ObjectMapper();
            Task[] tasks = objectMapper.readValue(new File("resources/job_scheduling_dataset.json"), Task[].class);
            List<Cloudlet> cloudletList = createCloudlets(broker.getId(), tasks);
            broker.submitCloudletList(cloudletList);

            // 4. Run GPC Algorithm
            Stone bestWorker = runGPC(tasks);

            // 5. Calculate Metrics
            double makespan = calculateMakespan(bestWorker.position, tasks);
            double utilization = calculateUtilization(bestWorker.position, tasks, nVMs);
            double loadBalancing = calculateLoadBalancing(bestWorker.position, tasks, nVMs);

            System.out.printf("Makespan: %.2f%n", makespan);
            System.out.printf("Utilization: %.2f%n", utilization);
            System.out.printf("Load Balancing: %.2f%n", loadBalancing);

            // 6. Start Simulation
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < nVMs; i++) { // Number of hosts equal to nVMs
            List<Pe> peList = new ArrayList<>();
            peList.add(new Pe(0, new PeProvisionerSimple(1000))); // Number of processing elements (CPUs)

            int ram = 20480; // Minimum 20 GB RAM per host
            long storage = 1000000; // Minimum 1 TB storage
            int bw = 10000; // Minimum 10 Gbps bandwidth

            hostList.add(new Host(
                    i,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
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
            cloudlet.setUserId(brokerId); // Assign Cloudlet to the user
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }

    private static Stone runGPC(Task[] tasks) {
        Random rand = new Random();
        Stone[] pop = new Stone[nPop];
        Stone bestWorker = new Stone(nTasks);

        for (int i = 0; i < nPop; i++) {
            pop[i] = new Stone(nTasks);
            for (int j = 0; j < nTasks; j++) {
                pop[i].position[j] = rand.nextInt(nVMs);
            }
            pop[i].cost = calculateMakespan(pop[i].position, tasks);
            if (pop[i].cost < bestWorker.cost) {
                bestWorker = pop[i];
            }
        }
        return bestWorker;
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