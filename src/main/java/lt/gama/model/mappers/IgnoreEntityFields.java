package lt.gama.model.mappers;

import org.mapstruct.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "createdOn", ignore = true)
@Mapping(target = "updatedOn", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "archive", ignore = true)
@Mapping(target = "hidden", ignore = true)
@Mapping(target = "version", ignore = true)
@Mapping(target = "companyId", ignore = true)
@Mapping(target = "exportId", ignore = true)
public @interface IgnoreEntityFields {
}
