package at.itexperts.agent.course;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.embabel.agent.core.Agent;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;

@RequestMapping("/course")
@RestController
public class CourseController {

   private final AgentPlatform agentPlatform;
   private final CourseRepository courseRepository;

   public CourseController(AgentPlatform agentPlatform, CourseRepository courseRepository) {
      this.agentPlatform = agentPlatform;
      this.courseRepository = courseRepository;
   }

   @PostMapping()
   public Long createCourse(@RequestBody Course course) {
      Course saved = courseRepository.save(course);
      Agent agent = agentPlatform.agents().getFirst();
      AgentProcess agentProcess = agentPlatform.createAgentProcess(agent, ProcessOptions.DEFAULT, Map.of("course", saved));
      agentPlatform.start(agentProcess);
      return saved.id();
   }

   @GetMapping("/{id}")
   public Course getCourse(@PathVariable Long id) {
      return courseRepository.findById(id).orElseThrow();
   }
}
