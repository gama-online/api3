//package lt.gama.jpa;
//
//import lt.gama.service.JsonService;
//import org.hibernate.type.descriptor.WrapperOptions;
//import org.hibernate.type.descriptor.java.JavaType;
//import org.hibernate.type.format.FormatMapper;
//import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
//
//public class GamaJsonMapper implements FormatMapper {
//
//    private final JacksonJsonFormatMapper jacksonJsonFormatMapper = new JacksonJsonFormatMapper(JsonService.mapper());
//
//    @Override
//    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
//        return jacksonJsonFormatMapper.fromString(charSequence, javaType, wrapperOptions);
//    }
//
//    @Override
//    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
//        return jacksonJsonFormatMapper.toString(value, javaType, wrapperOptions);
//    }
//}
