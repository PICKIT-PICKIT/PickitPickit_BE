package PickitPickit.onboarding.dto.response;

public record OnboardingCompleteResponse(
        boolean onboardingCompleted,
        String next
) {

    public static OnboardingCompleteResponse mainMap() {
        return new OnboardingCompleteResponse(true, "MAIN_MAP");
    }
}
