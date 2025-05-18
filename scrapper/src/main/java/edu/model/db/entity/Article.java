package edu.model.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_author"))
    private User author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "text_content", nullable = false, columnDefinition = "TEXT")
    private String textContent;

    @SuppressWarnings("MagicNumber")
    @Column(name = "time_to_read")
    private Integer timeToRead = 30;

    @Column(name = "creation_date", updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean visibility = false;

    @SuppressWarnings("MultipleStringLiterals")
    @Column(length = 50)
    private String status = "draft";

    @Column
    private Integer views = 0;

    @Column
    private Integer likes = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "articles_categories",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "articles_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @SuppressWarnings({"MagicNumber", "MultipleStringLiterals"})
    @PrePersist
    protected void onCreate() {
        this.timeToRead = 30;
        this.creationDate = LocalDateTime.now();
        this.lastUpdateDate = LocalDateTime.now();
        this.visibility = false;
        this.status = "draft";
        this.views = 0;
        this.likes = 0;
    }
}
