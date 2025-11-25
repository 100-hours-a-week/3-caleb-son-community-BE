package com.ktb.community.service;

import com.ktb.community.domain.Post;
import com.ktb.community.domain.PostImage;
import com.ktb.community.domain.User;
import com.ktb.community.dto.PostDtos.CreatePostRequest;
import com.ktb.community.exception.ApiException;
import com.ktb.community.exception.ErrorCode;
import com.ktb.community.repository.PostImageRepository;
import com.ktb.community.repository.PostLikeRepository;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension ;
import org.springframework.test.util.ReflectionTestUtils ;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트(유닛): PostService.create
 * - UserRepository / PostRepository / PostImageRepository 등을 mock 으로 검증
 * - DB/Hibernate 없이 로직/호출만 검증
 */
@ExtendWith(MockitoExtension.class)
class PostServiceCreateUnitTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock PostImageRepository postImageRepository;
    // 서비스가 더 많은 빈을 의존한다면 여기서 함께 @Mock 선언해 주세요.
    @Mock PostLikeRepository postLikeRepository;

    @InjectMocks PostService postService;

    @Captor ArgumentCaptor<List<PostImage>> imagesCaptor;

    @DisplayName("게시글 생성 성공 - 이미지 2장, sortOrder 1..2, 기본 카운터 0")
    @Test
    void create_success() {
        // given
        Integer userId = 1;
        CreatePostRequest request = new CreatePostRequest(
                "첫 글", "본문입니다",
                "https://img.example/a.jpg,https://img.example/b.jpg"
        );

        // 유저 존재 세팅
        User author = new User();
        author.setNickname("neo");
        author.setEmail("neo@test.com");
        author.setPassword("pass1234");
        ReflectionTestUtils.setField(author, "id", userId);

        // save 결과로 반환될 Post(저장 후 id 부여된 상태로 가정)
        Post persisted = new Post();
        persisted.setAuthor(author);
        persisted.setTitle(request.title());
        persisted.setContent(request.content());
        persisted.setViewCount(0);
        persisted.setLikeCount(0);
        persisted.setCommentCount(0);
        ReflectionTestUtils.setField(persisted, "id", 100); // 새로 발급된 PK라고 가정

        // 스텁
        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(persisted);
        // saveAll 은 단순 호출 검증만 할 예정이므로 반환값만 동일 리스트로 돌려줌
        when(postImageRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // when
        Post result = postService.create(userId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getAuthor().getId()).isEqualTo(userId);
        assertThat(result.getTitle()).isEqualTo("첫 글");
        assertThat(result.getContent()).isEqualTo("본문입니다");
        assertThat(result.getViewCount()).isZero();
        assertThat(result.getLikeCount()).isZero();
        assertThat(result.getCommentCount()).isZero();

        // 호출/인자 검증
        verify(userRepository, times(1)).findById(userId);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(postImageRepository, times(1)).saveAll(imagesCaptor.capture());

        List<PostImage> savedImages = imagesCaptor.getValue();
        assertThat(savedImages).hasSize(2);
        assertThat(savedImages)
                .extracting(PostImage::getSortOrder)
                .containsExactly(1, 2);
        assertThat(savedImages)
                .extracting(PostImage::getImageUrl)
                .containsExactly("https://img.example/a.jpg", "https://img.example/b.jpg");

        // 각 이미지가 방금 생성된 게시글(100)에 매핑되었는지 확인
        assertThat(savedImages).allSatisfy(img ->
                assertThat(img.getPost().getId()).isEqualTo(100)
        );
    }

//    @DisplayName("게시글 생성 실패 - 사용자 없음")
//    @Test
//    void create_whenUserNotFound_thenThrows() {
//        // given
//        Integer userId = 999_999;
//        CreatePostRequest request = new CreatePostRequest("제목", "본문", List.of());
//
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // when
//        ApiException ex = org.junit.jupiter.api.Assertions.assertThrows(
//                ApiException.class, () -> postService.create(userId, request)
//        );
//
//        // then
//        assertThat(ex.getMessage()).contains("user_not_found");
//    }
}
