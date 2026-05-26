package PickitPickit.onboarding.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "interest_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_interest_tags_name", columnNames = "name")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;
}
