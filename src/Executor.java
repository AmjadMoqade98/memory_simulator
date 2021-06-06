
import java.util.*;

public class Executor extends Thread {
    private LinkedList<Job> readyQueue;
    private LinkedList<Job> jobQueue;

    private static Executor instance;

    public static Executor getInstance() {
        if (instance == null) {
            instance = new Executor();
            instance.readyQueue = new LinkedList<>();
            instance.jobQueue = new LinkedList<>();
        }

        return instance;
    }

    private Executor(){}


    public void run() {
        System.out.println("- Simulation Started..." + "\n");

        boolean workingFlag = true;
        boolean fitFlag = true;
        while (true) {
            if(readyQueue.size() == 0 && jobQueue.size() == 0 && workingFlag) {
                System.out.println("- There is no jobs to execute" + "\n");
                workingFlag = false;
            }
            if(readyQueue.size() < 8 && jobQueue.size() > 0){
                workingFlag = true;
                boolean fit = MoveJobToReadyQueue();
                if(fit) {
                    fitFlag = true;
                }
                if(!fit && fitFlag){

                    System.out.println("- can't find a hole to fit the job\n" +
                            "- waiting for defragmentation or execution finish...\"+ \"\\n\" ");
                    fitFlag = false;
                }

            }
        }
    }

    public synchronized void addToJobQueue(Job job) {
        jobQueue.add(job);
    }

    public Job pollFromJobQueue() {
        return jobQueue.poll();
    }

    public synchronized void removeFromReadyQueue(Job job) {
        readyQueue.remove(job);
    }

    public synchronized boolean MoveJobToReadyQueue() {
        Job job = jobQueue.peek();

        boolean findFit = Memory.getInstance().AllocateJobInMemory(job);
        if(!findFit) return false;

        job = jobQueue.poll();
        readyQueue.add(job);

        Job job1 = job;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                removeFromReadyQueue(job1);
                Memory.getInstance().deAllocatedJobFromMemory(job1);
                addToJobQueue(Job.generateRandomJob());
                Memory.getInstance().dumpMemory("job" + job1.getId() + " finished execution ");
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, job.getTimeLimit()* 1000L);
        return true;
    }
}
