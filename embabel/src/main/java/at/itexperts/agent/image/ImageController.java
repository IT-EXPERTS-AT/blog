package at.itexperts.agent.image;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.embabel.agent.core.Agent;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcess;
import com.embabel.agent.core.ProcessOptions;

@RequestMapping("/image")
@RestController
public class ImageController {

   private final AgentPlatform agentPlatform;

   public ImageController(AgentPlatform agentPlatform) {
      this.agentPlatform = agentPlatform;
   }

   @PostMapping()
   public void processImage(@RequestParam("image") MultipartFile imageFile) {
      try {
         if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("No image file provided");
         }

         byte[] imageBytes = imageFile.getBytes();

         List<Agent> agents = agentPlatform.agents();
         Agent agent = agents.stream().filter(a -> a.getName().contains("Image")).findFirst().orElseThrow();
         AgentProcess agentProcess = agentPlatform.createAgentProcess(agent, ProcessOptions.DEFAULT,
            Map.of("imageData", new ImageAgent.ImageData(imageBytes)));
         agentPlatform.start(agentProcess);
      } catch (IOException e) {
         throw new RuntimeException("Error processing uploaded image", e);
      }
   }
}
