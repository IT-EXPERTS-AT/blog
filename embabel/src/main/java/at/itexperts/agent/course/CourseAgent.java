package at.itexperts.agent.course;

import java.util.Random;

import at.itexperts.agent.NoResult;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;

@Agent(description = "Course")
public class CourseAgent {

   private static final Random RANDOM = new Random();
   private final CourseRepository courseRepository;

   public CourseAgent(CourseRepository courseRepository) {this.courseRepository = courseRepository;}

   @Action
   public MarketingText generateText(Course course, OperationContext context) {
      var shouldReplacePrompt = RANDOM.nextDouble() < 0.2;
      var prompt = """
            Create a catching marketing text for the following sport course:
            Type: %s, Level: %s, Duration: %s, Location: %s
            """.formatted(
            course.sport(), course.level(), course.durationInHours(), course.location());

      var wrongPrompt = "Write a short story";

      return context.ai()
            .withDefaultLlm()
            .createObject(shouldReplacePrompt ? wrongPrompt : prompt, MarketingText.class);
   }

   @Action
   public ReviewedMarketingText reviewText(Course course, MarketingText text, OperationContext context) {
      var prompt = """
            Review this marketing text carefully: %s
            Does it fit the course:
            Type: %s, Level: %s, Duration: %s, Location: %s?
            """.formatted(text.text(), course.sport(), course.level(), course.durationInHours(), course.location());

      return context.ai()
            .withDefaultLlm()
            .createObject(prompt, ReviewedMarketingText.class);
   }

   @AchievesGoal(description = "Generate a marketing text for a sport course and store it in the database")
   @Action
   public NoResult updateCourseMarketingText(Course course, ReviewedMarketingText marketingText) {
      if (marketingText.passedReview()) {
         courseRepository.updateMarketingText(course.id(), marketingText.text());
      } else {
         courseRepository.updateMarketingText(course.id(), "No text available. Generated Text failed review");
      }
      return new NoResult();
   }

}
