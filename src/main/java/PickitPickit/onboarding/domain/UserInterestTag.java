package PickitPickit.onboarding.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_interest_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_interest_tags_user_tag",
                        columnNames = {"user_id", "interest_tag_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterestTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_tag_id", nullable = false)
    private InterestTag interestTag;

    private UserInterestTag(User user, InterestTag interestTag) {
        this.user = user;
        this.interestTag = interestTag;
    }

    public static UserInterestTag create(User user, InterestTag interestTag) {
        return new UserInterestTag(user, interestTag);
    }
}
