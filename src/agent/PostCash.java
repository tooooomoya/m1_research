package agent;

import constants.Const;

public class PostCash {
    private Post[] postQueue;
    private int size;
    private int maxNumOfPostCash;

    public PostCash(){
        this.maxNumOfPostCash = Const.MAX_READABLE_POSTS_NUM;
        this.postQueue = new Post[Const.MAX_READABLE_POSTS_NUM];
        this.size = 0;
    }

    public void reset() {
        for (int i = 0; i < size; i++) {
            postQueue[i] = null;
        }
        size = 0;
    }

    // 投稿を追加（古いものから押し出す）
    public void addPost(Post post) {
        if (size < maxNumOfPostCash) {
            postQueue[size] = post;
            size++;
        } else {
            // FIFO: 先頭を削除して後ろに詰める
            for (int i = 1; i < maxNumOfPostCash; i++) {
                postQueue[i - 1] = postQueue[i];
            }
            postQueue[maxNumOfPostCash - 1] = post;
        }
    }

    public Post getPost(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range.");
        }
        return postQueue[index];
    }

    public int getSize() {
        return size;
    }

    public Post[] getAllPosts() {
        Post[] currentPosts = new Post[size];
        System.arraycopy(postQueue, 0, currentPosts, 0, size);
        return currentPosts;
    }
}
