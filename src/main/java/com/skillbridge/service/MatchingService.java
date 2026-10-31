package com.skillbridge.service;

import com.skillbridge.model.Project;
import com.skillbridge.model.Skill;
import com.skillbridge.model.User;
import com.skillbridge.model.UserSkill;
import com.skillbridge.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Transparent rule-based matching engine:
 * 01: Compare candidate skills with project requirements
 * 02: Basic overlap score = matched required skills / total required skills
 * 03: Example: Java and MySQL matching 2 out of 3 requirements = 67%
 * 04: Weighted score factors in UserSkill.proficiency (1-5 scale)
 */
@Service
public class MatchingService {

    private final UserRepository users;

    public MatchingService(UserRepository users) {
        this.users = users;
    }

    public static class Candidate {
        private final User user;
        private final int score;             // 0-100 (basic overlap or primary score)
        private final int weightedScore;     // 0-100 (proficiency-weighted)
        private final List<String> matched;
        private final List<String> missing;
        private final double averageProficiency;

        public Candidate(User user, int score, List<String> matched) {
            this(user, score, score, matched, List.of(), 0.0);
        }

        public Candidate(User user, int score, int weightedScore, List<String> matched,
                         List<String> missing, double averageProficiency) {
            this.user = user;
            this.score = score;
            this.weightedScore = weightedScore;
            this.matched = matched != null ? matched : List.of();
            this.missing = missing != null ? missing : List.of();
            this.averageProficiency = averageProficiency;
        }

        public User getUser() {
            return user;
        }

        public int getScore() {
            return score;
        }

        public int getWeightedScore() {
            return weightedScore;
        }

        public List<String> getMatched() {
            return matched;
        }

        public List<String> getMissing() {
            return missing;
        }

        public double getAverageProficiency() {
            return averageProficiency;
        }

        public String getExplanation() {
            if (matched.isEmpty()) {
                return "No matching required skills yet";
            }
            return matched.size() + " skill" + (matched.size() > 1 ? "s" : "") + " matched (" +
                   String.join(", ", matched) + ")";
        }
    }

    /** Basic overlap rule: score = matched required skills / total required skills. */
    public Candidate score(Project project, User user) {
        Set<Long> requiredIds = new HashSet<>();
        Map<Long, String> requiredNames = new HashMap<>();
        for (Skill s : project.getRequiredSkills()) {
            if (s.getId() != null) {
                requiredIds.add(s.getId());
                requiredNames.put(s.getId(), s.getName());
            }
        }

        if (requiredIds.isEmpty()) {
            return new Candidate(user, 0, 0, List.of(), List.of(), 0.0);
        }

        List<String> matched = new ArrayList<>();
        Set<Long> matchedIds = new HashSet<>();
        int totalProficiency = 0;
        double weightedSum = 0.0;

        for (UserSkill us : user.getSkills()) {
            if (us.getSkill() != null && requiredIds.contains(us.getSkill().getId())) {
                matched.add(us.getSkill().getName());
                matchedIds.add(us.getSkill().getId());
                int prof = Math.max(1, Math.min(5, us.getProficiency()));
                totalProficiency += prof;
                double factor = switch (prof) {
                    case 1 -> 0.60;
                    case 2 -> 0.75;
                    case 3 -> 0.85;
                    case 4 -> 0.95;
                    case 5 -> 1.00;
                    default -> 0.85;
                };
                weightedSum += factor;
            }
        }

        List<String> missing = new ArrayList<>();
        for (Map.Entry<Long, String> entry : requiredNames.entrySet()) {
            if (!matchedIds.contains(entry.getKey())) {
                missing.add(entry.getValue());
            }
        }

        int basicScore = Math.round(100f * matched.size() / requiredIds.size());
        int weightedScore = Math.min(100, (int) Math.round(100.0 * (weightedSum / requiredIds.size())));
        double avgProf = matched.isEmpty() ? 0.0 : ((double) totalProficiency / matched.size());

        return new Candidate(user, basicScore, weightedScore, matched, missing, avgProf);
    }

    /** Proficiency-weighted candidate score */
    public Candidate scoreWeighted(Project project, User user) {
        Candidate base = score(project, user);
        return new Candidate(
            base.getUser(),
            base.getWeightedScore(),
            base.getWeightedScore(),
            base.getMatched(),
            base.getMissing(),
            base.getAverageProficiency()
        );
    }

    /** Rank all candidates excluding project lead, highest match first */
    public List<Candidate> rankCandidates(Project project) {
        return users.findAll().stream()
                .filter(u -> project.getLead() == null || !u.getId().equals(project.getLead().getId()))
                .map(u -> score(project, u))
                .sorted((a, b) -> {
                    int c = Integer.compare(b.getScore(), a.getScore());
                    return c != 0 ? c : Integer.compare(b.getWeightedScore(), a.getWeightedScore());
                })
                .toList();
    }

    /** Rank candidates with proficiency weighting */
    public List<Candidate> rankCandidatesWeighted(Project project) {
        return users.findAll().stream()
                .filter(u -> project.getLead() == null || !u.getId().equals(project.getLead().getId()))
                .map(u -> scoreWeighted(project, u))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();
    }
}
