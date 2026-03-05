package com.malgn.controller;

import com.malgn.dto.ContentCreateRequest;
import com.malgn.dto.ContentResponse;
import com.malgn.dto.ContentUpdateRequest;
import com.malgn.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/content")
public class ContentController {
    private final ContentService contentService;

    @Autowired
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    @Operation(summary = "컨텐츠 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<ContentResponse> createContent(@RequestBody ContentCreateRequest request) {
        String username = request.createdBy();
        ContentResponse createdContent = contentService.createContent(request, username);

        return ResponseEntity.ok(createdContent);
    }

    @GetMapping("/{contentId}")
    @Operation(summary = "컨텐츠 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "컨텐츠 없음")
    })
    public ResponseEntity<ContentResponse> getContent(@PathVariable Long contentId) {
        ContentResponse content = contentService.getContentById(contentId);
        return ResponseEntity.ok(content);
    }

    @GetMapping
    @Operation(summary = "컨텐츠 전체 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity<Page<ContentResponse>> getAllContents(@RequestParam int page, @RequestParam int size) {
        Page<ContentResponse> contents = contentService.getAllContents(page, size);
        return ResponseEntity.ok(contents);
    }

    @GetMapping("/user/{createdBy}")
    @Operation(summary = "사용자가 작성한 컨텐츠 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<Page<ContentResponse>> getContentsByCreatedBy(@PathVariable String createdBy, @RequestParam int page, @RequestParam int size) {
        Page<ContentResponse> contents = contentService.getContentByCreatedBy(createdBy, page, size);
        return ResponseEntity.ok(contents);
    }

    @PutMapping("/{contentId}")
    @Operation(summary = "컨텐츠 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "컨텐츠 없음")
    })
    public ResponseEntity<ContentResponse> updateContent(@PathVariable Long contentId, @RequestBody ContentUpdateRequest request) {
        ContentResponse content = contentService.updateContent(contentId, request);
        return ResponseEntity.ok(content);
    }

    @DeleteMapping("/{contentId}")
    @Operation(summary = "컨텐츠 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "성공"),
            @ApiResponse(responseCode = "404", description = "컨텐츠 없음")
    })
    public ResponseEntity<Void> deleteContent(@PathVariable Long contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }
}
