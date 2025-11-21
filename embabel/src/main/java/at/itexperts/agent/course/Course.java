package at.itexperts.agent.course;

import org.springframework.data.annotation.Id;

public record Course(@Id Long id, String sport, String level, int durationInHours, String location, String marketingText) {}

