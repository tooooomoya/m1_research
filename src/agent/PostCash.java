package agent;

public class PostCash {
    private int maxNumOfPostCash;
    private Post[] postCash;
    private int size;

    public PostCash(int maxNumOfPostCash){
        this.maxNumOfPostCash = maxNumOfPostCash;
        this.postCash = new Post[maxNumOfPostCash];
        this.size = 0;
    }

    // 投稿を追加（古いものから押し出す）
    public void addPost(Post post) {
        if (size < maxNumOfPostCash) {
            postCash[size] = post;
            size++;
        } else {
            // FIFO: 先頭を削除して後ろに詰める
            for (int i = 1; i < maxNumOfPostCash; i++) {
                postCash[i - 1] = postCash[i];
            }
            postCash[maxNumOfPostCash - 1] = post;
        }
    }

    public Post getPost(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range.");
        }
        return postCash[index];
    }

    public int getSize() {
        return size;
    }

    public Post[] getAllPosts() {
        Post[] currentPosts = new Post[size];
        System.arraycopy(postCash, 0, currentPosts, 0, size);
        return currentPosts;
    }
}
