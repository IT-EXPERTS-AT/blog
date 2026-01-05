package at.itexperts.agent.course;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends CrudRepository<Course, Long> {

   @Modifying
   @Query("update course set marketing_text = :marketingText where id = :id")
   void updateMarketingText(@Param("id") Long id, @Param("marketingText") String marketingText);

}

