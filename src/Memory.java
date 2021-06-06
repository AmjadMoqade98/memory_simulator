import sun.rmi.runtime.Log;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Memory {
    private int totalSize;
    private int freeSize;
    private List<Partition> partitions;
    private static Memory instance;
    private Map<Job, Partition> allocatedJobTable;
    private Map<Partition, Job> allocatedJobTable1;

    static Memory getInstance() {
        if (instance == null) {
            instance = new Memory();
            instance.initiateDefragmentationThread();
            System.out.println("- Memory initialized Successfully");
            instance.dumpMemory("");
        }
        return instance;
    }

    private Memory() {
        this.totalSize = 1024 - 256;
        this.freeSize = totalSize;
        allocatedJobTable = new HashMap<>();
        allocatedJobTable1 = new HashMap<>();
        this.partitions = new CopyOnWriteArrayList<>();
        Partition partition = new Partition(0, freeSize, PartitionStatus.FREE);
        this.partitions.add(partition);
    }

    public int getTotalSize() {
        return totalSize;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public int getFreeSize() {
        return freeSize;
    }

    public Map<Job, Partition> getAllocatedJobTable() {
        return allocatedJobTable;
    }

    public synchronized boolean AllocateJobInMemory(Job job) {
        Partition chosen = firstFit(job.getSize());
        Partition allocatedPartition = null;
        if (chosen != null) {
            if (chosen.getSize() == job.getSize()) {
                chosen.setStatus(PartitionStatus.ALLOCATED);
                allocatedPartition = chosen;
            } else {
                Partition newP = new Partition(chosen.getStart(), job.getSize(), PartitionStatus.ALLOCATED);
                Partition oldP = new Partition(newP.getEnd() + 1, chosen.getSize() - job.getSize(), PartitionStatus.FREE);
                partitions.remove(chosen);
                partitions.add(newP);
                partitions.add(oldP);
                allocatedPartition = newP;
            }

            this.freeSize -= job.getSize();
            this.allocatedJobTable.put(job, allocatedPartition);
            this.allocatedJobTable1.put(allocatedPartition, job);
            Memory.getInstance().dumpMemory("- successfully Allocated job" + job.getId() + " in memory");
            return true;
        }
        return false;
    }

    public synchronized boolean deAllocatedJobFromMemory(Job job) {
        Partition partition = this.allocatedJobTable.get(job);
        if (partition != null) {
            partition.setStatus(PartitionStatus.FREE);
            this.freeSize += job.getSize();
            this.allocatedJobTable.remove(job);
            this.allocatedJobTable1.remove(allocatedJobTable.get(job));
            return true;
        }
        return false;
    }

    private Partition firstFit(int size) {
        for (Partition partition : this.partitions) {
            if (partition.getStatus() == PartitionStatus.FREE && partition.getSize() >= size) {
                return partition;
            }
        }
        return null;
    }

    private void initiateDefragmentationThread() {
        Thread defragmentationThread = new Thread(() -> {
            while (true) {
                if (freePartitionNumber() > 3 || (freeSize == totalSize && getPartitions().size() > 1)) {
                    Memory.getInstance().DefragMemory();
                }
            }
        });
        defragmentationThread.start();
    }

    private int freePartitionNumber() {
        int counter = 0;

        try {
            for (int i = 0; i < this.partitions.size(); i++) {
                if (partitions.get(i).getStatus() == PartitionStatus.FREE) {
                    counter++;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());;
        }

        return counter;
    }

    private void DefragMemory() {
        nonContiguousBlocksDefragmentation();
        dumpMemory("- defragmentation done");
    }

    private void nonContiguousBlocksDefragmentation() {
        removeFreePartitions();
        int freeStart = shiftAllocatedPartitions();
        Partition freePartition = new Partition(freeStart, freeSize, PartitionStatus.FREE);
        this.partitions.add(freePartition);
    }

    private void removeFreePartitions() {
        for (int i = 0; i < this.partitions.size(); ++i) {
            if (this.partitions.get(i).getStatus() == PartitionStatus.FREE) {
                this.partitions.remove(i);
                i--;
            }
        }
    }

    private int shiftAllocatedPartitions() {
        int start = 0;
        for (Partition partition : this.partitions) {
            if (partition.getStart() != start) {
                partition.setStart(start);
                start = partition.getEnd() + 1;
            }
        }
        return start;
    }

    public synchronized void dumpMemory(String Message) {
        StringBuilder log =  new StringBuilder();
        log.append("$ " + Message + "\n");
        log.append("**************** Memory Info ******************\n");
        log.append("Total Size = " + this.totalSize + "\n");
        log.append("Free Size = " + this.freeSize + "\n");
        log.append("Used Size = " + (this.totalSize - this.freeSize) + "\n");
        log.append("number of partitions = " + this.partitions.size() + "\n");
        log.append("number of used partitions = " + (this.partitions.size() - freePartitionNumber()) + "\n");
        log.append("number of free partitions = " + freePartitionNumber() + "\n");
        log.append("\n");
        log.append("Partitions:\n");
        int index = 1;
        for (Partition partition : this.partitions) {
            log.append(index++ + "-  " + partition.getStatus() + " : " + partition.getSize() + " MB");
            if (partition.getStatus() == PartitionStatus.ALLOCATED) {
                log.append(" : job" + allocatedJobTable1.get(partition).getId());
            }
            log.append("\n");
        }

        log.append("**********************************************\n");
        System.out.println(log);

    }
}
