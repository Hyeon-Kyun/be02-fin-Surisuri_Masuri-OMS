package com.example.Surisuri_Masuri.question.model.entity;

import com.example.Surisuri_Masuri.member.Model.Entity.User;
import com.example.Surisuri_Masuri.question.model.request.PatchUpdateQuestionReq;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "answer")
public class Question   {

    // TODO: 2024-03-10 user 테이블과 관계 맺어야 함 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 1)
    private Boolean status;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_idx")
    private User user;

    @OneToOne(mappedBy = "question", fetch = FetchType.LAZY)
    private Answer answer;

    @PrePersist
    void createdAt() {
        this.createdAt = Timestamp.from(Instant.now());
        this.updatedAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public void update(PatchUpdateQuestionReq patchUpdateQuestionReq) {
        if (patchUpdateQuestionReq.getCategory() != null) {
            this.category = patchUpdateQuestionReq.getCategory();
        }
        if (patchUpdateQuestionReq.getTitle() != null) {
            this.title = patchUpdateQuestionReq.getTitle();
        }
        if (patchUpdateQuestionReq.getContent() != null) {
            this.content = patchUpdateQuestionReq.getContent();
        }
        if (patchUpdateQuestionReq.getStatus() != null) {
            this.status = patchUpdateQuestionReq.getStatus();
        }
    }
}

