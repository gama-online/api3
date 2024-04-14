package lt.gama.jpa.generators;

import org.hibernate.annotations.ValueGenerationType;

import java.lang.annotation.*;

@ValueGenerationType(generatedBy = CreatorValueGeneration.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface GeneratedCreatorValue {
}
