public class Partition {
    private int start ;
    private int end ;
    private int size ;
    private PartitionStatus status;

    public Partition(int start, int size , PartitionStatus status) {
        this.start = start;
        this.size = size;
        this.status = status;
        this.end = start + size ;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        this.end = start + end;
    }

    public int getEnd() {
        return end;
    }

    public int getSize() {
        return size;
    }

    public PartitionStatus getStatus() {
        return status;
    }

    public void setStatus(PartitionStatus status) {
        this.status = status;
    }

}
