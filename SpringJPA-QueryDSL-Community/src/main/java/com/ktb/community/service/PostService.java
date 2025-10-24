package com.ktb.community.service;

import com.ktb.community.domain.*;
import com.ktb.community.dto.PostDtos.*;
import com.ktb.community.exception.ApiException;
import com.ktb.community.exception.ErrorCode;
import com.ktb.community.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {
    private final PostRepository posts;
    private final UserRepository users;
    private final PostLikeRepository likes;
    private final PostImageRepository images;

    public PostService(PostRepository posts, UserRepository users, PostLikeRepository likes, PostImageRepository images) {
        this.posts = posts; this.users = users; this.likes = likes; this.images = images;
    }

    public Page<Post> search(String query, Integer authorId, Boolean hasImage, java.time.LocalDateTime from, java.time.LocalDateTime to,
                             int page, int size, String sortKey) {
        Sort sort = switch (sortKey == null ? "LATEST" : sortKey.toUpperCase()) {
            case "POPULAR" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "VIEW" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "publishedAt");
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        return posts.search(query, authorId, hasImage, from, to, pageable);
    }

    public Post get(Integer id, boolean increaseView) {
        Post p = posts.findActiveById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "post_not_found"));
        if (increaseView) { p.setViewCount(p.getViewCount() + 1); posts.save(p); }
        return p;
    }

    @Transactional
    public Post create(Integer authorId, CreatePostRequest req) {
        User author = users.findById(authorId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "token_not_valid"));

        Post p = new Post();
        p.setAuthor(author); p.setTitle(req.title()); p.setContent(req.content());
        p = posts.save(p);
        if (req.imageUrls() != null && !req.imageUrls().trim().isEmpty()) {
            List<PostImage> list = new ArrayList<>();
            // imageUrls를 쉼표로 분리하여 개별 URL로 처리
            String[] imageUrls = req.imageUrls().split(",");
            int i = 0;
            for (String url : imageUrls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    PostImage img = new PostImage();
                    img.setPost(p);
                    img.setImageUrl(trimmedUrl);
                    img.setSortOrder(++i);
                    list.add(img);
                }
            }
            if (!list.isEmpty()) {
                images.saveAll(list);
            }
        }
        return p;
    }

    @Transactional
    public Post update(Integer authorId, Integer postId, UpdatePostRequest req) {
        Post p = posts.findActiveById(postId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "post_not_found"));
        if (!p.getAuthor().getId().equals(authorId)) throw new ApiException(ErrorCode.FORBIDDEN, "not_authorized");
        if (req.title() != null) p.setTitle(req.title());
        if (req.content() != null) p.setContent(req.content());
        p = posts.save(p);
        if (req.imageUrls() != null && !req.imageUrls().trim().isEmpty()) {
            var old = images.findByPostIdOrderBySort(postId);
            images.deleteAll(old);
            List<PostImage> list = new ArrayList<>();
            // imageUrls를 쉼표로 분리하여 개별 URL로 처리
            String[] imageUrls = req.imageUrls().split(",");
            int i = 0;
            for (String url : imageUrls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    PostImage img = new PostImage();
                    img.setPost(p);
                    img.setImageUrl(trimmedUrl);
                    img.setSortOrder(++i);
                    list.add(img);
                }
            }
            if (!list.isEmpty()) {
                images.saveAll(list);
            }
        }
        return p;
    }

    public void softDelete(Integer authorId, Integer postId) {
        Post p = posts.findActiveById(postId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "post_not_found"));
        if (!p.getAuthor().getId().equals(authorId)) throw new ApiException(ErrorCode.FORBIDDEN, "not_authorized");
        p.setDeleted(true); posts.save(p);
    }

    public long like(Integer userId, Integer postId) {
        User u = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "token_not_valid"));
        Post p = get(postId, false);
        if (likes.findByUserIdAndPostId(u.getId(), p.getId()).isPresent()) throw new ApiException(ErrorCode.ALREADY_LIKED, "already_liked");
        likes.save(new PostLike(u, p));
        long cnt = likes.countByPostId(p.getId());
        p.setLikeCount((int)cnt); posts.save(p);
        return cnt;
    }

    public long unlike(Integer userId, Integer postId) {
        User u = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "token_not_valid"));
        Post p = get(postId, false);
        likes.findByUserIdAndPostId(u.getId(), p.getId()).ifPresent(likes::delete);
        long cnt = likes.countByPostId(p.getId());
        p.setLikeCount((int)cnt); posts.save(p);
        return cnt;
    }

    public Page<Post> likedBy(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return likes.findPostsLikedByUser(userId, pageable);
    }
}
