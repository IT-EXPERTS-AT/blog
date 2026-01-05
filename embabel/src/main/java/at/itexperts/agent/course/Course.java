package at.itexperts.agent.course;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.annotation.Id;

public record Course(@Id Long id, String sport, String level, int durationInHours, String location, String marketingText) {

   @Tool(description = "returns a short summary about the sport course")
   public String getSummary() {
      return "Sport: %s at %s level for %d hours at %s".formatted(sport, level, durationInHours, location);
   }
}

