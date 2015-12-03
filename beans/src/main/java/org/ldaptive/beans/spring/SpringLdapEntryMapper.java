/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.util.Calendar;
import org.ldaptive.beans.AbstractLdapEntryMapper;
import org.ldaptive.beans.ClassDescriptor;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;

/**
 * Uses a {@link SpringClassDescriptor} for ldap entry mapping.
 *
 * @param  <T>  type of object to map
 *
 * @author  Middleware Services
 */
public class SpringLdapEntryMapper<T> extends AbstractLdapEntryMapper<T>
{

  /** Type converter used by all contexts. */
  private final TypeConverter typeConverter;


  /** Default constructor. */
  public SpringLdapEntryMapper()
  {
    this((Converter<?, ?>) null);
  }


  /**
   * Creates a new spring ldap entry mapper.
   *
   * @param  c  additional converters to add to the spring conversion service.
   */
  public SpringLdapEntryMapper(final Converter<?, ?>... c)
  {
    typeConverter = createTypeConverter(c);
  }


  @Override
  protected ClassDescriptor getClassDescriptor(final Object object)
  {
    final SpringClassDescriptor descriptor = new SpringClassDescriptor(createEvaluationContext(object));
    descriptor.initialize(object.getClass());
    return descriptor;
  }


  /**
   * Creates an evaluation context to use in the spring class descriptor. Adds the default converters from the default
   * conversion service.
   *
   * @param  object  to supply to the evaluation context
   *
   * @return  evalutation context
   */
  protected EvaluationContext createEvaluationContext(final Object object)
  {
    final StandardEvaluationContext context = new StandardEvaluationContext(object);
    context.setTypeConverter(typeConverter);
    return context;
  }


  /**
   * Returns a type converter that is initialized with the supplied converters and any converters supplied by {@link
   * #addDefaultConverters(GenericConversionService)}.
   *
   * @param  converters  to add to the conversion service
   *
   * @return  type converter
   */
  protected TypeConverter createTypeConverter(final Converter<?, ?>... converters)
  {
    final GenericConversionService conversionService = new GenericConversionService();
    DefaultConversionService.addDefaultConverters(conversionService);
    if (converters != null) {
      for (Converter<?, ?> converter : converters) {
        conversionService.addConverter(converter);
      }
    }
    addDefaultConverters(conversionService);
    return new StandardTypeConverter(conversionService);
  }


  /**
   * Adds default converters to the supplied conversion service.
   *
   * @param  service  to add default converters to
   */
  protected void addDefaultConverters(final GenericConversionService service)
  {
    if (!service.canConvert(String.class, Calendar.class)) {
      service.addConverter(
        new Converter<String, Calendar>() {
          @Override
          public Calendar convert(final String s)
          {
            final GeneralizedTimeValueTranscoder transcoder = new GeneralizedTimeValueTranscoder();
            return transcoder.decodeStringValue(s);
          }
        });
    }
    if (!service.canConvert(Calendar.class, String.class)) {
      service.addConverter(
        new Converter<Calendar, String>() {
          @Override
          public String convert(final Calendar c)
          {
            final GeneralizedTimeValueTranscoder transcoder = new GeneralizedTimeValueTranscoder();
            return transcoder.encodeStringValue(c);
          }
        });
    }
  }
}
