package com.ebanking.graphql.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class GraphQLConfig {

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.Json);
  }

  @Bean
  public RuntimeWiringConfigurer uploadScalarConfigurer() {
    return wiringBuilder ->
        wiringBuilder.scalar(
            GraphQLScalarType.newScalar()
                .name("Upload")
                .description("Represents a file upload in multipart request")
                .coercing(
                    new Coercing<MultipartFile, Void>() {

                      @Override
                      public Void serialize(Object result) throws CoercingSerializeException {
                        throw new CoercingSerializeException("Upload scalar is input-only");
                      }

                      @Override
                      public MultipartFile parseValue(Object input)
                          throws CoercingParseValueException {
                        if (input instanceof MultipartFile file) {
                          return file;
                        }
                        throw new CoercingParseValueException(
                            "Expected MultipartFile but got: "
                                + (input == null ? "null" : input.getClass().getName()));
                      }

                      @Override
                      public MultipartFile parseLiteral(Object input)
                          throws CoercingParseLiteralException {
                        throw new CoercingParseLiteralException(
                            "Upload scalar does not support literal parsing");
                      }
                    })
                .build());
  }
}
