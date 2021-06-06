import java.util.Random;

public class Job {
    private static int currentId = 0 ;

    private int id ;

    // execution time in seconds
    private int timeLimit;

    private int size;

    public Job(int timeLimit, int size) {
        this.id = currentId++;
        this.timeLimit = timeLimit;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getSize() {
        return size;
    }

    public static Job generateRandomJob() {
        int min = 50;
        int max = 500;
        int size = (int) Math.floor(Math.random() * (max - min + 1) + min);

        min = 1;
        max = 10;
        int timeLimit = (int) Math.floor(Math.random() * (max - min + 1) + min);

        return new Job(timeLimit, size);
    }
}
