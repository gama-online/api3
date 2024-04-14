package lt.gama.jpa.generators;

import org.hibernate.annotations.ValueGenerationType;

import java.lang.annotation.*;

@ValueGenerationType(generatedBy = EditorValueGeneration.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface GeneratedEditorValue {
}
