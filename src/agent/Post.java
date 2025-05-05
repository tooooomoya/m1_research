package agent;

public class Post {
    private int postUserId;
    private double postOpinion;
    private int postedStep;

    public Post(int postUserId, double postOpinion, int postedStep){
        this.postUserId = postUserId;
        this.postOpinion = postOpinion;
        this.postedStep = postedStep;    
    }

    // Getter
    public int getPostUserId() {
        return postUserId;
    }

    public double getPostOpinion() {
        return postOpinion;
    }

    public int getPostedStep() {
        return postedStep;
    }

    // Setter
    public void setPostUserId(int postUserId) {
        this.postUserId = postUserId;
    }

    public void setPostOpinion(double postOpinion) {
        this.postOpinion = postOpinion;
    }

    public void setPostedStep(int postedStep) {
        this.postedStep = postedStep;
    }

    // other
    public Post copyPost(){
        Post copiedPost = new Post(this.postUserId, this.postOpinion, this.postedStep);
        return copiedPost;
    }
}
