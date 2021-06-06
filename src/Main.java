public class Main {
    public static void main(String[] args) throws InterruptedException {

        Executor executor = Executor.getInstance();

        for (int i = 0 ; i < 12 ; i++) {
            Executor.getInstance().addToJobQueue(Job.generateRandomJob());
        }

        System.out.println("- Created 12 random jobs in the job queue" );
        executor.start();
    }
}